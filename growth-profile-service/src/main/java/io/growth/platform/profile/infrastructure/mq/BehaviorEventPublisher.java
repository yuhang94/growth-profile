package io.growth.platform.profile.infrastructure.mq;

import io.growth.platform.profile.api.dto.NormalizedBehaviorEvent;
import io.growth.platform.profile.api.enums.SourceType;
import io.growth.platform.profile.domain.model.BehaviorEvent;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BehaviorEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(BehaviorEventPublisher.class);

    private final RocketMQTemplate rocketMQTemplate;

    public BehaviorEventPublisher(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    public void publish(BehaviorEvent event, SourceType sourceType, String sourceName) {
        if (event == null) {
            return;
        }

        NormalizedBehaviorEvent message = new NormalizedBehaviorEvent();
        message.setTraceId(UUID.randomUUID().toString().replace("-", ""));
        message.setEventId(event.getEventId());
        message.setTenantId("default");
        message.setUserId(event.getUserId());
        message.setEventName(event.getEventName());
        message.setEventType(event.getEventType());
        message.setOccurredAt(event.getEventTime());
        message.setSourceType(sourceType != null ? sourceType.name() : null);
        message.setSourceName(sourceName);
        message.setProperties(event.getProperties());

        try {
            rocketMQTemplate.convertAndSend(RocketMQConfig.TOPIC_BEHAVIOR_EVENT_NORMALIZED, message);
            log.info("Published normalized behavior event: eventId={}, eventName={}, userId={}",
                    event.getEventId(), event.getEventName(), event.getUserId());
        } catch (Exception e) {
            log.error("Failed to publish normalized behavior event: eventId={}, eventName={}, userId={}",
                    event.getEventId(), event.getEventName(), event.getUserId(), e);
        }
    }
}
