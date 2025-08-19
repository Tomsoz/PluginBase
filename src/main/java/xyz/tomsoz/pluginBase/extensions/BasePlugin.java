package xyz.tomsoz.pluginBase.extensions;

import info.preva1l.hooker.Hooker;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.tomsoz.pluginBase.annotations.PluginDisable;
import xyz.tomsoz.pluginBase.annotations.PluginEnable;
import xyz.tomsoz.pluginBase.annotations.PluginLoad;
import xyz.tomsoz.pluginBase.common.Version;
import xyz.tomsoz.pluginBase.common.extensions.BaseExtension;
import xyz.tomsoz.pluginBase.common.extensions.annotations.ExtensionReload;
import xyz.tomsoz.pluginBase.common.flavor.Flavor;
import xyz.tomsoz.pluginBase.common.flavor.FlavorOptions;
import xyz.tomsoz.pluginBase.common.flavor.PackageIndexer;
import xyz.tomsoz.pluginBase.flavor.binder.defaults.DefaultPluginBinder;

public class BasePlugin extends JavaPlugin implements BaseExtension {
    protected final Version currentVersion = Version.fromString(getDescription().getVersion().isEmpty() ? "1.0.0" : getDescription().getVersion());

    protected Flavor flavor;
    protected PackageIndexer packageIndexer;

    @Override
    public final void onLoad() {
        // Resolve all plugin annotations here, preventing class not found exceptions on shutdown
        try {
            Class.forName("xyz.tomsoz.pluginBase.common.extensions.annotations.PluginLoad");
            Class.forName("xyz.tomsoz.pluginBase.common.extensions.annotations.PluginEnable");
            Class.forName("xyz.tomsoz.pluginBase.common.extensions.annotations.PluginDisable");
            Class.forName("xyz.tomsoz.pluginBase.common.extensions.annotations.ExtensionReload");
            Class.forName("info.preva1l.hooker.annotations.OnStop");
        } catch (ClassNotFoundException ignored) {
        }

        this.flavor = Flavor.create(
                this.getClass(),
                new FlavorOptions(
                        this.getLogger(),
                        this.getClass().getPackageName()
                )
        );

        this.packageIndexer = flavor.reflections;
        this.packageIndexer.invokeMethodsAnnotatedWith(PluginLoad.class);
        this.flavor.inherit(new DefaultPluginBinder(this));

        try {
            var hooker = Hooker.class.getDeclaredField("instance");
            hooker.setAccessible(true);
            if (hooker.get(null) == null) {
                Hooker.register(this, "xyz.tomsoz");
            }
        } catch (Exception ignored) {
        }

        Hooker.load();
        InstanceHolder.modification = this;
    }

    @Override
    public final void onEnable() {
        flavor.startup();

        Hooker.enable();

        this.packageIndexer.invokeMethodsAnnotatedWith(PluginEnable.class);
    }

    @Override
    public final void onDisable() {
        Hooker.disable();

        this.packageIndexer.invokeMethodsAnnotatedWith(PluginDisable.class);

        flavor.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void reload() {
        this.packageIndexer.invokeMethodsAnnotatedWith(ExtensionReload.class);
        Hooker.reload();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Version getCurrentVersion() {
        return currentVersion;
    }

    protected final void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }
}
