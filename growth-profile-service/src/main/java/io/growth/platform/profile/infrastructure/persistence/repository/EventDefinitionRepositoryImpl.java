package io.growth.platform.profile.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.growth.platform.profile.domain.model.BehaviorEventDefinition;
import io.growth.platform.profile.domain.repository.EventDefinitionRepository;
import io.growth.platform.profile.infrastructure.persistence.converter.EventDefinitionConverter;
import io.growth.platform.profile.infrastructure.persistence.dataobject.EventDefinitionDO;
import io.growth.platform.profile.infrastructure.persistence.mapper.EventDefinitionMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EventDefinitionRepositoryImpl implements EventDefinitionRepository {

    private final EventDefinitionMapper mapper;
    private final EventDefinitionConverter converter = EventDefinitionConverter.INSTANCE;

    public EventDefinitionRepositoryImpl(EventDefinitionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void insert(BehaviorEventDefinition definition) {
        EventDefinitionDO dataObject = converter.toDataObject(definition);
        mapper.insert(dataObject);
        definition.setId(dataObject.getId());
    }

    @Override
    public void update(BehaviorEventDefinition definition) {
        EventDefinitionDO dataObject = converter.toDataObject(definition);
        mapper.updateById(dataObject);
    }

    @Override
    public Optional<BehaviorEventDefinition> findByEventName(String eventName) {
        LambdaQueryWrapper<EventDefinitionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EventDefinitionDO::getEventName, eventName);
        EventDefinitionDO dataObject = mapper.selectOne(wrapper);
        return Optional.ofNullable(dataObject).map(converter::toDomain);
    }

    @Override
    public List<BehaviorEventDefinition> findByEventType(String eventType, int pageNum, int pageSize) {
        Page<EventDefinitionDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<EventDefinitionDO> wrapper = new LambdaQueryWrapper<>();
        if (eventType != null && !eventType.isBlank()) {
            wrapper.eq(EventDefinitionDO::getEventType, eventType);
        }
        wrapper.orderByDesc(EventDefinitionDO::getId);
        mapper.selectPage(page, wrapper);
        return page.getRecords().stream().map(converter::toDomain).toList();
    }

    @Override
    public long countByEventType(String eventType) {
        LambdaQueryWrapper<EventDefinitionDO> wrapper = new LambdaQueryWrapper<>();
        if (eventType != null && !eventType.isBlank()) {
            wrapper.eq(EventDefinitionDO::getEventType, eventType);
        }
        return mapper.selectCount(wrapper);
    }

    @Override
    public boolean existsByEventName(String eventName) {
        LambdaQueryWrapper<EventDefinitionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EventDefinitionDO::getEventName, eventName);
        return mapper.selectCount(wrapper) > 0;
    }
}
