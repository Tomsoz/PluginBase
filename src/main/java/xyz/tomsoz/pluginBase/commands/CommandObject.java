package xyz.tomsoz.pluginBase.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
}
