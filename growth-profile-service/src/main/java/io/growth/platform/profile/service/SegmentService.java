package io.growth.platform.profile.service;

import io.growth.platform.common.core.exception.BizException;
import io.growth.platform.common.core.exception.CommonErrorCode;
import io.growth.platform.common.core.result.PageResult;
import io.growth.platform.profile.api.dto.*;
import io.growth.platform.profile.converter.SegmentDTOConverter;
import io.growth.platform.profile.domain.model.Segment;
import io.growth.platform.profile.domain.model.SegmentCondition;
import io.growth.platform.profile.domain.repository.SegmentQueryRepository;
import io.growth.platform.profile.domain.repository.SegmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SegmentService {

    private final SegmentRepository segmentRepository;
    private final SegmentQueryRepository segmentQueryRepository;
    private final SegmentDTOConverter converter = SegmentDTOConverter.INSTANCE;

    public SegmentService(SegmentRepository segmentRepository,
                          SegmentQueryRepository segmentQueryRepository) {
        this.segmentRepository = segmentRepository;
        this.segmentQueryRepository = segmentQueryRepository;
    }

    public SegmentDTO create(SegmentCreateRequest request) {
        Segment domain = converter.toDomain(request);
        domain.setStatus(1);
        segmentRepository.insert(domain);
        return converter.toDTO(domain);
    }

    public SegmentDTO update(Long id, SegmentUpdateRequest request) {
        Segment domain = segmentRepository.findById(id)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "分群不存在: " + id));
        converter.updateDomain(request, domain);
        segmentRepository.update(domain);
        return converter.toDTO(domain);
    }

    public SegmentDTO getById(Long id) {
        Segment domain = segmentRepository.findById(id)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "分群不存在: " + id));
        return converter.toDTO(domain);
    }

    public PageResult<SegmentDTO> page(int pageNum, int pageSize) {
        long total = segmentRepository.count();
        List<Segment> list = segmentRepository.findAll(pageNum, pageSize);
        List<SegmentDTO> dtoList = list.stream().map(converter::toDTO).toList();
        return PageResult.of(total, pageNum, pageSize, dtoList);
    }

    public void delete(Long id) {
        segmentRepository.findById(id)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "分群不存在: " + id));
        segmentRepository.deleteById(id);
    }

    public SegmentPreviewResult preview(SegmentPreviewRequest request) {
        SegmentCondition condition = converter.toConditionDomain(request.getRootCondition());
        long count = segmentQueryRepository.countUsers(condition);
        return new SegmentPreviewResult(count);
    }

    public SegmentDTO compute(Long id) {
        Segment domain = segmentRepository.findById(id)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "分群不存在: " + id));
        long count = segmentQueryRepository.countUsers(domain.getRootCondition());
        domain.setLastUserCount(count);
        domain.setLastComputedTime(LocalDateTime.now());
        segmentRepository.update(domain);
        return converter.toDTO(domain);
    }

    public PageResult<String> getSegmentUsers(Long id, int pageNum, int pageSize) {
        Segment domain = segmentRepository.findById(id)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "分群不存在: " + id));
        long total = segmentQueryRepository.countUsers(domain.getRootCondition());
        List<String> users = segmentQueryRepository.queryUsers(domain.getRootCondition(), pageNum, pageSize);
        return PageResult.of(total, pageNum, pageSize, users);
    }

    public SegmentMatchResult match(SegmentMatchRequest request) {
        Segment domain = segmentRepository.findById(request.getSegmentId())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "分群不存在: " + request.getSegmentId()));
        boolean matched = segmentQueryRepository.matchesUser(domain.getRootCondition(), request.getUserId());
        return new SegmentMatchResult(
                domain.getId(),
                request.getUserId(),
                matched,
                0L,
                matched ? "matched_by_realtime_query" : "not_matched_by_realtime_query"
        );
    }

    public SegmentBatchMatchResult batchMatch(SegmentBatchMatchRequest request) {
        Segment domain = segmentRepository.findById(request.getSegmentId())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "分群不存在: " + request.getSegmentId()));
        List<SegmentMatchResult> results = new ArrayList<>();
        for (String userId : request.getUserIds()) {
            boolean matched = segmentQueryRepository.matchesUser(domain.getRootCondition(), userId);
            results.add(new SegmentMatchResult(
                    domain.getId(),
                    userId,
                    matched,
                    0L,
                    matched ? "matched_by_realtime_query" : "not_matched_by_realtime_query"
            ));
        }
        return new SegmentBatchMatchResult(domain.getId(), results);
    }
}
