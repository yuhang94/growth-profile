package io.growth.platform.profile.service;

import io.growth.platform.common.core.exception.BizException;
import io.growth.platform.common.core.result.PageResult;
import io.growth.platform.profile.api.dto.EventDefinitionCreateRequest;
import io.growth.platform.profile.api.dto.EventDefinitionDTO;
import io.growth.platform.profile.api.dto.EventDefinitionUpdateRequest;
import io.growth.platform.profile.api.dto.FieldMapping;
import io.growth.platform.profile.api.dto.MqSourceConfigDTO;
import io.growth.platform.profile.api.enums.EventType;
import io.growth.platform.profile.api.enums.ExtractStrategy;
import io.growth.platform.profile.api.enums.SourceType;
import io.growth.platform.profile.domain.model.BehaviorEventDefinition;
import io.growth.platform.profile.domain.model.MqSourceConfig;
import io.growth.platform.profile.domain.repository.EventDefinitionRepository;
import io.growth.platform.profile.infrastructure.mq.DynamicMqConsumerManager;
import io.growth.platform.profile.infrastructure.mq.EventMessageParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventDefinitionServiceTest {

    @Mock
    private EventDefinitionRepository eventDefinitionRepository;

    @Mock
    private DynamicMqConsumerManager dynamicMqConsumerManager;

    @Mock
    private EventMessageParser eventMessageParser;

    @InjectMocks
    private EventDefinitionService eventDefinitionService;

    private BehaviorEventDefinition sampleDomain;

    @BeforeEach
    void setUp() {
        sampleDomain = new BehaviorEventDefinition();
        sampleDomain.setId(1L);
        sampleDomain.setEventName("page_view");
        sampleDomain.setEventType(EventType.PAGE_VIEW);
        sampleDomain.setDisplayName("页面浏览");
        sampleDomain.setSourceType(SourceType.SDK);
        sampleDomain.setStatus(1);
    }

    @Test
    void create_success() {
        when(eventDefinitionRepository.existsByEventName("page_view")).thenReturn(false);
        doAnswer(inv -> {
            BehaviorEventDefinition def = inv.getArgument(0);
            def.setId(1L);
            return null;
        }).when(eventDefinitionRepository).insert(any());

        EventDefinitionCreateRequest request = new EventDefinitionCreateRequest();
        request.setEventName("page_view");
        request.setEventType(EventType.PAGE_VIEW);
        request.setDisplayName("页面浏览");

        EventDefinitionDTO result = eventDefinitionService.create(request);

        assertNotNull(result);
        assertEquals("page_view", result.getEventName());
        assertEquals("页面浏览", result.getDisplayName());
        verify(eventDefinitionRepository).insert(any());
        verify(dynamicMqConsumerManager, never()).register(any(), any());
    }

    @Test
    void create_mqType_registersConsumer() {
        when(eventDefinitionRepository.existsByEventName("mq_event")).thenReturn(false);
        doAnswer(inv -> {
            BehaviorEventDefinition def = inv.getArgument(0);
            def.setId(2L);
            return null;
        }).when(eventDefinitionRepository).insert(any());

        EventDefinitionCreateRequest request = new EventDefinitionCreateRequest();
        request.setEventName("mq_event");
        request.setEventType(EventType.CUSTOM);
        request.setDisplayName("MQ事件");
        request.setSourceType(SourceType.MQ);
        request.setMqSourceConfig(newMqConfig());

        eventDefinitionService.create(request);

        verify(dynamicMqConsumerManager).register(eq("mq_event"), any());
    }

    @Test
    void create_mqType_withoutConfig_throwsException() {
        when(eventDefinitionRepository.existsByEventName("mq_event")).thenReturn(false);

        EventDefinitionCreateRequest request = new EventDefinitionCreateRequest();
        request.setEventName("mq_event");
        request.setEventType(EventType.CUSTOM);
        request.setDisplayName("MQ事件");
        request.setSourceType(SourceType.MQ);

        assertThrows(BizException.class, () -> eventDefinitionService.create(request));
    }

    @Test
    void create_duplicateName_throwsException() {
        when(eventDefinitionRepository.existsByEventName("page_view")).thenReturn(true);

        EventDefinitionCreateRequest request = new EventDefinitionCreateRequest();
        request.setEventName("page_view");
        request.setEventType(EventType.PAGE_VIEW);
        request.setDisplayName("页面浏览");

        assertThrows(BizException.class, () -> eventDefinitionService.create(request));
    }

    @Test
    void update_success() {
        when(eventDefinitionRepository.findByEventName("page_view")).thenReturn(Optional.of(sampleDomain));

        EventDefinitionUpdateRequest request = new EventDefinitionUpdateRequest();
        request.setEventType(EventType.PAGE_VIEW);
        request.setDisplayName("页面浏览事件");

        EventDefinitionDTO result = eventDefinitionService.update("page_view", request);

        assertEquals("页面浏览事件", result.getDisplayName());
        verify(eventDefinitionRepository).update(any());
    }

    @Test
    void update_notFound() {
        when(eventDefinitionRepository.findByEventName("nonexistent")).thenReturn(Optional.empty());

        EventDefinitionUpdateRequest request = new EventDefinitionUpdateRequest();
        request.setEventType(EventType.CLICK);
        request.setDisplayName("test");

        assertThrows(BizException.class, () -> eventDefinitionService.update("nonexistent", request));
    }

    @Test
    void getByEventName_success() {
        when(eventDefinitionRepository.findByEventName("page_view")).thenReturn(Optional.of(sampleDomain));

        EventDefinitionDTO result = eventDefinitionService.getByEventName("page_view");

        assertEquals("page_view", result.getEventName());
    }

    @Test
    void getByEventName_notFound() {
        when(eventDefinitionRepository.findByEventName("nonexistent")).thenReturn(Optional.empty());

        assertThrows(BizException.class, () -> eventDefinitionService.getByEventName("nonexistent"));
    }

    @Test
    void page_success() {
        when(eventDefinitionRepository.countByEventType(null)).thenReturn(1L);
        when(eventDefinitionRepository.findByEventType(null, 1, 20)).thenReturn(List.of(sampleDomain));

        PageResult<EventDefinitionDTO> result = eventDefinitionService.page(null, 1, 20);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
    }

    @Test
    void updateStatus_success() {
        when(eventDefinitionRepository.findByEventName("page_view")).thenReturn(Optional.of(sampleDomain));

        eventDefinitionService.updateStatus("page_view", 0);

        assertEquals(0, sampleDomain.getStatus());
        verify(eventDefinitionRepository).update(sampleDomain);
    }

    @Test
    void updateStatus_mqType_enabled_registersConsumer() {
        BehaviorEventDefinition mqDomain = new BehaviorEventDefinition();
        mqDomain.setId(2L);
        mqDomain.setEventName("mq_event");
        mqDomain.setEventType(EventType.CUSTOM);
        mqDomain.setSourceType(SourceType.MQ);
        mqDomain.setMqSourceConfig(newMqSourceConfig());
        mqDomain.setStatus(0);

        when(eventDefinitionRepository.findByEventName("mq_event")).thenReturn(Optional.of(mqDomain));

        eventDefinitionService.updateStatus("mq_event", 1);

        verify(dynamicMqConsumerManager).register(eq("mq_event"), any());
    }

    @Test
    void updateStatus_mqType_disabled_unregistersConsumer() {
        BehaviorEventDefinition mqDomain = new BehaviorEventDefinition();
        mqDomain.setId(2L);
        mqDomain.setEventName("mq_event");
        mqDomain.setEventType(EventType.CUSTOM);
        mqDomain.setSourceType(SourceType.MQ);
        mqDomain.setMqSourceConfig(newMqSourceConfig());
        mqDomain.setStatus(1);

        when(eventDefinitionRepository.findByEventName("mq_event")).thenReturn(Optional.of(mqDomain));

        eventDefinitionService.updateStatus("mq_event", 0);

        verify(dynamicMqConsumerManager).unregister("mq_event");
    }

    private MqSourceConfigDTO newMqConfig() {
        MqSourceConfigDTO config = new MqSourceConfigDTO();
        config.setTopic("test-topic");
        config.setConsumerGroup("test-cg");

        FieldMapping mapping = new FieldMapping();
        mapping.setTargetField("userId");
        mapping.setStrategy(ExtractStrategy.JSON_PATH);
        mapping.setExpression("$.uid");
        config.setFieldMappings(List.of(mapping));

        return config;
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
