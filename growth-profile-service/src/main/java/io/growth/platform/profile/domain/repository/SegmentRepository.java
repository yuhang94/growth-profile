package io.growth.platform.profile.domain.repository;

import io.growth.platform.profile.domain.model.Segment;

import java.util.List;
import java.util.Optional;

public interface SegmentRepository {

    void insert(Segment segment);

    void update(Segment segment);

    Optional<Segment> findById(Long id);

    List<Segment> findAll(int pageNum, int pageSize);

    long count();

    void deleteById(Long id);
}
