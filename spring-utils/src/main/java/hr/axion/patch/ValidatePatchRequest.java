package hr.axion.patch;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Version: 1 (01.07.2022.)
 * Validator annotation<br/>
 * Used on @RequestBody parameters implementing {@link PatchRequest}.<br/>
 * <br/>
 * Validation checks all the strings in the {@link PatchRequest#getFieldMask()} list
 * are present as fields in the object being patched. <br/>
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DefaultPatchRequestValidator.class)
public @interface ValidatePatchRequest {
    String message() default "Invalid field in the field mask.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}