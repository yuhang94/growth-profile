package io.growth.platform.profile.infrastructure.mq;

import io.growth.platform.profile.api.enums.SourceType;
import io.growth.platform.profile.domain.model.BehaviorEvent;
import io.growth.platform.profile.domain.model.BehaviorEventDefinition;
import io.growth.platform.profile.domain.model.MqSourceConfig;
import io.growth.platform.profile.domain.repository.BehaviorEventRepository;
import io.growth.platform.profile.domain.repository.EventDefinitionRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DynamicMqConsumerManager {

    private static final Logger log = LoggerFactory.getLogger(DynamicMqConsumerManager.class);

    private final ConcurrentHashMap<String, DefaultMQPushConsumer> activeConsumers = new ConcurrentHashMap<>();

    private final EventDefinitionRepository eventDefinitionRepository;
    private final BehaviorEventRepository behaviorEventRepository;
    private final EventMessageParser eventMessageParser;

    @Value("${rocketmq.name-server}")
    private String nameServer;

    public DynamicMqConsumerManager(EventDefinitionRepository eventDefinitionRepository,
                                    BehaviorEventRepository behaviorEventRepository,
                                    EventMessageParser eventMessageParser) {
        this.eventDefinitionRepository = eventDefinitionRepository;
        this.behaviorEventRepository = behaviorEventRepository;
        this.eventMessageParser = eventMessageParser;
    }

    @PostConstruct
    public void initOnStartup() {
        try {
            List<BehaviorEventDefinition> mqDefinitions =
                    eventDefinitionRepository.findAllBySourceTypeAndStatus(SourceType.MQ, 1);
            for (BehaviorEventDefinition def : mqDefinitions) {
                try {
                    register(def.getEventName(), def.getMqSourceConfig());
                } catch (Exception e) {
                    log.error("Failed to register MQ consumer for event: {}", def.getEventName(), e);
                }
            }
            log.info("Initialized {} dynamic MQ consumers", mqDefinitions.size());
        } catch (Exception e) {
            log.error("Failed to initialize dynamic MQ consumers", e);
        }
    }

    @PreDestroy
    public void shutdownAll() {
        for (Map.Entry<String, DefaultMQPushConsumer> entry : activeConsumers.entrySet()) {
            try {
                entry.getValue().shutdown();
                log.info("Shutdown MQ consumer for event: {}", entry.getKey());
            } catch (Exception e) {
                log.error("Error shutting down consumer for event: {}", entry.getKey(), e);
            }
        }
        activeConsumers.clear();
    }

    public void register(String eventName, MqSourceConfig config) {
        if (activeConsumers.containsKey(eventName)) {
            log.warn("Consumer already registered for event: {}, unregistering first", eventName);
            unregister(eventName);
        }

        try {
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(config.getConsumerGroup());
            consumer.setNamesrvAddr(nameServer);

            String tag = config.getTag() != null ? config.getTag() : "*";
            consumer.subscribe(config.getTopic(), tag);

            consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                for (MessageExt msg : msgs) {
                    try {
                        String rawJson = new String(msg.getBody(), StandardCharsets.UTF_8);
                        BehaviorEventDefinition def = eventDefinitionRepository.findByEventName(eventName).orElse(null);
                        if (def == null) {
                            log.warn("Event definition not found for MQ event: {}", eventName);
                            continue;
                        }
                        BehaviorEvent event = eventMessageParser.parse(
                                rawJson,
                                eventName,
                                config.getFieldMappings(),
                                def.getProperties());
                        event.setEventType(def.getEventType().name());

                        behaviorEventRepository.insert(event);
                        log.debug("Processed MQ event: eventName={}, userId={}", eventName, event.getUserId());
                    } catch (Exception e) {
                        log.error("Failed to process MQ message for event: {}", eventName, e);
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });

            consumer.start();
            activeConsumers.put(eventName, consumer);
            log.info("Registered MQ consumer for event: {}, topic={}, tag={}", eventName, config.getTopic(), tag);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register MQ consumer for event: " + eventName, e);
        }
    }

    public void unregister(String eventName) {
        DefaultMQPushConsumer consumer = activeConsumers.remove(eventName);
        if (consumer != null) {
            consumer.shutdown();
            log.info("Unregistered MQ consumer for event: {}", eventName);
        }
    }

    public Set<String> getActiveConsumerNames() {
        return activeConsumers.keySet();
    }

    public boolean isActive(String eventName) {
        return activeConsumers.containsKey(eventName);
    }
}
