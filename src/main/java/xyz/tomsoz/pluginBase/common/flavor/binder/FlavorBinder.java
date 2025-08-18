package xyz.tomsoz.pluginBase.common.flavor.binder;

import xyz.tomsoz.pluginBase.common.flavor.annotations.inject.InjectScope;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A wrapper class to easily
 * create and register a flavor binder.
 */
@SuppressWarnings({"UNCHECKED_CAST", "unused"})
public class FlavorBinder<T> {
    private final Class<T> clazz;
    private final Map<Class<? extends Annotation>, Predicate<? extends Annotation>> annotationChecks = new HashMap<>();

    public Object instance;
    public InjectScope scope = InjectScope.NO_SCOPE;

    public FlavorBinder(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * Sets the injection scope for the binder.
     *
     * @param scope The injection scope to set.
     * @return The current {@link FlavorBinder} instance.
     */
    public FlavorBinder<T> scoped(InjectScope scope) {
        this.scope = scope;
        return this;
    }

    /**
     * Convert an instance to a {@link FlavorBinder}.
     */
    public FlavorBinder<T> to(Object object) {
        instance = object;
        return this;
    }

    /**
     * Check an annotation lambda and convert into {@link FlavorBinder}.
     */
    public <A extends Annotation> FlavorBinder<T> annotated(Class<A> annotation, Predicate<A> lambda) {
        annotationChecks.put(annotation, lambda);
        return this;
    }

    public Predicate<Annotation> getAnnotationCheck(Class<?> annotation) {
        return ((Predicate<Annotation>) annotationChecks.get(annotation));
    }

    public Class<?> getClazz() {
        return clazz;
    }
}
