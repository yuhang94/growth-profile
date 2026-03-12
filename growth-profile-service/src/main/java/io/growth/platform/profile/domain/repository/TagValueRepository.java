package io.growth.platform.profile.domain.repository;

import io.growth.platform.profile.domain.model.TagValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TagValueRepository {

    void put(TagValue tagValue);

    void putBatch(List<TagValue> tagValues);

    Optional<String> get(String userId, String tagKey);

    Map<String, String> getUserTags(String userId);

    void delete(String userId, String tagKey);
}
