package xyz.tomsoz.pluginBase.common.flavor;

import org.reflections.Reflections;
import org.reflections.Store;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.QueryFunction;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class PackageIndexer {
    private final Class<?> clazz;
    private final FlavorOptions options;
    public final Reflections reflections;

    public PackageIndexer(Class<?> clazz, FlavorOptions options) {
        this.clazz = clazz;
        this.options = options;
        ConfigurationBuilder config = new ConfigurationBuilder()
                .forPackage(options.mainPackage(), clazz.getClassLoader())
                .setParallel(true)
                .addScanners(
                        Scanners.MethodsAnnotated,
                        Scanners.TypesAnnotated,
                        Scanners.SubTypes
                );
        this.reflections = new Reflections(config);
    }

    /**
     * Returns a list of subtypes of the specified type.
     *
     * @param type the type whose subtypes are to be retrieved
     * @return a list of subtypes of the specified type
     */
    public <T> List<Class<?>> getSubTypes(Class<T> type) {
        return reflections
                .get(subTypes(type))
                .stream()
                .toList();
    }

    /**
     * Gets all methods annotated with the specified annotation and invokes them.
     * <p>
     * Method must either be static or in a singleton ({@code public static final Instance instance})
     * </p>
     *
     * @param annotation the annotation type
     */
    public void invokeMethodsAnnotatedWith(Class<? extends Annotation> annotation) {
        getMethodsAnnotatedWith(annotation)
                .forEach(it -> {
                    try {
                        Object target = Modifier.isStatic(it.getModifiers())
                                ? null
                                : Flavor.objectInstance(it.getDeclaringClass());

                        it.setAccessible(true);
                        it.invoke(target);
                    } catch (Exception e) {
                        options.logger().log(
                                Level.WARNING,
                                String.join(" ",
                                        "Failed to run container part",
                                        it.getClass().getSimpleName(),
                                        "on",
                                        annotation.getSimpleName(),
                                        ":",
                                        it.getName()
                                ), e
                        );
                    }
                });
    }

    /**
     * Returns a list of methods annotated with the specified annotation.
     *
     * @param annotation the annotation type
     * @return a list of methods annotated with the specified annotation
     */
    public <T extends Annotation> List<Method> getMethodsAnnotatedWith(Class<T> annotation) {
        return reflections
                .get(annotated(annotation))
                .stream()
                .toList();
    }

    /**
     * Returns a list of types annotated with the specified annotation.
     *
     * @param annotation the annotation type
     * @return a list of types annotated with the specified annotation
     */
    public <T extends Annotation> List<Class<?>> getTypesAnnotatedWith(Class<T> annotation) {
        List<Class<?>> result = new ArrayList<>();
        for (String className : reflections.get(Scanners.TypesAnnotated.with(annotation))) {
            try {
                result.add(Class.forName(className));
            } catch (ClassNotFoundException ignored) {
            }
        }
        return result;
    }

    /**
     * Returns a query function for methods annotated with the specified annotation.
     *
     * @param annotation the annotation type
     * @return a query function for methods annotated with the specified annotation
     */
    public <T> QueryFunction<Store, Method> annotated(Class<T> annotation) {
        return Scanners.MethodsAnnotated
                .with(annotation)
                .as(Method.class, clazz.getClassLoader());
    }

    /**
     * Returns a query function for subtypes of the specified type.
     *
     * @param <T> the type whose subtypes are to be retrieved
     * @return a query function for subtypes of the specified type
     */
    public <T> QueryFunction<Store, Class<?>> subTypes(Class<T> annotation) {
        return Scanners.SubTypes
                .with(annotation)
                .as();
    }
}
