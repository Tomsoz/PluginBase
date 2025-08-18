package xyz.tomsoz.pluginBase.common.flavor.binder;

import java.util.ArrayList;
import java.util.List;

public abstract class FlavorBinderContainer {
    public final List<FlavorBinder<?>> binders = new ArrayList<>();

    /**
     * Populates the container with flavor binders.
     */
    public abstract void populate();

    /**
     * Binds the specified object to a multi-type flavor binder.
     *
     * @param object the object to bind
     * @return a multi-type flavor binder
     */
    public FlavorBinderMultiType bind(Object object) {
        return new FlavorBinderMultiType(this, object);
    }
}