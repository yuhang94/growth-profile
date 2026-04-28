package io.growth.platform.profile.domain.repository;

import io.growth.platform.profile.api.enums.SourceType;
import io.growth.platform.profile.api.enums.UsageChannel;
import io.growth.platform.profile.domain.model.BehaviorEventDefinition;

import java.util.List;
import java.util.Optional;

public interface EventDefinitionRepository {

    void insert(BehaviorEventDefinition definition);

    void update(BehaviorEventDefinition definition);

    Optional<BehaviorEventDefinition> findByEventName(String eventName);

    List<BehaviorEventDefinition> findByEventType(String eventType, int pageNum, int pageSize);

    long countByEventType(String eventType);

    List<BehaviorEventDefinition> findPage(String eventType, UsageChannel usageChannel, int pageNum, int pageSize);

    long count(String eventType, UsageChannel usageChannel);

    boolean existsByEventName(String eventName);

    List<BehaviorEventDefinition> findAllBySourceTypeAndStatus(SourceType sourceType, Integer status);
}
