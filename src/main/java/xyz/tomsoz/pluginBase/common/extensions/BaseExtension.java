package xyz.tomsoz.pluginBase.common.extensions;

import xyz.tomsoz.pluginBase.common.Version;

/**
 * The base interface for a plugin extension.
 * <p>
 * When using BasePlugin, you can access specific methods
 * from your common module.
 * </p>
 * <p>
 *
 * @author Preva1l
 */
@SuppressWarnings("unused")
public interface BaseExtension {
    /**
     * Get the instance of your extension.
     *
     * @return the instance.
     */
    static BaseExtension instance() {
        return InstanceHolder.modification;
    }

    /**
     * Reloads your plugin, runs any methods annotated with @PluginReload
     */
    void reload();

    /**
     * Get the current version of your plugin.
     *
     * @return the current version.
     */
    Version getCurrentVersion();

    class InstanceHolder {
        public static BaseExtension modification;
    }
}
