package xyz.tomsoz.pluginBase.common.flavor.binder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FlavorBinderMultiType {
    private final FlavorBinderContainer container;
    private final Object instance;

    final List<Class<?>> types = new ArrayList<>();
    Consumer<FlavorBinder<?>> binderInternalPopulator = b -> {
    };

    public FlavorBinderMultiType(FlavorBinderContainer container, Object instance) {
        this.container = container;
        this.instance = instance;
    }

    /**
     * Adds the specified type to the list of types to bind.
     *
     * @param type the type to add
     * @return the current {@link FlavorBinderMultiType} instance
     */
    public <T> FlavorBinderMultiType to(Class<T> type) {
        types.add(type);
        return this;
    }

    /**
     * Sets the internal populator function for the binder.
     *
     * @param populator the populator function
     * @return the current {@link FlavorBinderMultiType} instance
     */
    public FlavorBinderMultiType populate(Consumer<FlavorBinder<?>> populator) {
        binderInternalPopulator = populator;
        return this;
    }

    /**
     * Binds the instance to the specified types.
     */
    public void bind() {
        for (Class<?> type : types) {
            var binder = new FlavorBinder<>(type);
            binderInternalPopulator.accept(binder);
            container.binders.add(binder.to(instance));
        }
    }
}
