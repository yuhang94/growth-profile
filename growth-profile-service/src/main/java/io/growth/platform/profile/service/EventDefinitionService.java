package io.growth.platform.profile.service;

import io.growth.platform.common.core.exception.BizException;
import io.growth.platform.common.core.exception.CommonErrorCode;
import io.growth.platform.common.core.result.PageResult;
import io.growth.platform.profile.api.dto.EventDefinitionCreateRequest;
import io.growth.platform.profile.api.dto.EventDefinitionDTO;
import io.growth.platform.profile.api.dto.EventDefinitionUpdateRequest;
import io.growth.platform.profile.converter.EventDefinitionDTOConverter;
import io.growth.platform.profile.domain.model.BehaviorEventDefinition;
import io.growth.platform.profile.domain.repository.EventDefinitionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventDefinitionService {

    private final EventDefinitionRepository eventDefinitionRepository;
    private final EventDefinitionDTOConverter converter = EventDefinitionDTOConverter.INSTANCE;

    public EventDefinitionService(EventDefinitionRepository eventDefinitionRepository) {
        this.eventDefinitionRepository = eventDefinitionRepository;
    }

    public EventDefinitionDTO create(EventDefinitionCreateRequest request) {
        if (eventDefinitionRepository.existsByEventName(request.getEventName())) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "事件名称已存在: " + request.getEventName());
        }
        BehaviorEventDefinition domain = converter.toDomain(request);
        domain.setStatus(1);
        eventDefinitionRepository.insert(domain);
        return converter.toDTO(domain);
    }

    public EventDefinitionDTO update(String eventName, EventDefinitionUpdateRequest request) {
        BehaviorEventDefinition domain = eventDefinitionRepository.findByEventName(eventName)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "事件定义不存在: " + eventName));
        converter.updateDomain(request, domain);
        eventDefinitionRepository.update(domain);
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
    }
}
