package io.growth.platform.profile.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.growth.platform.profile.domain.model.Segment;
import io.growth.platform.profile.domain.repository.SegmentRepository;
import io.growth.platform.profile.infrastructure.persistence.converter.SegmentConverter;
import io.growth.platform.profile.infrastructure.persistence.dataobject.SegmentDO;
import io.growth.platform.profile.infrastructure.persistence.mapper.SegmentMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SegmentRepositoryImpl implements SegmentRepository {

    private final SegmentMapper mapper;
    private final SegmentConverter converter = SegmentConverter.INSTANCE;

    public SegmentRepositoryImpl(SegmentMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void insert(Segment segment) {
        SegmentDO dataObject = converter.toDataObject(segment);
        mapper.insert(dataObject);
        segment.setId(dataObject.getId());
    }

    @Override
    public void update(Segment segment) {
        SegmentDO dataObject = converter.toDataObject(segment);
        mapper.updateById(dataObject);
    }

    @Override
    public Optional<Segment> findById(Long id) {
        SegmentDO dataObject = mapper.selectById(id);
        return Optional.ofNullable(dataObject).map(converter::toDomain);
    }

    @Override
    public List<Segment> findAll(int pageNum, int pageSize) {
        Page<SegmentDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SegmentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SegmentDO::getId);
        mapper.selectPage(page, wrapper);
        return page.getRecords().stream().map(converter::toDomain).toList();
    }

    @Override
    public long count() {
        return mapper.selectCount(null);
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }
}
