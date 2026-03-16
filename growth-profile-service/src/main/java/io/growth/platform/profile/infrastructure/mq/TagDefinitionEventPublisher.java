package io.growth.platform.profile.infrastructure.mq;

import io.growth.platform.profile.api.event.TagDefinitionChangedEvent;
import io.growth.platform.profile.domain.event.TagDefinitionChanged;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TagDefinitionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TagDefinitionEventPublisher.class);

    private final RocketMQTemplate rocketMQTemplate;

    public TagDefinitionEventPublisher(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @EventListener
    public void onTagDefinitionChanged(TagDefinitionChanged event) {
        TagDefinitionChangedEvent mqEvent = new TagDefinitionChangedEvent();
        mqEvent.setTagDefinitionId(event.getTagDefinitionId());
        mqEvent.setTagKey(event.getTagKey());
        mqEvent.setChangeType(TagDefinitionChangedEvent.ChangeType.valueOf(event.getChangeType().name()));
        mqEvent.setTimestamp(LocalDateTime.now());

        try {
            rocketMQTemplate.convertAndSend(RocketMQConfig.TOPIC_TAG_DEFINITION_CHANGED, mqEvent);
            log.info("Published tag definition changed event: tagKey={}, changeType={}",
                    event.getTagKey(), event.getChangeType());
        } catch (Exception e) {
            log.error("Failed to publish tag definition changed event", e);
        }
    }
}
