package hr.axion.patch;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DefaultPatchRequestValidator implements ConstraintValidator<ValidatePatchRequest, PatchRequestInterface<?>> {

    @Override
    public boolean isValid(final PatchRequestInterface<?> patchRequest, final ConstraintValidatorContext context) {
        final List<String> inputMaskItems = patchRequest.getFieldMask();
        final List<String> allowedMaskItems = PatchUtils.getPatchFieldsForClass(patchRequest.getData().getClass());

        // remove * for JsonNode object so that sub-object could be allowed
        final List<String> allowedJsonObjects = allowedMaskItems.stream()
                .filter(x -> x.endsWith(".*"))
                .map(x -> x.replaceAll("\\*", ""))
                .toList();

        final List<String> invalidMaskItems = new ArrayList<>();
        inputMaskItems.forEach(inputMask -> {
            // if input mask isn't contained in allow list
            // and if mask doesn't start as JsonNode mask objects
            if(!allowedMaskItems.contains(inputMask)
                && allowedJsonObjects.stream().noneMatch(inputMask::startsWith)
            ) {
                invalidMaskItems.add(inputMask);
            }
        });

        // NOTE this could be better
        if(invalidMaskItems.size() > 0) {
            final String violationMessage = String.format("Invalid masks [%s]. Allowed values: [%s]",
                    String.join(",", invalidMaskItems),
                    String.join(",", allowedMaskItems));
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(violationMessage)
                    .addPropertyNode("fieldMask")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }



}
