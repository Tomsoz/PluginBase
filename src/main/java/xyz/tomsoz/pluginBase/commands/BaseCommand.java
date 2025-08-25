package xyz.tomsoz.pluginBase.commands;

import lombok.Getter;
import xyz.tomsoz.pluginBase.extensions.BasePlugin;

@Getter
public class BaseCommand {
    private final BasePlugin plugin;
    private final CommandObject commandObject;

    protected BaseCommand(BasePlugin plugin, CommandObject command) {
        this.plugin = plugin;
        this.commandObject = command;
    }

    public void build() {
    }
}
