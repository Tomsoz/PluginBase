package xyz.tomsoz.pluginBase.common.flavor.annotations.inject;

import java.lang.reflect.Field;
import java.util.function.Function;

@SuppressWarnings("unused")
public enum InjectScope {
    SINGLETON(clazz -> {
        Field instanceField;
        try {
            instanceField = clazz.getField("INSTANCE");
        } catch (NoSuchFieldException ignored) {
            try {
                instanceField = clazz.getField("instance");
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return instanceField.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }),
    NO_SCOPE(clazz -> null),
    ;

    public final Function<Class<?>, Object> instanceCreator;

    InjectScope(Function<Class<?>, Object> instanceCreator) {
        this.instanceCreator = instanceCreator;
    }
}