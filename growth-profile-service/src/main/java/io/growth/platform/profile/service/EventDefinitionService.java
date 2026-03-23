package io.growth.platform.profile.service;

import io.growth.platform.common.core.exception.BizException;
import io.growth.platform.common.core.exception.CommonErrorCode;
import io.growth.platform.common.core.result.PageResult;
import io.growth.platform.profile.api.dto.EventDefinitionCreateRequest;
import io.growth.platform.profile.api.dto.EventDefinitionDTO;
import io.growth.platform.profile.api.dto.EventDefinitionUpdateRequest;
import io.growth.platform.profile.api.dto.FieldMapping;
import io.growth.platform.profile.api.dto.MqMappingTestRequest;
import io.growth.platform.profile.api.dto.MqMappingTestResult;
import io.growth.platform.profile.api.dto.MqSourceConfigDTO;
import io.growth.platform.profile.api.dto.PropertyDefinitionDTO;
import io.growth.platform.profile.api.enums.SourceType;
import io.growth.platform.profile.converter.EventDefinitionDTOConverter;
import io.growth.platform.profile.domain.model.BehaviorEventDefinition;
import io.growth.platform.profile.domain.repository.EventDefinitionRepository;
import io.growth.platform.profile.infrastructure.mq.DynamicMqConsumerManager;
import io.growth.platform.profile.infrastructure.mq.EventMessageParser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventDefinitionService {
    private final EventDefinitionRepository eventDefinitionRepository;
    private final DynamicMqConsumerManager dynamicMqConsumerManager;
    private final EventMessageParser eventMessageParser;
    private final EventDefinitionDTOConverter converter = EventDefinitionDTOConverter.INSTANCE;

    public EventDefinitionService(EventDefinitionRepository eventDefinitionRepository,
                                  DynamicMqConsumerManager dynamicMqConsumerManager,
                                  EventMessageParser eventMessageParser) {
        this.eventDefinitionRepository = eventDefinitionRepository;
        this.dynamicMqConsumerManager = dynamicMqConsumerManager;
        this.eventMessageParser = eventMessageParser;
    }

    public EventDefinitionDTO create(EventDefinitionCreateRequest request) {
        if (eventDefinitionRepository.existsByEventName(request.getEventName())) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "事件名称已存在: " + request.getEventName());
        }

        // Default sourceType to SDK
        if (request.getSourceType() == null) {
            request.setSourceType(SourceType.SDK);
        }

        // Validate MQ config if sourceType is MQ
        if (request.getSourceType() == SourceType.MQ) {
            validateMqSourceConfig(request.getMqSourceConfig());
            request.setProperties(mergeMqFieldMappingsIntoProperties(request.getProperties(), request.getMqSourceConfig()));
        }

        BehaviorEventDefinition domain = converter.toDomain(request);
        domain.setStatus(1);
        eventDefinitionRepository.insert(domain);

        // Register MQ consumer if MQ type
        if (domain.getSourceType() == SourceType.MQ && domain.getMqSourceConfig() != null) {
            dynamicMqConsumerManager.register(domain.getEventName(), domain.getMqSourceConfig());
        }

        return converter.toDTO(domain);
    }

    public EventDefinitionDTO update(String eventName, EventDefinitionUpdateRequest request) {
        BehaviorEventDefinition domain = eventDefinitionRepository.findByEventName(eventName)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "事件定义不存在: " + eventName));

        boolean wasMq = domain.getSourceType() == SourceType.MQ;

        // Validate MQ config if updating to MQ type
        if (request.getSourceType() == SourceType.MQ) {
            validateMqSourceConfig(request.getMqSourceConfig());
            request.setProperties(mergeMqFieldMappingsIntoProperties(request.getProperties(), request.getMqSourceConfig()));
        }

        converter.updateDomain(request, domain);
        eventDefinitionRepository.update(domain);

        boolean isMq = domain.getSourceType() == SourceType.MQ;

        // Handle consumer lifecycle on MQ config changes
        if (wasMq) {
            dynamicMqConsumerManager.unregister(eventName);
        }
        if (isMq && domain.isEnabled() && domain.getMqSourceConfig() != null) {
            dynamicMqConsumerManager.register(eventName, domain.getMqSourceConfig());
        }

        return converter.toDTO(domain);
    }

    public EventDefinitionDTO getByEventName(String eventName) {
        BehaviorEventDefinition domain = eventDefinitionRepository.findByEventName(eventName)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "事件定义不存在: " + eventName));
        return converter.toDTO(domain);
    }

    public PageResult<EventDefinitionDTO> page(String eventType, int pageNum, int pageSize) {
        long total = eventDefinitionRepository.countByEventType(eventType);
        List<BehaviorEventDefinition> list = eventDefinitionRepository.findByEventType(eventType, pageNum, pageSize);
        List<EventDefinitionDTO> dtoList = list.stream().map(converter::toDTO).toList();
        return PageResult.of(total, pageNum, pageSize, dtoList);
    }

    public void updateStatus(String eventName, Integer status) {
        BehaviorEventDefinition domain = eventDefinitionRepository.findByEventName(eventName)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "事件定义不存在: " + eventName));
        domain.setStatus(status);
        eventDefinitionRepository.update(domain);

        // Handle MQ consumer lifecycle
        if (domain.getSourceType() == SourceType.MQ && domain.getMqSourceConfig() != null) {
            if (domain.isEnabled()) {
                dynamicMqConsumerManager.register(eventName, domain.getMqSourceConfig());
            } else {
                dynamicMqConsumerManager.unregister(eventName);
            }
        }
    }

    public MqMappingTestResult testMqMapping(MqMappingTestRequest request) {
        Map<String, MqMappingTestResult.ExtractedField> extractedFields = new LinkedHashMap<>();
        List<MqMappingTestResult.FieldError> errors = new ArrayList<>();

        for (FieldMapping mapping : request.getFieldMappings()) {
            try {
                Object value = eventMessageParser.extractSingleField(request.getSampleMessage(), mapping);
                extractedFields.put(mapping.getTargetField(),
                        new MqMappingTestResult.ExtractedField(value));
            } catch (Exception e) {
                errors.add(new MqMappingTestResult.FieldError(mapping.getTargetField(), e.getMessage()));
            }
        }

        return new MqMappingTestResult(errors.isEmpty(), extractedFields, errors);
    }

    private void validateMqSourceConfig(MqSourceConfigDTO config) {
        if (config == null) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "MQ类型事件必须提供MQ来源配置");
        }
        if (config.getTopic() == null || config.getTopic().isBlank()) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "MQ来源配置的topic不能为空");
        }
        if (config.getConsumerGroup() == null || config.getConsumerGroup().isBlank()) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "MQ来源配置的consumerGroup不能为空");
        }
        if (config.getFieldMappings() == null || config.getFieldMappings().isEmpty()) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "MQ来源配置的字段映射不能为空");
        }
    }

    private List<PropertyDefinitionDTO> mergeMqFieldMappingsIntoProperties(List<PropertyDefinitionDTO> properties,
                                                                           MqSourceConfigDTO mqSourceConfig) {
        Map<String, PropertyDefinitionDTO> mergedProperties = new LinkedHashMap<>();

        if (properties != null) {
            for (PropertyDefinitionDTO property : properties) {
                if (property == null || property.getPropertyName() == null || property.getPropertyName().isBlank()) {
                    continue;
                }
                mergedProperties.put(property.getPropertyName(), property);
            }
        }

        for (FieldMapping mapping : mqSourceConfig.getFieldMappings()) {
            if (mapping == null || mapping.getTargetField() == null || mapping.getTargetField().isBlank()) {
                continue;
            }

            PropertyDefinitionDTO property = mergedProperties.computeIfAbsent(
                    mapping.getTargetField(),
                    this::newPropertyDefinition);
            if (property.getDisplayName() == null || property.getDisplayName().isBlank()) {
                property.setDisplayName(mapping.getTargetField());
            }
        }

        if (mergedProperties.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(mergedProperties.values());
    }

    private PropertyDefinitionDTO newPropertyDefinition(String propertyName) {
        PropertyDefinitionDTO property = new PropertyDefinitionDTO();
        property.setPropertyName(propertyName);
        property.setDisplayName(propertyName);
        property.setRequired(false);
        return property;
    }
}
