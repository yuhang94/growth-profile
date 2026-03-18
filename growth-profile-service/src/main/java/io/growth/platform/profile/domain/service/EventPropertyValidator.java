package io.growth.platform.profile.domain.service;

import io.growth.platform.common.core.exception.BizException;
import io.growth.platform.common.core.exception.CommonErrorCode;
import io.growth.platform.profile.domain.model.PropertyDefinition;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class EventPropertyValidator {

    /**
     * Validate properties against definitions.
     * Checks required fields and type matching.
     */
    public void validate(Map<String, String> properties, List<PropertyDefinition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            return;
        }

        Map<String, String> props = properties != null ? properties : Map.of();
        List<String> errors = new ArrayList<>();

        for (PropertyDefinition def : definitions) {
            String value = props.get(def.getPropertyName());

            // Required check
            if (def.isRequired() && (value == null || value.isBlank())) {
                errors.add("必填属性缺失: " + def.getPropertyName());
                continue;
            }

            // Type check (skip if value is absent)
            if (value != null && !value.isBlank()) {
                String typeError = validateType(value, def.getPropertyType(), def.getPropertyName());
                if (typeError != null) {
                    errors.add(typeError);
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, String.join("; ", errors));
        }
    }

    private String validateType(String value, String propertyType, String propertyName) {
        if (propertyType == null) {
            return null;
        }
        return switch (propertyType.toUpperCase()) {
            case "STRING" -> null; // any string is valid
            case "LONG" -> {
                try {
                    Long.parseLong(value);
                    yield null;
                } catch (NumberFormatException e) {
                    yield "属性 " + propertyName + " 类型不匹配，期望 LONG，实际值: " + value;
                }
            }
            case "DOUBLE" -> {
                try {
                    Double.parseDouble(value);
                    yield null;
                } catch (NumberFormatException e) {
                    yield "属性 " + propertyName + " 类型不匹配，期望 DOUBLE，实际值: " + value;
                }
            }
            case "BOOLEAN" -> {
                if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                    yield null;
                }
                yield "属性 " + propertyName + " 类型不匹配，期望 BOOLEAN，实际值: " + value;
            }
            case "DATETIME_STRING", "EPOCH_SECOND", "EPOCH_MILLIS" -> {
                try {
                    DateTimeParser.parse(value, propertyType.toUpperCase());
                    yield null;
                } catch (Exception e) {
                    yield "属性 " + propertyName + " 类型不匹配，期望 " + propertyType + "，实际值: " + value;
                }
            }
            default -> null; // unknown type, skip validation
        };
    }
}
