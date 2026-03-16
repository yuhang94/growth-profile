package io.growth.platform.profile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.growth.platform.profile.api.dto.BehaviorEventBatchRequest;
import io.growth.platform.profile.api.dto.BehaviorEventDTO;
import io.growth.platform.profile.api.dto.BehaviorEventRequest;
import io.growth.platform.profile.service.BehaviorEventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BehaviorEventController.class)
class BehaviorEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BehaviorEventService behaviorEventService;

    @Test
    void report_success() throws Exception {
        BehaviorEventRequest request = new BehaviorEventRequest();
        request.setUserId("user001");
        request.setEventName("page_view");
        request.setProperties(Map.of("page", "/home"));
        request.setEventTime(LocalDateTime.of(2024, 6, 1, 10, 0));

        mockMvc.perform(post("/api/v1/profile/behavior-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(behaviorEventService).report(any());
    }

    @Test
    void batchReport_success() throws Exception {
        BehaviorEventRequest item = new BehaviorEventRequest();
        item.setUserId("user001");
        item.setEventName("page_view");
        item.setEventTime(LocalDateTime.of(2024, 6, 1, 10, 0));

        BehaviorEventBatchRequest request = new BehaviorEventBatchRequest();
        request.setEvents(List.of(item));

        mockMvc.perform(post("/api/v1/profile/behavior-events/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(behaviorEventService).batchReport(any());
    }

    @Test
    void getUserRecentEvents_success() throws Exception {
        BehaviorEventDTO dto = new BehaviorEventDTO();
        dto.setEventId("eid1");
        dto.setUserId("user001");
        dto.setEventName("click");
        dto.setEventType("CLICK");
        dto.setEventTime(LocalDateTime.of(2024, 6, 1, 10, 0));
        dto.setCreatedTime(LocalDateTime.of(2024, 6, 1, 10, 0));

        when(behaviorEventService.getUserRecentEvents("user001", 50)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/profile/behavior-events/user/user001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].eventName").value("click"));
    }

    @Test
    void query_success() throws Exception {
        when(behaviorEventService.query(any(), any(), any(), any(), eq(1), eq(20)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/profile/behavior-events")
                        .param("userId", "user001")
                        .param("eventName", "page_view"))
                .andExpect(status().isOk());
    }
}
