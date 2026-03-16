package io.growth.platform.profile.infrastructure.mq;

import io.growth.platform.profile.api.dto.BehaviorEventMQMessage;
import io.growth.platform.profile.api.dto.BehaviorEventRequest;
import io.growth.platform.profile.service.BehaviorEventService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
        topic = RocketMQConfig.TOPIC_BEHAVIOR_EVENT,
        consumerGroup = RocketMQConfig.CONSUMER_GROUP_BEHAVIOR_EVENT
)
public class BehaviorEventMQConsumer implements RocketMQListener<BehaviorEventMQMessage> {

    private static final Logger log = LoggerFactory.getLogger(BehaviorEventMQConsumer.class);

    private final BehaviorEventService behaviorEventService;

    public BehaviorEventMQConsumer(BehaviorEventService behaviorEventService) {
        this.behaviorEventService = behaviorEventService;
    }

    @Override
    public void onMessage(BehaviorEventMQMessage message) {
        log.info("Received behavior event message: userId={}, eventName={}",
                message.getUserId(), message.getEventName());
        try {
            BehaviorEventRequest request = new BehaviorEventRequest();
            request.setUserId(message.getUserId());
            request.setEventName(message.getEventName());
            request.setProperties(message.getProperties());
            request.setEventTime(message.getEventTime());
            behaviorEventService.report(request);
        } catch (Exception e) {
            log.error("Failed to process behavior event: userId={}, eventName={}",
                    message.getUserId(), message.getEventName(), e);
        }
    }
}
