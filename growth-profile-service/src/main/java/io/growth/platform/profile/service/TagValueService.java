package io.growth.platform.profile.service;

import io.growth.platform.common.core.exception.BizException;
import io.growth.platform.common.core.exception.CommonErrorCode;
import io.growth.platform.profile.api.dto.*;
import io.growth.platform.profile.converter.TagValueDTOConverter;
import io.growth.platform.profile.domain.model.TagValue;
import io.growth.platform.profile.domain.repository.TagDefinitionRepository;
import io.growth.platform.profile.domain.repository.TagValueRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TagValueService {

    private final TagValueRepository tagValueRepository;
    private final TagDefinitionRepository tagDefinitionRepository;
    private final TagValueDTOConverter converter = TagValueDTOConverter.INSTANCE;

    public TagValueService(TagValueRepository tagValueRepository, TagDefinitionRepository tagDefinitionRepository) {
        this.tagValueRepository = tagValueRepository;
        this.tagDefinitionRepository = tagDefinitionRepository;
    }

    public void write(TagValueWriteRequest request) {
        validateTagKey(request.getTagKey());
        TagValue tagValue = converter.toDomain(request);
        tagValueRepository.put(tagValue);
    }

    public void batchWrite(TagValueBatchWriteRequest request) {
        request.getItems().forEach(item -> validateTagKey(item.getTagKey()));
        List<TagValue> tagValues = request.getItems().stream().map(converter::toDomain).toList();
        tagValueRepository.putBatch(tagValues);
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
    }

    private void validateTagKey(String tagKey) {
        if (!tagDefinitionRepository.existsByTagKey(tagKey)) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "标签定义不存在: " + tagKey);
        }
    }
}
