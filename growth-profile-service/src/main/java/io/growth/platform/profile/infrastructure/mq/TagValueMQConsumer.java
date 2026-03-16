package io.growth.platform.profile.infrastructure.mq;

import io.growth.platform.profile.api.dto.TagValueBatchMQMessage;
import io.growth.platform.profile.api.dto.TagValueMQMessage;
import io.growth.platform.profile.api.dto.TagValueWriteRequest;
import io.growth.platform.profile.service.TagValueService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
        topic = RocketMQConfig.TOPIC_TAG_VALUE_WRITE,
        consumerGroup = RocketMQConfig.CONSUMER_GROUP_TAG_VALUE_WRITE
)
public class TagValueMQConsumer implements RocketMQListener<TagValueBatchMQMessage> {

    private static final Logger log = LoggerFactory.getLogger(TagValueMQConsumer.class);

    private final TagValueService tagValueService;

    public TagValueMQConsumer(TagValueService tagValueService) {
        this.tagValueService = tagValueService;
    }

    @Override
    public void onMessage(TagValueBatchMQMessage message) {
        if (message.getItems() == null || message.getItems().isEmpty()) {
            return;
        }
        log.info("Received tag value write message, size={}", message.getItems().size());
        for (TagValueMQMessage item : message.getItems()) {
            try {
                TagValueWriteRequest request = new TagValueWriteRequest();
                request.setUserId(item.getUserId());
                request.setTagKey(item.getTagKey());
                request.setTagValue(item.getTagValue());
                tagValueService.write(request);
            } catch (Exception e) {
                log.error("Failed to write tag value: userId={}, tagKey={}", item.getUserId(), item.getTagKey(), e);
            }
        }
    }
}
