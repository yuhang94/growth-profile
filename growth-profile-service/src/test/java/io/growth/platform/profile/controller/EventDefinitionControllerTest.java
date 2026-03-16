package io.growth.platform.profile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.growth.platform.common.core.result.PageResult;
import io.growth.platform.profile.api.dto.EventDefinitionCreateRequest;
import io.growth.platform.profile.api.dto.EventDefinitionDTO;
import io.growth.platform.profile.api.dto.EventDefinitionUpdateRequest;
import io.growth.platform.profile.api.enums.EventType;
import io.growth.platform.profile.service.EventDefinitionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventDefinitionController.class)
class EventDefinitionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventDefinitionService eventDefinitionService;

    @Test
    void create_success() throws Exception {
        EventDefinitionDTO dto = new EventDefinitionDTO();
        dto.setId(1L);
        dto.setEventName("page_view");
        dto.setDisplayName("页面浏览");
        dto.setEventType(EventType.PAGE_VIEW);
        when(eventDefinitionService.create(any())).thenReturn(dto);

        EventDefinitionCreateRequest request = new EventDefinitionCreateRequest();
        request.setEventName("page_view");
        request.setEventType(EventType.PAGE_VIEW);
        request.setDisplayName("页面浏览");

        mockMvc.perform(post("/api/v1/profile/event-definitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventName").value("page_view"))
                .andExpect(jsonPath("$.data.displayName").value("页面浏览"));
    }

    @Test
    void update_success() throws Exception {
        EventDefinitionDTO dto = new EventDefinitionDTO();
        dto.setId(1L);
        dto.setEventName("page_view");
        dto.setDisplayName("页面浏览事件");
        when(eventDefinitionService.update(eq("page_view"), any())).thenReturn(dto);

        EventDefinitionUpdateRequest request = new EventDefinitionUpdateRequest();
        request.setEventType(EventType.PAGE_VIEW);
        request.setDisplayName("页面浏览事件");

        mockMvc.perform(put("/api/v1/profile/event-definitions/page_view")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayName").value("页面浏览事件"));
    }

    @Test
    void getByEventName_success() throws Exception {
        EventDefinitionDTO dto = new EventDefinitionDTO();
        dto.setEventName("page_view");
        dto.setDisplayName("页面浏览");
        when(eventDefinitionService.getByEventName("page_view")).thenReturn(dto);

        mockMvc.perform(get("/api/v1/profile/event-definitions/page_view"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventName").value("page_view"));
    }

    @Test
    void page_success() throws Exception {
        EventDefinitionDTO dto = new EventDefinitionDTO();
        dto.setEventName("page_view");
        when(eventDefinitionService.page(null, 1, 20)).thenReturn(PageResult.of(1L, 1, 20, List.of(dto)));

        mockMvc.perform(get("/api/v1/profile/event-definitions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void updateStatus_success() throws Exception {
        mockMvc.perform(put("/api/v1/profile/event-definitions/page_view/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", 0))))
                .andExpect(status().isOk());

        verify(eventDefinitionService).updateStatus("page_view", 0);
    }
}
