package io.growth.platform.profile.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.growth.platform.profile.domain.model.SegmentTemplate;
import io.growth.platform.profile.domain.repository.SegmentTemplateRepository;
import io.growth.platform.profile.infrastructure.persistence.converter.SegmentTemplateConverter;
import io.growth.platform.profile.infrastructure.persistence.dataobject.SegmentTemplateDO;
import io.growth.platform.profile.infrastructure.persistence.mapper.SegmentTemplateMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SegmentTemplateRepositoryImpl implements SegmentTemplateRepository {

    private final SegmentTemplateMapper mapper;
    private final SegmentTemplateConverter converter;

    public SegmentTemplateRepositoryImpl(SegmentTemplateMapper mapper, SegmentTemplateConverter converter) {
        this.mapper = mapper;
        this.converter = converter;
    }

    @Override
    public void insert(SegmentTemplate template) {
        SegmentTemplateDO dataObject = converter.toDataObject(template);
        mapper.insert(dataObject);
        template.setId(dataObject.getId());
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public Optional<SegmentTemplate> findById(Long id) {
        SegmentTemplateDO dataObject = mapper.selectById(id);
        return Optional.ofNullable(dataObject).map(converter::toDomain);
    }

    @Override
    public Optional<SegmentTemplate> findByTemplateKey(String templateKey) {
        LambdaQueryWrapper<SegmentTemplateDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SegmentTemplateDO::getTemplateKey, templateKey);
        SegmentTemplateDO dataObject = mapper.selectOne(wrapper);
        return Optional.ofNullable(dataObject).map(converter::toDomain);
    }

    @Override
    public List<SegmentTemplate> findAllOrderBySortOrder() {
        LambdaQueryWrapper<SegmentTemplateDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SegmentTemplateDO::getSortOrder);
        return mapper.selectList(wrapper).stream().map(converter::toDomain).toList();
    }
}
