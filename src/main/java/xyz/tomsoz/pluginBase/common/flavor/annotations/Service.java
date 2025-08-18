package xyz.tomsoz.pluginBase.common.flavor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a flavor Service.
 * <p>
 * If the {@link #name()} is blank, an identifier
 * will be automatically created.
 * </p>
 * <p>
 * Services are sorted by their priority
 * when initialized.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Service {
    String name() default "";

    int priority() default 1;
}