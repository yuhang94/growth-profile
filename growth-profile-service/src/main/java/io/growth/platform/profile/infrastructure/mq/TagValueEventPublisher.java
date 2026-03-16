package io.growth.platform.profile.infrastructure.mq;

import io.growth.platform.profile.api.event.TagValueChangedEvent;
import io.growth.platform.profile.domain.event.TagValueChanged;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TagValueEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TagValueEventPublisher.class);

    private final RocketMQTemplate rocketMQTemplate;

    public TagValueEventPublisher(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @EventListener
    public void onTagValueChanged(TagValueChanged event) {
        TagValueChangedEvent mqEvent = new TagValueChangedEvent();
        mqEvent.setUserId(event.getUserId());
        mqEvent.setTagKey(event.getTagKey());
        mqEvent.setTagValue(event.getTagValue());
        mqEvent.setChangeType(TagValueChangedEvent.ChangeType.valueOf(event.getChangeType().name()));
        mqEvent.setTimestamp(LocalDateTime.now());

        try {
            rocketMQTemplate.convertAndSend(RocketMQConfig.TOPIC_TAG_VALUE_CHANGED, mqEvent);
            log.info("Published tag value changed event: userId={}, tagKey={}, changeType={}",
                    event.getUserId(), event.getTagKey(), event.getChangeType());
        } catch (Exception e) {
            log.error("Failed to publish tag value changed event", e);
        }
    }
}
