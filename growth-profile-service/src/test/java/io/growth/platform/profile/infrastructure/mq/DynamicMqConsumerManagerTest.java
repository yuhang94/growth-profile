package io.growth.platform.profile.infrastructure.mq;

import io.growth.platform.profile.api.dto.FieldMapping;
import io.growth.platform.profile.api.enums.ExtractStrategy;
import io.growth.platform.profile.domain.model.MqSourceConfig;
import io.growth.platform.profile.domain.repository.BehaviorEventRepository;
import io.growth.platform.profile.domain.repository.EventDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DynamicMqConsumerManagerTest {

    @Mock
    private EventDefinitionRepository eventDefinitionRepository;

    @Mock
    private BehaviorEventRepository behaviorEventRepository;

    @Mock
    private EventMessageParser eventMessageParser;

    @Mock
    private BehaviorEventPublisher behaviorEventPublisher;

    private DynamicMqConsumerManager manager;

    @BeforeEach
    void setUp() {
        manager = new DynamicMqConsumerManager(
                eventDefinitionRepository,
                behaviorEventRepository,
                eventMessageParser,
                behaviorEventPublisher
        );
    }

    @Test
    void getActiveConsumerNames_empty() {
        assertTrue(manager.getActiveConsumerNames().isEmpty());
    }

    @Test
    void isActive_noConsumer_returnsFalse() {
        assertFalse(manager.isActive("nonexistent"));
    }

    @Test
    void unregister_noConsumer_noError() {
        // Should not throw
        manager.unregister("nonexistent");
    }

    @Test
    void shutdownAll_empty_noError() {
        // Should not throw
        manager.shutdownAll();
    }

    private MqSourceConfig newMqSourceConfig() {
        MqSourceConfig config = new MqSourceConfig();
        config.setTopic("test-topic");
        config.setConsumerGroup("test-cg");

        FieldMapping mapping = new FieldMapping();
        mapping.setTargetField("userId");
        mapping.setStrategy(ExtractStrategy.JSON_PATH);
        mapping.setExpression("$.uid");
        config.setFieldMappings(List.of(mapping));

        return config;
    }
}
