package io.growth.platform.profile.service;

import io.growth.platform.common.core.exception.BizException;
import io.growth.platform.common.core.exception.CommonErrorCode;
import io.growth.platform.common.core.result.PageResult;
import io.growth.platform.profile.api.dto.TagDefinitionCreateRequest;
import io.growth.platform.profile.api.dto.TagDefinitionDTO;
import io.growth.platform.profile.api.dto.TagDefinitionUpdateRequest;
import io.growth.platform.profile.converter.TagDefinitionDTOConverter;
import io.growth.platform.profile.domain.event.TagDefinitionChanged;
import io.growth.platform.profile.domain.model.TagDefinition;
import io.growth.platform.profile.domain.repository.TagDefinitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagDefinitionService {

    private final TagDefinitionRepository tagDefinitionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TagDefinitionDTOConverter converter = TagDefinitionDTOConverter.INSTANCE;

    public TagDefinitionService(TagDefinitionRepository tagDefinitionRepository,
                                ApplicationEventPublisher eventPublisher) {
        this.tagDefinitionRepository = tagDefinitionRepository;
        this.eventPublisher = eventPublisher;
    }

    public TagDefinitionDTO create(TagDefinitionCreateRequest request) {
        if (tagDefinitionRepository.existsByTagKey(request.getTagKey())) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "标签key已存在: " + request.getTagKey());
        }
        TagDefinition domain = converter.toDomain(request);
        domain.setStatus(1);
        tagDefinitionRepository.insert(domain);
        eventPublisher.publishEvent(new TagDefinitionChanged(this,
                domain.getId(), domain.getTagKey(), TagDefinitionChanged.ChangeType.CREATED));
        return converter.toDTO(domain);
    }

    public TagDefinitionDTO update(String tagKey, TagDefinitionUpdateRequest request) {
        TagDefinition domain = tagDefinitionRepository.findByTagKey(tagKey)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "标签定义不存在: " + tagKey));
        converter.updateDomain(request, domain);
        tagDefinitionRepository.update(domain);
        eventPublisher.publishEvent(new TagDefinitionChanged(this,
                domain.getId(), domain.getTagKey(), TagDefinitionChanged.ChangeType.UPDATED));
        return converter.toDTO(domain);
    }

    public TagDefinitionDTO getByTagKey(String tagKey) {
        TagDefinition domain = tagDefinitionRepository.findByTagKey(tagKey)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "标签定义不存在: " + tagKey));
        return converter.toDTO(domain);
    }

    public PageResult<TagDefinitionDTO> page(String category, int pageNum, int pageSize) {
        long total = tagDefinitionRepository.countByCategory(category);
        List<TagDefinition> list = tagDefinitionRepository.findByCategory(category, pageNum, pageSize);
        List<TagDefinitionDTO> dtoList = list.stream().map(converter::toDTO).toList();
        return PageResult.of(total, pageNum, pageSize, dtoList);
    }

    public void updateStatus(String tagKey, Integer status) {
        TagDefinition domain = tagDefinitionRepository.findByTagKey(tagKey)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "标签定义不存在: " + tagKey));
        domain.setStatus(status);
        tagDefinitionRepository.update(domain);
        eventPublisher.publishEvent(new TagDefinitionChanged(this,
                domain.getId(), domain.getTagKey(), TagDefinitionChanged.ChangeType.STATUS_CHANGED));
    }
}
