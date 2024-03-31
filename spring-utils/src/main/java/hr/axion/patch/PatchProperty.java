package hr.axion.patch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PatchProperty {
    /**
     * Used for input objects
     * - empty string - use target value,
     * - null - don't map the field
     * - set value - use field
     */
    String mapToField() default "";

    /**
     * If field should be copied as primitive Java types (e.g. JsonNode)
     */
    boolean forceAsPrimitive() default false;

}