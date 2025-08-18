package xyz.tomsoz.pluginBase.common.flavor;

import javax.annotation.CheckForNull;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public record FlavorOptions(Logger logger, String mainPackage) {
    public FlavorOptions() {
        this(Logger.getAnonymousLogger());
    }

    public FlavorOptions(Logger logger) {
        this(logger, null);
    }

    @Override
    public @CheckForNull String mainPackage() {
        return mainPackage;
    }
}
