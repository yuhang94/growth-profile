package io.growth.platform.profile.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.growth.platform.profile.domain.model.ConditionSlot;
import io.growth.platform.profile.domain.model.SegmentTemplate;
import io.growth.platform.profile.infrastructure.persistence.dataobject.SegmentTemplateDO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SegmentTemplateConverter {

    private final ObjectMapper objectMapper;

    public SegmentTemplateConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SegmentTemplateDO toDataObject(SegmentTemplate domain) {
        SegmentTemplateDO dataObject = new SegmentTemplateDO();
        dataObject.setId(domain.getId());
        dataObject.setTemplateKey(domain.getTemplateKey());
        dataObject.setTitle(domain.getTitle());
        dataObject.setDescription(domain.getDescription() != null ? domain.getDescription() : "");
        dataObject.setSortOrder(domain.getSortOrder());
        dataObject.setBuiltIn(domain.isBuiltIn());
        dataObject.setSlots(serializeSlots(domain.getSlots()));
        return dataObject;
    }

    public SegmentTemplate toDomain(SegmentTemplateDO dataObject) {
        SegmentTemplate domain = new SegmentTemplate();
        domain.setId(dataObject.getId());
        domain.setTemplateKey(dataObject.getTemplateKey());
        domain.setTitle(dataObject.getTitle());
        domain.setDescription(dataObject.getDescription());
        domain.setSortOrder(dataObject.getSortOrder());
        domain.setBuiltIn(dataObject.isBuiltIn());
        domain.setSlots(deserializeSlots(dataObject.getSlots()));
        domain.setCreatedTime(dataObject.getCreatedTime());
        domain.setUpdatedTime(dataObject.getUpdatedTime());
        return domain;
    }

    private String serializeSlots(List<ConditionSlot> slots) {
        try {
            return objectMapper.writeValueAsString(slots);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize slots", e);
        }
    }

    private List<ConditionSlot> deserializeSlots(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<ConditionSlot>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize slots: " + json, e);
        }
    }
}
