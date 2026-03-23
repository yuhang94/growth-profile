package io.growth.platform.profile.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.growth.platform.profile.api.enums.EventType;
import io.growth.platform.profile.domain.model.EventTemplate;
import io.growth.platform.profile.domain.repository.EventTemplateRepository;
import io.growth.platform.profile.infrastructure.persistence.converter.EventTemplateConverter;
import io.growth.platform.profile.infrastructure.persistence.dataobject.EventTemplateDO;
import io.growth.platform.profile.infrastructure.persistence.mapper.EventTemplateMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EventTemplateRepositoryImpl implements EventTemplateRepository {

    private final EventTemplateMapper mapper;
    private final EventTemplateConverter converter = EventTemplateConverter.INSTANCE;

    public EventTemplateRepositoryImpl(EventTemplateMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void insert(EventTemplate template) {
        EventTemplateDO dataObject = converter.toDataObject(template);
        mapper.insert(dataObject);
        template.setId(dataObject.getId());
    }

    @Override
    public void update(EventTemplate template) {
        EventTemplateDO dataObject = converter.toDataObject(template);
        mapper.updateById(dataObject);
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public Optional<EventTemplate> findById(Long id) {
        EventTemplateDO dataObject = mapper.selectById(id);
        return Optional.ofNullable(dataObject).map(converter::toDomain);
    }

    @Override
    public List<EventTemplate> findByEventType(EventType eventType) {
        LambdaQueryWrapper<EventTemplateDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EventTemplateDO::getEventType, eventType.name());
        wrapper.orderByDesc(EventTemplateDO::getId);
        return mapper.selectList(wrapper).stream().map(converter::toDomain).toList();
    }
}
