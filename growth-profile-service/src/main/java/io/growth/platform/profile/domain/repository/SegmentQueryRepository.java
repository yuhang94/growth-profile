package io.growth.platform.profile.domain.repository;

import io.growth.platform.profile.domain.model.SegmentCondition;

import java.util.List;

public interface SegmentQueryRepository {

    long countUsers(SegmentCondition rootCondition);

    List<String> queryUsers(SegmentCondition rootCondition, int pageNum, int pageSize);
}
