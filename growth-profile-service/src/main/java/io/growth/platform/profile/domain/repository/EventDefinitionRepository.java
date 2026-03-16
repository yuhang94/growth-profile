package io.growth.platform.profile.domain.repository;

import io.growth.platform.profile.domain.model.BehaviorEventDefinition;

import java.util.List;
import java.util.Optional;

public interface EventDefinitionRepository {

    void insert(BehaviorEventDefinition definition);

    void update(BehaviorEventDefinition definition);

    Optional<BehaviorEventDefinition> findByEventName(String eventName);

    List<BehaviorEventDefinition> findByEventType(String eventType, int pageNum, int pageSize);

    long countByEventType(String eventType);

    boolean existsByEventName(String eventName);
}
