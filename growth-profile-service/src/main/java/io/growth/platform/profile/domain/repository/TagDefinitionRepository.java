package io.growth.platform.profile.domain.repository;

import io.growth.platform.profile.domain.model.TagDefinition;

import java.util.List;
import java.util.Optional;

public interface TagDefinitionRepository {

    void insert(TagDefinition tagDefinition);

    void update(TagDefinition tagDefinition);

    Optional<TagDefinition> findByTagKey(String tagKey);

    List<TagDefinition> findByCategory(String category, int pageNum, int pageSize);

    long countByCategory(String category);

    boolean existsByTagKey(String tagKey);
}
