package xyz.tomsoz.pluginBase;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.tomsoz.pluginBase.commands.annotations.Argument;
import xyz.tomsoz.pluginBase.commands.annotations.Command;
import xyz.tomsoz.pluginBase.commands.annotations.Description;
import xyz.tomsoz.pluginBase.commands.annotations.Permission;

public class ExampleCommand {
    @Command("skin")
    @Description("Changes your skin")
    @Permission({"advancednicks.skin", "ff"})
    public void command(CommandSender sender, @Argument Player target) {

    }
}
