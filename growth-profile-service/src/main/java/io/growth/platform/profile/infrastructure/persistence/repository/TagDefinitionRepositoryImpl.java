package io.growth.platform.profile.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.growth.platform.profile.domain.model.TagDefinition;
import io.growth.platform.profile.domain.repository.TagDefinitionRepository;
import io.growth.platform.profile.infrastructure.persistence.converter.TagDefinitionConverter;
import io.growth.platform.profile.infrastructure.persistence.dataobject.TagDefinitionDO;
import io.growth.platform.profile.infrastructure.persistence.mapper.TagDefinitionMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TagDefinitionRepositoryImpl implements TagDefinitionRepository {

    private final TagDefinitionMapper mapper;
    private final TagDefinitionConverter converter = TagDefinitionConverter.INSTANCE;

    public TagDefinitionRepositoryImpl(TagDefinitionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void insert(TagDefinition tagDefinition) {
        TagDefinitionDO dataObject = converter.toDataObject(tagDefinition);
        mapper.insert(dataObject);
        tagDefinition.setId(dataObject.getId());
    }

    @Override
    public void update(TagDefinition tagDefinition) {
        TagDefinitionDO dataObject = converter.toDataObject(tagDefinition);
        mapper.updateById(dataObject);
    }

    @Override
    public Optional<TagDefinition> findByTagKey(String tagKey) {
        LambdaQueryWrapper<TagDefinitionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TagDefinitionDO::getTagKey, tagKey);
        TagDefinitionDO dataObject = mapper.selectOne(wrapper);
        return Optional.ofNullable(dataObject).map(converter::toDomain);
    }

    @Override
    public List<TagDefinition> findByCategory(String category, int pageNum, int pageSize) {
        LambdaQueryWrapper<TagDefinitionDO> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isBlank()) {
            wrapper.eq(TagDefinitionDO::getCategory, category);
        }
        wrapper.orderByDesc(TagDefinitionDO::getId);
        Page<TagDefinitionDO> page = mapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return page.getRecords().stream().map(converter::toDomain).toList();
    }

    @Override
    public long countByCategory(String category) {
        LambdaQueryWrapper<TagDefinitionDO> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isBlank()) {
            wrapper.eq(TagDefinitionDO::getCategory, category);
        }
        return mapper.selectCount(wrapper);
    }

    @Override
    public boolean existsByTagKey(String tagKey) {
        LambdaQueryWrapper<TagDefinitionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TagDefinitionDO::getTagKey, tagKey);
        return mapper.selectCount(wrapper) > 0;
    }
}
