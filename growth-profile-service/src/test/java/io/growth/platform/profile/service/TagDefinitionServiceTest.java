package io.growth.platform.profile.service;

import io.growth.platform.common.core.exception.BizException;
import io.growth.platform.common.core.result.PageResult;
import io.growth.platform.profile.api.dto.TagDefinitionCreateRequest;
import io.growth.platform.profile.api.dto.TagDefinitionDTO;
import io.growth.platform.profile.api.dto.TagDefinitionUpdateRequest;
import io.growth.platform.profile.api.enums.TagType;
import io.growth.platform.profile.domain.model.TagDefinition;
import io.growth.platform.profile.domain.repository.TagDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagDefinitionServiceTest {

    @Mock
    private TagDefinitionRepository tagDefinitionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TagDefinitionService tagDefinitionService;

    private TagDefinition sampleDomain;

    @BeforeEach
    void setUp() {
        sampleDomain = new TagDefinition();
        sampleDomain.setId(1L);
        sampleDomain.setTagKey("age");
        sampleDomain.setTagName("年龄");
        sampleDomain.setTagType(TagType.LONG);
        sampleDomain.setCategory("基础属性");
        sampleDomain.setStatus(1);
    }

    @Test
    void create_success() {
        when(tagDefinitionRepository.existsByTagKey("age")).thenReturn(false);
        doAnswer(inv -> {
            TagDefinition td = inv.getArgument(0);
            td.setId(1L);
            return null;
        }).when(tagDefinitionRepository).insert(any());

        TagDefinitionCreateRequest request = new TagDefinitionCreateRequest();
        request.setTagKey("age");
        request.setTagName("年龄");
        request.setTagType(TagType.LONG);
        request.setCategory("基础属性");

        TagDefinitionDTO result = tagDefinitionService.create(request);

        assertNotNull(result);
        assertEquals("age", result.getTagKey());
        assertEquals("年龄", result.getTagName());
        verify(tagDefinitionRepository).insert(any());
    }

    @Test
    void create_duplicateKey_throwsException() {
        when(tagDefinitionRepository.existsByTagKey("age")).thenReturn(true);

        TagDefinitionCreateRequest request = new TagDefinitionCreateRequest();
        request.setTagKey("age");
        request.setTagName("年龄");
        request.setTagType(TagType.LONG);

        assertThrows(BizException.class, () -> tagDefinitionService.create(request));
    }

    @Test
    void getByTagKey_success() {
        when(tagDefinitionRepository.findByTagKey("age")).thenReturn(Optional.of(sampleDomain));

        TagDefinitionDTO result = tagDefinitionService.getByTagKey("age");

        assertEquals("age", result.getTagKey());
        assertEquals("年龄", result.getTagName());
    }

    @Test
    void getByTagKey_notFound_throwsException() {
        when(tagDefinitionRepository.findByTagKey("nonexistent")).thenReturn(Optional.empty());

        assertThrows(BizException.class, () -> tagDefinitionService.getByTagKey("nonexistent"));
    }

    @Test
    void update_success() {
        when(tagDefinitionRepository.findByTagKey("age")).thenReturn(Optional.of(sampleDomain));

        TagDefinitionUpdateRequest request = new TagDefinitionUpdateRequest();
        request.setTagName("用户年龄");
        request.setTagType(TagType.LONG);

        TagDefinitionDTO result = tagDefinitionService.update("age", request);

        assertEquals("用户年龄", result.getTagName());
        verify(tagDefinitionRepository).update(any());
    }

    @Test
    void page_success() {
        when(tagDefinitionRepository.countByCategory(null)).thenReturn(1L);
        when(tagDefinitionRepository.findByCategory(null, 1, 20)).thenReturn(List.of(sampleDomain));

        PageResult<TagDefinitionDTO> result = tagDefinitionService.page(null, 1, 20);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
    }

    @Test
    void updateStatus_success() {
        when(tagDefinitionRepository.findByTagKey("age")).thenReturn(Optional.of(sampleDomain));

        tagDefinitionService.updateStatus("age", 0);

        assertEquals(0, sampleDomain.getStatus());
        verify(tagDefinitionRepository).update(sampleDomain);
    }
}
