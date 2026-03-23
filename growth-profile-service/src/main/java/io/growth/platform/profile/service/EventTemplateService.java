package io.growth.platform.profile.service;

import io.growth.platform.common.core.exception.BizException;
import io.growth.platform.common.core.exception.CommonErrorCode;
import io.growth.platform.profile.api.dto.EventTemplateCreateRequest;
import io.growth.platform.profile.api.dto.EventTemplateDTO;
import io.growth.platform.profile.api.dto.EventTemplateUpdateRequest;
import io.growth.platform.profile.api.enums.EventType;
import io.growth.platform.profile.converter.EventTemplateDTOConverter;
import io.growth.platform.profile.domain.model.EventTemplate;
import io.growth.platform.profile.domain.repository.EventTemplateRepository;
import io.growth.platform.profile.domain.template.BuiltInTemplateProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventTemplateService {

    private final EventTemplateRepository eventTemplateRepository;
    private final BuiltInTemplateProvider builtInTemplateProvider;
    private final EventTemplateDTOConverter converter = EventTemplateDTOConverter.INSTANCE;

    public EventTemplateService(EventTemplateRepository eventTemplateRepository,
                                BuiltInTemplateProvider builtInTemplateProvider) {
        this.eventTemplateRepository = eventTemplateRepository;
        this.builtInTemplateProvider = builtInTemplateProvider;
    }

    public List<EventTemplateDTO> getByEventType(EventType eventType) {
        List<EventTemplate> customTemplates = eventTemplateRepository.findByEventType(eventType);
        if (!customTemplates.isEmpty()) {
            return customTemplates.stream().map(converter::toDTO).toList();
        }
        return builtInTemplateProvider.getTemplates(eventType);
    }

    public EventTemplateDTO create(EventTemplateCreateRequest request) {
        EventTemplate domain = converter.toDomain(request);
        eventTemplateRepository.insert(domain);
        return converter.toDTO(domain);
    }

    public EventTemplateDTO update(Long id, EventTemplateUpdateRequest request) {
        EventTemplate domain = eventTemplateRepository.findById(id)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "模版不存在: " + id));
        converter.updateDomain(request, domain);
        eventTemplateRepository.update(domain);
        return converter.toDTO(domain);
    }

    public void delete(Long id) {
        eventTemplateRepository.findById(id)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "模版不存在: " + id));
        eventTemplateRepository.deleteById(id);
    }
}
