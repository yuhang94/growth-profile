package io.growth.platform.profile.converter;

import io.growth.platform.profile.api.dto.TagValueDTO;
import io.growth.platform.profile.api.dto.TagValueWriteRequest;
import io.growth.platform.profile.api.dto.UserTagsDTO;
import io.growth.platform.profile.domain.model.TagValue;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Map;

@Mapper
public interface TagValueDTOConverter {

    TagValueDTOConverter INSTANCE = Mappers.getMapper(TagValueDTOConverter.class);

    TagValue toDomain(TagValueWriteRequest request);

    TagValueDTO toDTO(TagValue domain);

    default UserTagsDTO toUserTagsDTO(String userId, Map<String, String> tags) {
        UserTagsDTO dto = new UserTagsDTO();
        dto.setUserId(userId);
        dto.setTags(tags);
        return dto;
    }
}
