package io.growth.platform.profile.service;

import io.growth.platform.common.core.exception.BizException;
import io.growth.platform.profile.api.dto.BehaviorEventBatchRequest;
import io.growth.platform.profile.api.dto.BehaviorEventDTO;
import io.growth.platform.profile.api.dto.BehaviorEventRequest;
import io.growth.platform.profile.api.enums.EventType;
import io.growth.platform.profile.domain.model.BehaviorEvent;
import io.growth.platform.profile.domain.model.BehaviorEventDefinition;
import io.growth.platform.profile.domain.repository.BehaviorEventRepository;
import io.growth.platform.profile.domain.repository.EventDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BehaviorEventServiceTest {

    @Mock
    private BehaviorEventRepository behaviorEventRepository;

    @Mock
    private EventDefinitionRepository eventDefinitionRepository;

    @InjectMocks
    private BehaviorEventService behaviorEventService;

    private BehaviorEventDefinition enabledDefinition;

    @BeforeEach
    void setUp() {
        enabledDefinition = new BehaviorEventDefinition();
        enabledDefinition.setId(1L);
        enabledDefinition.setEventName("page_view");
        enabledDefinition.setEventType(EventType.PAGE_VIEW);
        enabledDefinition.setStatus(1);
    }

    @Test
    void report_success() {
        when(eventDefinitionRepository.findByEventName("page_view")).thenReturn(Optional.of(enabledDefinition));

        BehaviorEventRequest request = new BehaviorEventRequest();
        request.setUserId("user001");
        request.setEventName("page_view");
        request.setProperties(Map.of("page", "/home"));
        request.setEventTime(LocalDateTime.now());

        behaviorEventService.report(request);

        verify(behaviorEventRepository).insert(any());
    }

    @Test
    void report_eventNotFound() {
        when(eventDefinitionRepository.findByEventName("nonexistent")).thenReturn(Optional.empty());

        BehaviorEventRequest request = new BehaviorEventRequest();
        request.setUserId("user001");
        request.setEventName("nonexistent");
        request.setEventTime(LocalDateTime.now());

        assertThrows(BizException.class, () -> behaviorEventService.report(request));
    }

    @Test
    void report_eventDisabled() {
        BehaviorEventDefinition disabled = new BehaviorEventDefinition();
        disabled.setEventName("page_view");
        disabled.setEventType(EventType.PAGE_VIEW);
        disabled.setStatus(0);
        when(eventDefinitionRepository.findByEventName("page_view")).thenReturn(Optional.of(disabled));

        BehaviorEventRequest request = new BehaviorEventRequest();
        request.setUserId("user001");
        request.setEventName("page_view");
        request.setEventTime(LocalDateTime.now());

        assertThrows(BizException.class, () -> behaviorEventService.report(request));
    }

    @Test
    void batchReport_success() {
        when(eventDefinitionRepository.findByEventName("page_view")).thenReturn(Optional.of(enabledDefinition));

        BehaviorEventRequest item1 = new BehaviorEventRequest();
        item1.setUserId("user001");
        item1.setEventName("page_view");
        item1.setEventTime(LocalDateTime.now());

        BehaviorEventRequest item2 = new BehaviorEventRequest();
        item2.setUserId("user002");
        item2.setEventName("page_view");
        item2.setEventTime(LocalDateTime.now());

        BehaviorEventBatchRequest request = new BehaviorEventBatchRequest();
        request.setEvents(List.of(item1, item2));

        behaviorEventService.batchReport(request);

        verify(behaviorEventRepository).insertBatch(argThat(list -> list.size() == 2));
    }

    @Test
    void query_success() {
        BehaviorEvent event = new BehaviorEvent();
        event.setEventId("eid1");
        event.setUserId("user001");
        event.setEventName("page_view");
        event.setEventType("PAGE_VIEW");
        event.setProperties(Map.of());
        event.setEventTime(LocalDateTime.now());
        event.setCreatedTime(LocalDateTime.now());

        when(behaviorEventRepository.query(eq("user001"), eq("page_view"), any(), any(), eq(1), eq(20)))
                .thenReturn(List.of(event));

        List<BehaviorEventDTO> results = behaviorEventService.query("user001", "page_view", null, null, 1, 20);

        assertEquals(1, results.size());
        assertEquals("page_view", results.get(0).getEventName());
    }

    @Test
    void getUserRecentEvents_success() {
        BehaviorEvent event = new BehaviorEvent();
        event.setEventId("eid1");
        event.setUserId("user001");
        event.setEventName("click");
        event.setEventType("CLICK");
        event.setProperties(Map.of());
        event.setEventTime(LocalDateTime.now());
        event.setCreatedTime(LocalDateTime.now());

        when(behaviorEventRepository.queryByUserId("user001", 50)).thenReturn(List.of(event));

        List<BehaviorEventDTO> results = behaviorEventService.getUserRecentEvents("user001", 50);

        assertEquals(1, results.size());
        assertEquals("click", results.get(0).getEventName());
    }
}
