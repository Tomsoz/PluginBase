package xyz.tomsoz.pluginBase.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;

@AllArgsConstructor
@Getter
public class CommandObject {
    private String name;
    private String[] aliases;
    private String description;
    private String[] permissions;
    private PermissionType permissionType;
    private String usage;
    private CommandType type;
    private CommandArgument[] arguments;
    private Method execute;
}
