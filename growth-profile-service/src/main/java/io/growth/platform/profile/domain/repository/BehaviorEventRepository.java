package io.growth.platform.profile.domain.repository;

import io.growth.platform.profile.domain.model.BehaviorEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface BehaviorEventRepository {

    void insert(BehaviorEvent event);

    void insertBatch(List<BehaviorEvent> events);

    List<BehaviorEvent> query(String userId, String eventName, LocalDateTime startTime, LocalDateTime endTime,
                              int pageNum, int pageSize);

    List<BehaviorEvent> queryByUserId(String userId, int limit);
}
