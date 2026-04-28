package io.growth.platform.profile.service;

import io.growth.platform.common.core.exception.BizException;
import io.growth.platform.common.core.result.PageResult;
import io.growth.platform.profile.api.dto.*;
import io.growth.platform.profile.api.enums.CompareOperator;
import io.growth.platform.profile.api.enums.ConditionOperator;
import io.growth.platform.profile.domain.model.Segment;
import io.growth.platform.profile.domain.model.SegmentCondition;
import io.growth.platform.profile.domain.repository.SegmentQueryRepository;
import io.growth.platform.profile.domain.repository.SegmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SegmentServiceTest {

    @Mock
    private SegmentRepository segmentRepository;

    @Mock
    private SegmentQueryRepository segmentQueryRepository;

    @InjectMocks
    private SegmentService segmentService;

    private Segment sampleSegment;

    @BeforeEach
    void setUp() {
        sampleSegment = new Segment();
        sampleSegment.setId(1L);
        sampleSegment.setSegmentName("高价值用户");
        sampleSegment.setDescription("年龄>25的男性");
        sampleSegment.setStatus(1);

        SegmentCondition leaf = new SegmentCondition();
        leaf.setTagKey("age");
        leaf.setCompareOp(CompareOperator.GT);
        leaf.setValues(List.of("25"));
        sampleSegment.setRootCondition(leaf);
    }

    @Test
    void create_success() {
        doAnswer(inv -> {
            Segment s = inv.getArgument(0);
            s.setId(1L);
            return null;
        }).when(segmentRepository).insert(any());

        SegmentCreateRequest request = new SegmentCreateRequest();
        request.setSegmentName("高价值用户");
        request.setDescription("年龄>25");
        SegmentConditionDTO condition = new SegmentConditionDTO();
        condition.setTagKey("age");
        condition.setCompareOp(CompareOperator.GT);
        condition.setValues(List.of("25"));
        request.setRootCondition(condition);

        SegmentDTO result = segmentService.create(request);

        assertNotNull(result);
        assertEquals("高价值用户", result.getSegmentName());
        verify(segmentRepository).insert(any());
    }

    @Test
    void update_success() {
        when(segmentRepository.findById(1L)).thenReturn(Optional.of(sampleSegment));

        SegmentUpdateRequest request = new SegmentUpdateRequest();
        request.setSegmentName("更新分群");
        request.setDescription("更新描述");
        SegmentConditionDTO condition = new SegmentConditionDTO();
        condition.setTagKey("age");
        condition.setCompareOp(CompareOperator.GT);
        condition.setValues(List.of("30"));
        request.setRootCondition(condition);

        SegmentDTO result = segmentService.update(1L, request);

        assertEquals("更新分群", result.getSegmentName());
        verify(segmentRepository).update(any());
    }

    @Test
    void update_notFound() {
        when(segmentRepository.findById(99L)).thenReturn(Optional.empty());

        SegmentUpdateRequest request = new SegmentUpdateRequest();
        request.setSegmentName("test");
        SegmentConditionDTO condition = new SegmentConditionDTO();
        request.setRootCondition(condition);

        assertThrows(BizException.class, () -> segmentService.update(99L, request));
    }

    @Test
    void getById_success() {
        when(segmentRepository.findById(1L)).thenReturn(Optional.of(sampleSegment));

        SegmentDTO result = segmentService.getById(1L);

        assertEquals("高价值用户", result.getSegmentName());
    }

    @Test
    void getById_notFound() {
        when(segmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BizException.class, () -> segmentService.getById(99L));
    }

    @Test
    void delete_success() {
        when(segmentRepository.findById(1L)).thenReturn(Optional.of(sampleSegment));

        segmentService.delete(1L);

        verify(segmentRepository).deleteById(1L);
    }

    @Test
    void page_success() {
        when(segmentRepository.count()).thenReturn(1L);
        when(segmentRepository.findAll(1, 20)).thenReturn(List.of(sampleSegment));

        PageResult<SegmentDTO> result = segmentService.page(1, 20);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
    }

    @Test
    void preview_success() {
        when(segmentQueryRepository.countUsers(any())).thenReturn(500L);

        SegmentPreviewRequest request = new SegmentPreviewRequest();
        SegmentConditionDTO condition = new SegmentConditionDTO();
        condition.setTagKey("age");
        condition.setCompareOp(CompareOperator.GT);
        condition.setValues(List.of("25"));
        request.setRootCondition(condition);

        SegmentPreviewResult result = segmentService.preview(request);

        assertEquals(500L, result.getUserCount());
    }

    @Test
    void compute_success() {
        when(segmentRepository.findById(1L)).thenReturn(Optional.of(sampleSegment));
        when(segmentQueryRepository.countUsers(any())).thenReturn(1000L);

        SegmentDTO result = segmentService.compute(1L);

        assertEquals(1000L, result.getLastUserCount());
        assertNotNull(result.getLastComputedTime());
        verify(segmentRepository).update(any());
    }

    @Test
    void getSegmentUsers_success() {
        when(segmentRepository.findById(1L)).thenReturn(Optional.of(sampleSegment));
        when(segmentQueryRepository.countUsers(any())).thenReturn(2L);
        when(segmentQueryRepository.queryUsers(any(), eq(1), eq(20))).thenReturn(List.of("user001", "user002"));

        PageResult<String> result = segmentService.getSegmentUsers(1L, 1, 20);

        assertEquals(2, result.getTotal());
        assertEquals(List.of("user001", "user002"), result.getList());
    }

    @Test
    void match_success() {
        when(segmentRepository.findById(1L)).thenReturn(Optional.of(sampleSegment));
        when(segmentQueryRepository.matchesUser(any(), eq("user001"))).thenReturn(true);

        SegmentMatchRequest request = new SegmentMatchRequest();
        request.setSegmentId(1L);
        request.setUserId("user001");
        request.setContextTime(LocalDateTime.now());

        SegmentMatchResult result = segmentService.match(request);

        assertTrue(result.getMatched());
        assertEquals(1L, result.getSegmentId());
        assertEquals("matched_by_realtime_query", result.getReason());
    }

    @Test
    void batchMatch_success() {
        when(segmentRepository.findById(1L)).thenReturn(Optional.of(sampleSegment));
        when(segmentQueryRepository.matchesUser(any(), eq("user001"))).thenReturn(true);
        when(segmentQueryRepository.matchesUser(any(), eq("user002"))).thenReturn(false);

        SegmentBatchMatchRequest request = new SegmentBatchMatchRequest();
        request.setSegmentId(1L);
        request.setUserIds(List.of("user001", "user002"));
        request.setContextTime(LocalDateTime.now());

        SegmentBatchMatchResult result = segmentService.batchMatch(request);

        assertEquals(2, result.getResults().size());
        assertTrue(result.getResults().get(0).getMatched());
        assertFalse(result.getResults().get(1).getMatched());
    }
}
