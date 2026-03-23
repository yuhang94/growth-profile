package io.growth.platform.profile.domain.repository;

import io.growth.platform.profile.domain.model.SegmentTemplate;

import java.util.List;
import java.util.Optional;

public interface SegmentTemplateRepository {

    void insert(SegmentTemplate template);

    void deleteById(Long id);

    Optional<SegmentTemplate> findById(Long id);

    Optional<SegmentTemplate> findByTemplateKey(String templateKey);

    List<SegmentTemplate> findAllOrderBySortOrder();
}
