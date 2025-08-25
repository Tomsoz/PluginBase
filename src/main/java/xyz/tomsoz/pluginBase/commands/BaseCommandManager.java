package xyz.tomsoz.pluginBase.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import xyz.tomsoz.pluginBase.commands.annotations.Argument;
import xyz.tomsoz.pluginBase.commands.annotations.Description;
import xyz.tomsoz.pluginBase.commands.annotations.Permission;
import xyz.tomsoz.pluginBase.commands.annotations.Usage;
import xyz.tomsoz.pluginBase.common.flavor.FlavorOptions;
import xyz.tomsoz.pluginBase.common.flavor.PackageIndexer;
import xyz.tomsoz.pluginBase.extensions.BasePlugin;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.logging.Level;

public class BaseCommandManager {
    private final BasePlugin plugin;
    public final List<BaseCommand> commands = new ArrayList<>();
    private final FlavorOptions options;
    public final PackageIndexer reflections;

    private final Set<String> registeredNames = new HashSet<>();
    private final Set<String> registeredAliases = new HashSet<>();

    public BaseCommandManager(BasePlugin plugin, Class<?> initializer, FlavorOptions options) {
        this.plugin = plugin;
        this.options = options;
        this.reflections = new PackageIndexer(initializer, options);
    }

    /**
     * Creates a new {@link BaseCommandManager} instance using T's {@link Class},
     * and the {@code options}, if any are given.
     */
    public static <T> BaseCommandManager create(BasePlugin plugin, T initializer, FlavorOptions options) {
        return new BaseCommandManager(plugin, initializer.getClass(), options);
    }

    /**
     * Creates a new {@link BaseCommandManager} instance using the specified [initializer] and [options].
     *
     * @param initializer the class used to initialize the manager
     * @param options     the flavor options
     * @return a new {@link BaseCommandManager} instance
     */
    public static BaseCommandManager create(BasePlugin plugin, Class<?> initializer, FlavorOptions options) {
        return new BaseCommandManager(plugin, initializer, options);
    }

    protected void register() {
        List<Method> methods = reflections.getMethodsAnnotatedWith(xyz.tomsoz.pluginBase.commands.annotations.Command.class);

        for (Method method : methods) {
            try {
                CommandObject command = extractCommandDetails(method);
                String commandName = command.getName();
                if (registeredNames.contains(commandName)) {
                    throw new RuntimeException("Cannot register command: Command name '%s' (from method %s) already exists!".formatted(commandName, method.getName()));
                }

                Set<String> newCommandAliasesSet = new HashSet<>(Arrays.asList(command.getAliases()));
                if (newCommandAliasesSet.size() != command.getAliases().length) {
                    throw new RuntimeException("Cannot register command: Command '%s' (from method %s) has duplicate aliases within itself.".formatted(commandName, method.getName()));
                }

                Optional<String> conflictingAliasWithExistingName = Arrays.stream(command.getAliases())
                        .filter(registeredNames::contains)
                        .findFirst();

                if (conflictingAliasWithExistingName.isPresent()) {
                    throw new RuntimeException("Cannot register command: Alias '%s' for command '%s' (from method %s) conflicts with an existing command name."
                            .formatted(conflictingAliasWithExistingName.get(), commandName, method.getName()));
                }

                Optional<String> conflictingAliasWithExistingAlias = Arrays.stream(command.getAliases())
                        .filter(registeredAliases::contains)
                        .findFirst();

                if (conflictingAliasWithExistingAlias.isPresent()) {
                    throw new RuntimeException("Cannot register command: Alias '%s' for command '%s' (from method %s) conflicts with an existing command alias."
                            .formatted(conflictingAliasWithExistingAlias.get(), commandName, method.getName()));
                }

                commands.add(new BaseCommand(plugin, command));
                registeredNames.add(commandName);
                registeredAliases.addAll(Arrays.asList(command.getAliases()));
            } catch (Exception e) {
                options.logger().log(Level.WARNING, "An exception was thrown during command registration", e);
            }
        }

        for (BaseCommand command : commands) {
        }
    }

    private CommandObject extractCommandDetails(Method method) {
        xyz.tomsoz.pluginBase.commands.annotations.Command commandAnnotation = method.getAnnotation(xyz.tomsoz.pluginBase.commands.annotations.Command.class);
        String commandName = commandAnnotation.value();
        String[] commandAliases = commandAnnotation.aliases();

        String commandDescription = "";
        if (method.isAnnotationPresent(Description.class)) {
            Description descriptionAnnotation = method.getAnnotation(Description.class);
            commandDescription = descriptionAnnotation.value();
        }

        String[] commandPermissions = new String[]{};
        PermissionType commandPermissionType = PermissionType.OR;
        if (method.isAnnotationPresent(Permission.class)) {
            Permission permissionAnnotation = method.getAnnotation(Permission.class);
            commandPermissions = permissionAnnotation.value();
            commandPermissionType = permissionAnnotation.type();
        }

        String commandUsage = "";
        if (method.isAnnotationPresent(Usage.class)) {
            Usage usageAnnotation = method.getAnnotation(Usage.class);
            commandUsage = usageAnnotation.value();
        }

        CommandType commandType = CommandType.BOTH;
        Parameter[] parameters = method.getParameters();
        if (parameters.length < 1) {
            throw new RuntimeException("The first argument of all command functions must be of type: Player, CommandSender, ConsoleCommandSender");
        }
        List<CommandArgument> arguments = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (i == 0) {
                Class<?> parameterType = parameter.getType();
                if (Player.class.isAssignableFrom(parameterType)) {
                    commandType = CommandType.PLAYER;
                } else if (CommandSender.class.isAssignableFrom(parameterType)) {
                    commandType = CommandType.BOTH;
                } else if (ConsoleCommandSender.class.isAssignableFrom(parameterType)) {
                    commandType = CommandType.CONSOLE;
                } else {
                    throw new RuntimeException("The first argument of all command functions must be of type: Player, CommandSender, ConsoleCommandSender");
                }
            } else {
                String argumentName;
                String argumentDescription;
                if (parameter.isAnnotationPresent(Argument.class)) {
                    Argument argumentAnnotation = parameter.getAnnotation(Argument.class);
                    argumentName = argumentAnnotation.value();
                    argumentDescription = argumentAnnotation.description();
                } else {
                    argumentName = parameter.getName();
                    argumentDescription = "";
                }
                arguments.add(new CommandArgument(argumentName, argumentDescription));
            }
        }

        return new CommandObject(
                commandName,
                commandAliases,
                commandDescription,
                commandPermissions,
                commandPermissionType,
                commandUsage,
                commandType,
                arguments.toArray(new CommandArgument[0])
        );
    }
}
