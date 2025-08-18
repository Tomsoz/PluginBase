package xyz.tomsoz.pluginBase.common.flavor;

import org.jetbrains.annotations.Nullable;
import xyz.tomsoz.pluginBase.common.flavor.annotations.Close;
import xyz.tomsoz.pluginBase.common.flavor.annotations.Configure;
import xyz.tomsoz.pluginBase.common.flavor.annotations.IgnoreAutoScan;
import xyz.tomsoz.pluginBase.common.flavor.annotations.Service;
import xyz.tomsoz.pluginBase.common.flavor.annotations.inject.Inject;
import xyz.tomsoz.pluginBase.common.flavor.binder.FlavorBinder;
import xyz.tomsoz.pluginBase.common.flavor.binder.FlavorBinderContainer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Flavor is a light-weight kotlin IoC container and lifecycle management framework developed by GrowlyX (Subham)
 * This is a modified version to work in Java as-well with extension methods for kotlin users elsewhere.
 * (taken from <a href="https://github.com/Finally-A-Decent/Trashcan">Finally-A-Decent/Trashcan</a>) (Preva1l)
 */
@SuppressWarnings("unused")
public class Flavor {
    private final FlavorOptions options;

    public final PackageIndexer reflections;

    public final List<FlavorBinder<?>> binders = new ArrayList<>();
    public final Map<Class<?>, Object> services = new HashMap<>();

    private Flavor(Class<?> initializer, FlavorOptions options) {
        this.options = options;

        this.reflections = new PackageIndexer(initializer, options);
    }

    /**
     * Creates a new {@link Flavor} instance using T's {@link Class},
     * and the {@code options}, if any are given.
     */
    public static <T> Flavor create(T initializer, FlavorOptions options) {
        return new Flavor(initializer.getClass(), options);
    }

    /**
     * Creates a new {@link Flavor} instance using the specified [initializer] and [options].
     *
     * @param initializer the class used to initialize the flavor
     * @param options     the flavor options
     * @return a new {@link Flavor} instance
     */
    public static Flavor create(Class<?> initializer, FlavorOptions options) {
        return new Flavor(initializer, options);
    }

    /**
     * Inherit an arbitrary {@link FlavorBinderContainer}
     * and populate our binders with its ones.
     */
    public Flavor inherit(FlavorBinderContainer container) {
        container.populate();
        binders.addAll(container.binders);
        return this;
    }

    /**
     * Searches for and returns a
     * service matching type T.
     *
     * @return the service
     * @throws RuntimeException if there is
     *                          no service matching type T.
     */
    public <T> T service(Class<T> clazz) {
        var service = services.get(clazz);
        if (service == null) throw new IllegalArgumentException("A non-service class was provided.");
        return (T) service;
    }

    /**
     * Creates a new [FlavorBinder] for type [T].
     */
    public <T> FlavorBinder<T> bind(Class<T> clazz) {
        var binder = new FlavorBinder<>(clazz);
        binders.add(binder);
        return binder;
    }

    /**
     * Creates and inject a new instance of the clazz;
     *
     * @return the injected instance of clazz
     */
    public <T> T injected(Class<T> clazz, Object... params) {
        T instance;
        try {
            if (params.length == 0) {
                instance = clazz.getDeclaredConstructor().newInstance();
            } else {
                instance = clazz.getDeclaredConstructor(
                                Arrays.stream(params)
                                        .map(Object::getClass)
                                        .toArray(Class[]::new)
                        )
                        .newInstance(Arrays.stream(params).toArray());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        inject(instance);
        return instance;
    }

    /**
     * Injects fields into a pre-existing class, [any].
     */
    public void inject(Object object) {
        scanAndInject(object.getClass(), object);
    }

    /**
     * Scans and injects any services and/or singletons (kt objects or any class with a static final {@code instance} field)
     * that contain fields annotated with {@link Inject}.
     */
    public void startup() {
        List<Class<?>> classes = reflections
                .getTypesAnnotatedWith(Service.class)
                .stream()
                .sorted(Comparator.comparingInt((Class<?> clazz) -> {
                    Service annotation = clazz.getAnnotation(Service.class);
                    return annotation != null ? annotation.priority() : 1;
                }).reversed())
                .toList();

        for (Class<?> clazz : classes) {
            IgnoreAutoScan ignoreAutoScan = clazz.getAnnotation(IgnoreAutoScan.class);

            if (ignoreAutoScan == null) {
                try {
                    scanAndInject(clazz, objectInstance(clazz));
                } catch (Exception e) {
                    options.logger().log(Level.WARNING, "An exception was thrown during injection", e);
                }
            }
        }
    }

    /**
     * Invokes the {@link Close} method in all registered services. If a
     * service does not have a close method, the service will be skipped.
     */
    public void close() {
        for (Map.Entry<Class<?>, Object> entry : services.entrySet()) {
            Optional<Method> close = Arrays.stream(entry.getKey().getDeclaredMethods())
                    .filter(it -> it.isAnnotationPresent(Close.class))
                    .findFirst();

            Service service = entry.getKey().getDeclaredAnnotation(Service.class);

            long milli = tracked(() -> close.ifPresent(it -> {
                try {
                    it.invoke(entry.getValue());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    options.logger().log(Level.SEVERE, "An exception was thrown while closing service - {}", e);
                }
            }));

            if (milli != -1L) {
                options.logger().info(
                        "[Services] [%s] Shutdown in %sms.".formatted(
                                !service.name().isEmpty() ? service.name() : entry.getKey().getSimpleName(),
                                milli
                        )
                );
            } else {
                options.logger().info(
                        "[Services] [%s] Failed to shutdown!".formatted(
                                !service.name().isEmpty() ? service.name() : entry.getKey().getSimpleName()
                        )
                );
            }
        }
    }

    /**
     * Invokes the provided {@code lambda } while keeping track of
     * the amount of time it took to run in milliseconds.
     * <p>
     * Any exception thrown within the lambda will be printed,
     * and {@code -1} will be returned.
     */
    private long tracked(Runnable lambda) {
        long start = System.currentTimeMillis();

        try {
            lambda.run();
        } catch (Exception exception) {
            options.logger().log(Level.SEVERE, "Failed to invoke lambda", exception);
            return -1;
        }

        return System.currentTimeMillis() - start;
    }

    /**
     * Scans & injects a provided {@link Class}, along with its
     * singleton instance if there is one.
     */
    private void scanAndInject(Class<?> clazz, @Nullable Object instance) {
        Object singleton = instance != null ? instance : objectInstance(clazz);

        for (Field field : clazz.getDeclaredFields()) {
            // making sure this field is annotated with
            // Inject before modifying its value.
            if (field.isAnnotationPresent(Inject.class)) {
                // trying to find [FlavorBinder]s
                // of the field's type
                List<FlavorBinder<?>> bindersOfType = binders
                        .stream()
                        .filter(it -> it.getClazz().isAssignableFrom(field.getType()))
                        .collect(Collectors.toList());

                for (FlavorBinder<?> flavorBinder : bindersOfType) {
                    for (Annotation annotation : field.getDeclaredAnnotations()) {
                        // making sure if there are any annotation
                        // checks, that the field passes the check
                        Predicate<Annotation> predicate = flavorBinder.getAnnotationCheck(annotation.getClass());
                        boolean passesCheck = predicate == null || predicate.test(annotation);

                        if (!passesCheck) {
                            bindersOfType.remove(flavorBinder);
                        }
                    }
                }

                // retrieving the first binder of the field's type
                FlavorBinder<?> binder = bindersOfType.getFirst();
                boolean accessibility = field.canAccess(singleton);

                if (singleton == null) continue;

                try {
                    field.setAccessible(true);
                    field.set(singleton, binder.instance);
                    field.setAccessible(accessibility);
                } catch (IllegalAccessException e) {
                    options.logger().log(Level.SEVERE, "An exception was thrown while injecting field - {}", e);
                }
            }
        }

        // checking if this class is a service
        boolean isServiceClazz = clazz.isAnnotationPresent(Service.class);

        if (!isServiceClazz) return;
        Optional<Method> configure = Arrays.stream(clazz.getDeclaredMethods())
                .filter(it -> it.isAnnotationPresent(Configure.class))
                .findFirst();

        // singletons should always be non-null
        services.put(clazz, singleton);

        Service service = clazz.getDeclaredAnnotation(Service.class);
        String serviceName = !service.name().isEmpty() ? service.name() : clazz.getSimpleName();

        long milli = tracked(() -> configure.ifPresent(it -> {
            try {
                it.invoke(singleton);
            } catch (IllegalAccessException | InvocationTargetException e) {
                options.logger().log(
                        Level.SEVERE,
                        "An exception was thrown while configuring service - " + serviceName,
                        e
                );
            }
        }));

        // making sure an exception wasn't thrown
        // while trying to configure the service
        if (milli != -1L) {
            options.logger().info("[Services] [%s] Loaded in %sms.".formatted(serviceName, milli));
        } else {
            options.logger().info("[Services] [%s] Failed to load!".formatted(serviceName));
        }
    }


    /**
     * Returns the singleton instance of the class, if any.
     *
     * @return the singleton instance, or null if there is none
     */
    public static Object objectInstance(Class<?> clazz) {
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
    }
}
