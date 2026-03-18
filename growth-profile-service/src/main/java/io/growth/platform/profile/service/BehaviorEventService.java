package io.growth.platform.profile.service;

import io.growth.platform.common.core.exception.BizException;
import io.growth.platform.common.core.exception.CommonErrorCode;
import io.growth.platform.profile.api.dto.BehaviorEventBatchRequest;
import io.growth.platform.profile.api.dto.BehaviorEventDTO;
import io.growth.platform.profile.api.dto.BehaviorEventRequest;
import io.growth.platform.profile.api.enums.SourceType;
import io.growth.platform.profile.converter.BehaviorEventDTOConverter;
import io.growth.platform.profile.domain.model.BehaviorEvent;
import io.growth.platform.profile.domain.model.BehaviorEventDefinition;
import io.growth.platform.profile.domain.repository.BehaviorEventRepository;
import io.growth.platform.profile.domain.repository.EventDefinitionRepository;
import io.growth.platform.profile.domain.service.EventPropertyValidator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BehaviorEventService {

    private final BehaviorEventRepository behaviorEventRepository;
    private final EventDefinitionRepository eventDefinitionRepository;
    private final EventPropertyValidator eventPropertyValidator;
    private final BehaviorEventDTOConverter converter = BehaviorEventDTOConverter.INSTANCE;

    public BehaviorEventService(BehaviorEventRepository behaviorEventRepository,
                                EventDefinitionRepository eventDefinitionRepository,
                                EventPropertyValidator eventPropertyValidator) {
        this.behaviorEventRepository = behaviorEventRepository;
        this.eventDefinitionRepository = eventDefinitionRepository;
        this.eventPropertyValidator = eventPropertyValidator;
    }

    public void report(BehaviorEventRequest request) {
        BehaviorEventDefinition definition = eventDefinitionRepository.findByEventName(request.getEventName())
                .orElseThrow(() -> new BizException(CommonErrorCode.PARAM_ERROR, "事件定义不存在: " + request.getEventName()));
        if (!definition.isEnabled()) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "事件已禁用: " + request.getEventName());
        }

        // MQ type events cannot be reported via HTTP
        if (definition.getSourceType() == SourceType.MQ) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "MQ类型事件不允许通过HTTP上报: " + request.getEventName());
        }

        // Validate properties
        eventPropertyValidator.validate(request.getProperties(), definition.getProperties());

        BehaviorEvent event = new BehaviorEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setUserId(request.getUserId());
        event.setEventName(request.getEventName());
        event.setEventType(definition.getEventType().name());
        event.setProperties(request.getProperties());
        event.setEventTime(request.getEventTime());
        event.setCreatedTime(LocalDateTime.now());

        behaviorEventRepository.insert(event);
    }

    public void batchReport(BehaviorEventBatchRequest request) {
        List<BehaviorEvent> events = new ArrayList<>();
        for (BehaviorEventRequest item : request.getEvents()) {
            BehaviorEventDefinition definition = eventDefinitionRepository.findByEventName(item.getEventName())
                    .orElseThrow(() -> new BizException(CommonErrorCode.PARAM_ERROR, "事件定义不存在: " + item.getEventName()));
            if (!definition.isEnabled()) {
                throw new BizException(CommonErrorCode.PARAM_ERROR, "事件已禁用: " + item.getEventName());
            }

            // MQ type events cannot be reported via HTTP
            if (definition.getSourceType() == SourceType.MQ) {
                throw new BizException(CommonErrorCode.PARAM_ERROR, "MQ类型事件不允许通过HTTP上报: " + item.getEventName());
            }

            // Validate properties
            eventPropertyValidator.validate(item.getProperties(), definition.getProperties());

            BehaviorEvent event = new BehaviorEvent();
            event.setEventId(UUID.randomUUID().toString());
            event.setUserId(item.getUserId());
            event.setEventName(item.getEventName());
            event.setEventType(definition.getEventType().name());
            event.setProperties(item.getProperties());
            event.setEventTime(item.getEventTime());
            event.setCreatedTime(LocalDateTime.now());
            events.add(event);
        }
        behaviorEventRepository.insertBatch(events);
    }

    public List<BehaviorEventDTO> query(String userId, String eventName,
                                        LocalDateTime startTime, LocalDateTime endTime,
                                        int pageNum, int pageSize) {
        List<BehaviorEvent> events = behaviorEventRepository.query(userId, eventName, startTime, endTime, pageNum, pageSize);
        return events.stream().map(converter::toDTO).toList();
    }

    public List<BehaviorEventDTO> getUserRecentEvents(String userId, int limit) {
        List<BehaviorEvent> events = behaviorEventRepository.queryByUserId(userId, limit);
        return events.stream().map(converter::toDTO).toList();
    }
}
