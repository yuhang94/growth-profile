package io.growth.platform.profile.domain.service;

import io.growth.platform.common.core.exception.BizException;
import io.growth.platform.profile.domain.model.PropertyDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EventPropertyValidatorTest {

    private EventPropertyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EventPropertyValidator();
    }

    @Test
    void validate_nullDefinitions_passes() {
        // Should not throw
        validator.validate(Map.of("key", "value"), null);
    }

    @Test
    void validate_emptyDefinitions_passes() {
        validator.validate(Map.of("key", "value"), List.of());
    }

    @Test
    void validate_requiredFieldPresent_passes() {
        PropertyDefinition def = newPropDef("userId", "STRING", true);
        validator.validate(Map.of("userId", "user001"), List.of(def));
    }

    @Test
    void validate_requiredFieldMissing_throwsException() {
        PropertyDefinition def = newPropDef("userId", "STRING", true);

        BizException ex = assertThrows(BizException.class,
                () -> validator.validate(Map.of(), List.of(def)));
        assertTrue(ex.getMessage().contains("必填属性缺失: userId"));
    }

    @Test
    void validate_requiredFieldBlank_throwsException() {
        PropertyDefinition def = newPropDef("userId", "STRING", true);

        BizException ex = assertThrows(BizException.class,
                () -> validator.validate(Map.of("userId", ""), List.of(def)));
        assertTrue(ex.getMessage().contains("必填属性缺失: userId"));
    }

    @Test
    void validate_optionalFieldMissing_passes() {
        PropertyDefinition def = newPropDef("remark", "STRING", false);
        validator.validate(Map.of(), List.of(def));
    }

    @Test
    void validate_longType_valid() {
        PropertyDefinition def = newPropDef("amount", "LONG", false);
        validator.validate(Map.of("amount", "12345"), List.of(def));
    }

    @Test
    void validate_longType_invalid() {
        PropertyDefinition def = newPropDef("amount", "LONG", false);

        BizException ex = assertThrows(BizException.class,
                () -> validator.validate(Map.of("amount", "abc"), List.of(def)));
        assertTrue(ex.getMessage().contains("类型不匹配"));
    }

    @Test
    void validate_doubleType_valid() {
        PropertyDefinition def = newPropDef("price", "DOUBLE", false);
        validator.validate(Map.of("price", "99.9"), List.of(def));
    }

    @Test
    void validate_doubleType_invalid() {
        PropertyDefinition def = newPropDef("price", "DOUBLE", false);

        BizException ex = assertThrows(BizException.class,
                () -> validator.validate(Map.of("price", "not-a-number"), List.of(def)));
        assertTrue(ex.getMessage().contains("类型不匹配"));
    }

    @Test
    void validate_booleanType_valid() {
        PropertyDefinition def = newPropDef("active", "BOOLEAN", false);
        validator.validate(Map.of("active", "true"), List.of(def));
        validator.validate(Map.of("active", "False"), List.of(def));
    }

    @Test
    void validate_booleanType_invalid() {
        PropertyDefinition def = newPropDef("active", "BOOLEAN", false);

        BizException ex = assertThrows(BizException.class,
                () -> validator.validate(Map.of("active", "yes"), List.of(def)));
        assertTrue(ex.getMessage().contains("类型不匹配"));
    }

    @Test
    void validate_multipleErrors_joinedInMessage() {
        PropertyDefinition def1 = newPropDef("userId", "STRING", true);
        PropertyDefinition def2 = newPropDef("amount", "LONG", true);

        BizException ex = assertThrows(BizException.class,
                () -> validator.validate(Map.of(), List.of(def1, def2)));
        assertTrue(ex.getMessage().contains("userId"));
        assertTrue(ex.getMessage().contains("amount"));
    }

    @Test
    void validate_undefinedProperty_ignored() {
        PropertyDefinition def = newPropDef("userId", "STRING", true);
        // Extra property "extra" not in definitions — should be ignored
        validator.validate(Map.of("userId", "user001", "extra", "ignored"), List.of(def));
    }

    private PropertyDefinition newPropDef(String name, String type, boolean required) {
        PropertyDefinition def = new PropertyDefinition();
        def.setPropertyName(name);
        def.setPropertyType(type);
        def.setDisplayName(name);
        def.setRequired(required);
        return def;
    }
}
