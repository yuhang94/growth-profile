package io.growth.platform.profile.domain.repository;

import io.growth.platform.profile.api.enums.EventType;
import io.growth.platform.profile.domain.model.EventTemplate;

import java.util.List;
import java.util.Optional;

public interface EventTemplateRepository {

    void insert(EventTemplate template);

    void update(EventTemplate template);

    void deleteById(Long id);

    Optional<EventTemplate> findById(Long id);

    List<EventTemplate> findByEventType(EventType eventType);
}
