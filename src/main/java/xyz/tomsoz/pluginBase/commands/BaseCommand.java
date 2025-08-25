package xyz.tomsoz.pluginBase.commands;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.tomsoz.pluginBase.extensions.BasePlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

@Getter
public class BaseCommand extends Command {
    private final BasePlugin plugin;
    private final CommandObject commandObject;

    protected BaseCommand(BasePlugin plugin, CommandObject command) {
        super(command.getName(), command.getDescription(), command.getUsage(), Arrays.stream(command.getAliases()).toList());
        this.plugin = plugin;
        this.commandObject = command;
    }

    public boolean brigader() {
        try {
            Class.forName("io.papermc.paper.command.brigadier.Commands");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public BaseCommand register() {
        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

            commandMap.register(this.plugin.getName(), this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public BaseCommand unregister() {
        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

            this.unregister(commandMap);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
        if (!s.equals(getName())) return false;

        try {
            this.commandObject.getExecute().invoke(commandSender);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        return false;
    }
}
