package io.growth.platform.profile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.growth.platform.common.core.result.PageResult;
import io.growth.platform.profile.api.dto.*;
import io.growth.platform.profile.api.enums.CompareOperator;
import io.growth.platform.profile.service.SegmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SegmentController.class)
class SegmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SegmentService segmentService;

    @Test
    void create_success() throws Exception {
        SegmentDTO dto = new SegmentDTO();
        dto.setId(1L);
        dto.setSegmentName("高价值用户");
        when(segmentService.create(any())).thenReturn(dto);

        SegmentCreateRequest request = new SegmentCreateRequest();
        request.setSegmentName("高价值用户");
        SegmentConditionDTO condition = new SegmentConditionDTO();
        condition.setTagKey("age");
        condition.setCompareOp(CompareOperator.GT);
        condition.setValues(List.of("25"));
        request.setRootCondition(condition);

        mockMvc.perform(post("/api/v1/profile/segments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.segmentName").value("高价值用户"));
    }

    @Test
    void getById_success() throws Exception {
        SegmentDTO dto = new SegmentDTO();
        dto.setId(1L);
        dto.setSegmentName("测试分群");
        when(segmentService.getById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/profile/segments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.segmentName").value("测试分群"));
    }

    @Test
    void page_success() throws Exception {
        SegmentDTO dto = new SegmentDTO();
        dto.setId(1L);
        dto.setSegmentName("分群A");
        when(segmentService.page(1, 20)).thenReturn(PageResult.of(1L, 1, 20, List.of(dto)));

        mockMvc.perform(get("/api/v1/profile/segments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void delete_success() throws Exception {
        mockMvc.perform(delete("/api/v1/profile/segments/1"))
                .andExpect(status().isOk());

        verify(segmentService).delete(1L);
    }

    @Test
    void preview_success() throws Exception {
        when(segmentService.preview(any())).thenReturn(new SegmentPreviewResult(500L));

        SegmentPreviewRequest request = new SegmentPreviewRequest();
        SegmentConditionDTO condition = new SegmentConditionDTO();
        condition.setTagKey("age");
        condition.setCompareOp(CompareOperator.GT);
        condition.setValues(List.of("25"));
        request.setRootCondition(condition);

        mockMvc.perform(post("/api/v1/profile/segments/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userCount").value(500));
    }

    @Test
    void compute_success() throws Exception {
        SegmentDTO dto = new SegmentDTO();
        dto.setId(1L);
        dto.setLastUserCount(1000L);
        when(segmentService.compute(1L)).thenReturn(dto);

        mockMvc.perform(post("/api/v1/profile/segments/1/compute"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lastUserCount").value(1000));
    }

    @Test
    void getSegmentUsers_success() throws Exception {
        when(segmentService.getSegmentUsers(1L, 1, 20))
                .thenReturn(PageResult.of(2L, 1, 20, List.of("user001", "user002")));

        mockMvc.perform(get("/api/v1/profile/segments/1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2));
    }
}
