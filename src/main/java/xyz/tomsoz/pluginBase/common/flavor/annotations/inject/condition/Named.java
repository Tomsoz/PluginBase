package xyz.tomsoz.pluginBase.common.flavor.annotations.inject.condition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({
        ElementType.FIELD,
        ElementType.PARAMETER
})
public @interface Named {
    String value();
}