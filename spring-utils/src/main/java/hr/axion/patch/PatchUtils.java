package hr.axion.patch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Missing @JsonProperty check of JSON names
 */
public class PatchUtils {

    static Logger logger = LoggerFactory.getLogger(PatchUtils.class);

    private static boolean isFieldNonCustomType(final Field check) {
        final PatchProperty property = check.getAnnotation(PatchProperty.class);
        if (property != null && property.forceAsPrimitive()) {
            return true;
        }
        return check.getType().isPrimitive() || check.getType().getName().startsWith("java");
    }

    /**
     * Iterate over class and it's sub-property classes to retrieve all possible field mask values
     *
     * @return [ field1, field2, field3.sub_field1, field3.sub_field2, .. ]
     */
    public static List<String> getPatchFieldsForClass(final Class<?> objectClass) {
        return getPatchFieldsForClass(objectClass, "");
    }

    private static List<String> getPatchFieldsForClass(final Class<?> objectClass, String prefix) {
        final List<String> fieldNames = new ArrayList<>();
        FieldUtils.getFieldsListWithAnnotation(objectClass, PatchProperty.class)
                .forEach(patchField -> {
                    if (isFieldNonCustomType(patchField)) {
                        fieldNames.add(patchField.getName());
                    } else if (patchField.getType().equals(JsonNode.class)) {
                        fieldNames.add(patchField.getName() + ".*");
                    } else {
                        final List<String> subItems = getPatchFieldsForClass(patchField.getType(),
                                prefix + patchField.getName() + ".");
                        if (!subItems.isEmpty()) {
                            fieldNames.addAll(subItems);
                        }
                    }
                });
        // apply prefix
        return fieldNames.stream().map(it -> prefix + it).toList();
    }

    public static <T> T applyPatchValuesToTarget(final PatchRequestInterface<?> patchRequest, final T target) {
        return applyPatchValuesToTarget(patchRequest.getFieldMask(), patchRequest.getData(), target);
    }

    public static <T> T applyPatchValuesToTarget(final List<String> fieldMasks, final Object input,
                                                 final T target) {
        return applyPatchValuesToTarget(fieldMasks, input, target, "");
    }


    private static <T> T applyPatchValuesToTarget(final List<String> fieldMasks, final Object input,
                                                  final T target, final String prefix) {
        final List<Field> patchableFields = FieldUtils
                .getFieldsListWithAnnotation(input.getClass(), PatchProperty.class);
        patchableFields.forEach(patchField -> {
            // process normal attributes
            if (isFieldNonCustomType(patchField)) {
                final PatchProperty patchProperty = patchField.getAnnotation(PatchProperty.class);
                // ignore null values and fields outside fieldMask
                final String fieldWithPrefix = prefix + patchField.getName();
                if (patchProperty.mapToField() != null && fieldMasks.contains(fieldWithPrefix)) {
                    try {
                        // if annotation value is empty, use input object field name
                        final String targetFieldName = patchProperty.mapToField().isEmpty() ?
                                patchField.getName() : patchProperty.mapToField();
                        // copy input value (including null) to target object
                        final PropertyDescriptor pdInput = new PropertyDescriptor(patchField.getName(), input.getClass());
                        final PropertyDescriptor pdTarget = new PropertyDescriptor(targetFieldName, target.getClass());
                        pdTarget.getWriteMethod().invoke(target, pdInput.getReadMethod().invoke(input));
                    } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
                        // don't throw, but write to console
                        logger.error(String.format("Error while processing patch field [%s]", fieldWithPrefix), e);
                    }
                }

            } else if (patchField.getType().equals(JsonNode.class)) {
                final PatchProperty patchProperty = patchField.getAnnotation(PatchProperty.class);
                final String fieldWithPrefix = prefix + patchField.getName();

                // [ extraInfo, extraInfo.field1, extraInfo.field2 ] - if extraInfo is here
                // get all sub node mask elements (with ".")
                final List<String> subNodes = fieldMasks.stream()
                        .filter(filter -> filter.startsWith(fieldWithPrefix + "."))
                        .toList();
                try {
                    // if annotation value is empty, use input object field name
                    final String targetFieldName = patchProperty.mapToField().isEmpty() ?
                            patchField.getName() : patchProperty.mapToField();
                    // copy input value (including null) to target object
                    final PropertyDescriptor pdInput = new PropertyDescriptor(patchField.getName(), input.getClass());
                    final PropertyDescriptor pdTarget = new PropertyDescriptor(targetFieldName, target.getClass());

                    final JsonNode updateNode = (JsonNode) pdInput.getReadMethod().invoke(input);
                    JsonNode mainNode = (JsonNode) pdTarget.getReadMethod().invoke(target);
                    // update node needs to exists to write
                    if (updateNode != null) {
                        // if main node doesn't exists, just apply update node
                        if (mainNode == null) {
                            mainNode = JsonNodeFactory.instance.objectNode();
                        }
                        clearUpdateNodeFromNonMaskFields(updateNode, subNodes, fieldWithPrefix + ".");
                        clearOriginalNodeByRemovingFieldMaskItems(mainNode, subNodes, fieldWithPrefix + ".");
                        mergeJsonNodes(mainNode, updateNode);
                        pdTarget.getWriteMethod().invoke(target, mainNode);
                    }

                } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
                    // don't throw, but write to console
                    logger.error(String.format("Error while processing patch field [%s]", fieldWithPrefix), e);
                }

            } else {
                // process child elements
                try {
                    final PropertyDescriptor pdInput = new PropertyDescriptor(patchField.getName(), input.getClass());
                    final Object childInput = pdInput.getReadMethod().invoke(input);
                    if (childInput != null) {
                        applyPatchValuesToTarget(fieldMasks, childInput, target, prefix + patchField.getName() + ".");
                    }
                } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
                    // don't throw, but write to console
                    logger.error(String.format("Error while processing patch field [%s]", patchField.getName()), e);
                }
            }
        });
        return target;
    }


    public static void clearOriginalNodeByRemovingFieldMaskItems(final JsonNode node, final List<String> fieldMasks, final String prefix) {

        final List<String> fieldMasksWithoutPrefix = fieldMasks.stream()
                .map(x -> StringUtils.replaceOnce(x, prefix, ""))
                .toList();
        final List<String> currentLevelFields = fieldMasksWithoutPrefix.stream()
                .filter(x -> !x.contains("."))
                .toList();
        final List<String> nextLevelFields = fieldMasksWithoutPrefix.stream()
                .filter(x -> x.contains("."))
                .map(x -> Arrays.stream(x.split("\\.")).findFirst().get())
                .toList();

        for (String field : currentLevelFields) {
            if (node instanceof ObjectNode && node.has(field)) {
                ((ObjectNode) node).remove(field);
            }
        }
        for (String field : nextLevelFields) {
            final JsonNode value = node.get(field);
            if (value != null && value.isObject()) {
                clearOriginalNodeByRemovingFieldMaskItems(value, fieldMasks, prefix + field + ".");
            }
        }
    }


    public static void clearUpdateNodeFromNonMaskFields(final JsonNode node, final List<String> fieldMasks, final String prefix) {

        final List<String> fieldMasksWithoutPrefix = fieldMasks.stream()
                .map(x -> StringUtils.replaceOnce(x, prefix, ""))
                .toList();
        final List<String> currentLevelFields = fieldMasksWithoutPrefix.stream()
                .filter(x -> !x.contains("."))
                .toList();

        List<String> fieldNames = new ArrayList<>();
        node.fieldNames().forEachRemaining(fieldNames::add);

        for (String fieldName : fieldNames) {
            final JsonNode field = node.get(fieldName);
            if(field.isObject()) {
                clearUpdateNodeFromNonMaskFields(field, fieldMasks, prefix + fieldName + ".");
            } else {
                if(!currentLevelFields.contains(fieldName)) {
                    if (node instanceof ObjectNode) {
                        ((ObjectNode) node).remove(fieldName);
                    }
                }
            }
        }
    }

    public static void mergeJsonNodes(final JsonNode originalNode, final JsonNode updateNode) {
        final Iterator<String> fieldNames = updateNode.fieldNames();
        while (fieldNames.hasNext()) {
            final String fieldName = fieldNames.next();
            final JsonNode jsonNode = originalNode.get(fieldName);
            // if field exists and is an embedded object
            if (jsonNode != null && jsonNode.isObject()) {
                mergeJsonNodes(jsonNode, updateNode.get(fieldName));
            } else {
                if (originalNode instanceof ObjectNode) {
                    // Overwrite field
                    final JsonNode value = updateNode.get(fieldName);
                    ((ObjectNode) originalNode).set(fieldName, value);
                }
            }
        }
    }



}
