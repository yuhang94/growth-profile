package io.growth.platform.profile.service;

import io.growth.platform.common.core.exception.BizException;
import io.growth.platform.common.core.exception.CommonErrorCode;
import io.growth.platform.profile.api.dto.SegmentTemplateCreateRequest;
import io.growth.platform.profile.api.dto.SegmentTemplateDTO;
import io.growth.platform.profile.converter.SegmentTemplateDTOConverter;
import io.growth.platform.profile.domain.model.SegmentTemplate;
import io.growth.platform.profile.domain.repository.SegmentTemplateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SegmentTemplateService {

    private final SegmentTemplateRepository segmentTemplateRepository;
    private final SegmentTemplateDTOConverter converter = SegmentTemplateDTOConverter.INSTANCE;

    public SegmentTemplateService(SegmentTemplateRepository segmentTemplateRepository) {
        this.segmentTemplateRepository = segmentTemplateRepository;
    }

    public List<SegmentTemplateDTO> listAll() {
        return segmentTemplateRepository.findAllOrderBySortOrder().stream()
                .map(converter::toDTO)
                .toList();
    }

    public SegmentTemplateDTO create(SegmentTemplateCreateRequest request) {
        segmentTemplateRepository.findByTemplateKey(request.getTemplateKey()).ifPresent(existing -> {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "模板标识已存在: " + request.getTemplateKey());
        });
        SegmentTemplate domain = converter.toDomain(request);
        domain.setBuiltIn(false);
        segmentTemplateRepository.insert(domain);
        return converter.toDTO(domain);
    }

    public void delete(Long id) {
        SegmentTemplate template = segmentTemplateRepository.findById(id)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "模板不存在: " + id));
        if (template.isBuiltIn()) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "内置模板不允许删除");
        }
        segmentTemplateRepository.deleteById(id);
    }
}
