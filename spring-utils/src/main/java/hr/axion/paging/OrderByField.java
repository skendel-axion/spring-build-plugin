package hr.axion.paging;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OrderByField.OrderByFieldValidator.class)
public @interface OrderByField {

    String[] allowedValues() default {};

    String message() default "List cannot contain empty fields";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String DELIMETER = ":";

    class OrderByFieldValidator implements ConstraintValidator<OrderByField, List<String>> {
        private OrderByField orderByField;

        @Override
        public void initialize(OrderByField orderByField) {
            this.orderByField = orderByField;
        }

        @Override
        public boolean isValid(List<String> objects, ConstraintValidatorContext context) {
            // if property is not set, or it's empty don't validate
            if (objects == null || objects.isEmpty()) {
                return true;
            }
            final List<String> allPossibleValues = new ArrayList<>();
            for (String value : orderByField.allowedValues()) {
                allPossibleValues.add(value);
                allPossibleValues.add(value + DELIMETER + "asc");
                allPossibleValues.add(value + DELIMETER + "desc");
            }
            final boolean isOk = objects.stream().allMatch(x -> allPossibleValues.stream().anyMatch(
                    s -> s.equalsIgnoreCase(x)
            ));
            if (!isOk) {
                final String message = "Allowed fields are %s%s[asc,desc]".formatted(
                        String.join(", ", orderByField.allowedValues()), DELIMETER);
                context.disableDefaultConstraintViolation(); // disable violation message
                context.buildConstraintViolationWithTemplate(message)
                        .addConstraintViolation();
            }
            return isOk;
        }

    }
}
