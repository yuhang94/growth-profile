package io.growth.platform.profile.service;

import io.growth.platform.common.core.exception.BizException;
import io.growth.platform.common.core.exception.CommonErrorCode;
import io.growth.platform.profile.api.dto.*;
import io.growth.platform.profile.converter.TagValueDTOConverter;
import io.growth.platform.profile.domain.event.TagValueChanged;
import io.growth.platform.profile.domain.model.TagValue;
import io.growth.platform.profile.domain.repository.TagDefinitionRepository;
import io.growth.platform.profile.domain.repository.TagValueRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TagValueService {

    private final TagValueRepository tagValueRepository;
    private final TagDefinitionRepository tagDefinitionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TagValueDTOConverter converter = TagValueDTOConverter.INSTANCE;

    public TagValueService(TagValueRepository tagValueRepository,
                           TagDefinitionRepository tagDefinitionRepository,
                           ApplicationEventPublisher eventPublisher) {
        this.tagValueRepository = tagValueRepository;
        this.tagDefinitionRepository = tagDefinitionRepository;
        this.eventPublisher = eventPublisher;
    }

    public void write(TagValueWriteRequest request) {
        validateTagKey(request.getTagKey());
        TagValue tagValue = converter.toDomain(request);
        tagValueRepository.put(tagValue);
        eventPublisher.publishEvent(new TagValueChanged(this,
                request.getUserId(), request.getTagKey(), request.getTagValue(),
                TagValueChanged.ChangeType.PUT));
    }

    public void batchWrite(TagValueBatchWriteRequest request) {
        request.getItems().forEach(item -> validateTagKey(item.getTagKey()));
        List<TagValue> tagValues = request.getItems().stream().map(converter::toDomain).toList();
        tagValueRepository.putBatch(tagValues);
        for (TagValueWriteRequest item : request.getItems()) {
            eventPublisher.publishEvent(new TagValueChanged(this,
                    item.getUserId(), item.getTagKey(), item.getTagValue(),
                    TagValueChanged.ChangeType.PUT));
        }
    }

    public TagValueDTO getTagValue(String userId, String tagKey) {
        String value = tagValueRepository.get(userId, tagKey)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND,
                        "标签值不存在: userId=" + userId + ", tagKey=" + tagKey));
        TagValueDTO dto = new TagValueDTO();
        dto.setUserId(userId);
        dto.setTagKey(tagKey);
        dto.setTagValue(value);
        return dto;
    }

    public UserTagsDTO getUserTags(String userId) {
        Map<String, String> tags = tagValueRepository.getUserTags(userId);
        return converter.toUserTagsDTO(userId, tags);
    }

    public void delete(String userId, String tagKey) {
        tagValueRepository.delete(userId, tagKey);
        eventPublisher.publishEvent(new TagValueChanged(this,
                userId, tagKey, null, TagValueChanged.ChangeType.DELETE));
    }

    private void validateTagKey(String tagKey) {
        if (!tagDefinitionRepository.existsByTagKey(tagKey)) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "标签定义不存在: " + tagKey);
        }
    }
}
