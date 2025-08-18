package xyz.tomsoz.pluginBase.flavor.binder.defaults;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;
import xyz.tomsoz.pluginBase.common.Version;
import xyz.tomsoz.pluginBase.common.flavor.annotations.inject.condition.Named;
import xyz.tomsoz.pluginBase.common.flavor.binder.FlavorBinderContainer;
import xyz.tomsoz.pluginBase.common.logging.ServiceLogFormatter;
import xyz.tomsoz.pluginBase.extensions.BasePlugin;

import java.util.logging.Logger;

public class DefaultPluginBinder extends FlavorBinderContainer {
    private final BasePlugin plugin;
    private final Logger serviceLogger;

    public DefaultPluginBinder(BasePlugin plugin) {
        this.plugin = plugin;
        this.serviceLogger = Logger.getLogger(plugin.getName() + "-Services");
        this.serviceLogger.setUseParentHandlers(false);
        this.serviceLogger.addHandler(ServiceLogFormatter.asConsoleHandler(true, plugin.getLogger().getName()));
    }

    @Override
    public void populate() {
        bind(plugin)
                .to(plugin.getClass())
                .to(BasePlugin.class)
                .to(JavaPlugin.class)
                .to(Plugin.class)
                .bind();

        bind(serviceLogger)
                .to(Logger.class)
                .bind();

        bind(Bukkit.getServer())
                .to(Server.class)
                .bind();

        bind(Bukkit.getPluginManager())
                .to(PluginManager.class)
                .bind();

        bind(plugin.getCurrentVersion())
                .to(Version.class)
                .populate(it -> it.annotated(Named.class, a -> a.value().equals("plugin:version")))
                .bind();

        bind(Version.fromString(Bukkit.getBukkitVersion()))
                .to(Version.class)
                .populate(it -> it.annotated(Named.class, a -> a.value().equals("bukkit:version")))
                .bind();

        bind(Bukkit.getScheduler())
                .to(BukkitScheduler.class)
                .bind();

        bind(Bukkit.getMessenger())
                .to(Messenger.class)
                .bind();
    }
}