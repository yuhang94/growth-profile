package io.growth.platform.profile.service;

import io.growth.platform.common.core.exception.BizException;
import io.growth.platform.profile.api.dto.TagValueBatchWriteRequest;
import io.growth.platform.profile.api.dto.TagValueDTO;
import io.growth.platform.profile.api.dto.TagValueWriteRequest;
import io.growth.platform.profile.api.dto.UserTagsDTO;
import io.growth.platform.profile.domain.repository.TagDefinitionRepository;
import io.growth.platform.profile.domain.repository.TagValueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagValueServiceTest {

    @Mock
    private TagValueRepository tagValueRepository;

    @Mock
    private TagDefinitionRepository tagDefinitionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TagValueService tagValueService;

    @Test
    void write_success() {
        when(tagDefinitionRepository.existsByTagKey("age")).thenReturn(true);

        TagValueWriteRequest request = new TagValueWriteRequest();
        request.setUserId("user001");
        request.setTagKey("age");
        request.setTagValue("25");

        tagValueService.write(request);

        verify(tagValueRepository).put(any());
    }

    @Test
    void write_tagKeyNotExist_throwsException() {
        when(tagDefinitionRepository.existsByTagKey("nonexistent")).thenReturn(false);

        TagValueWriteRequest request = new TagValueWriteRequest();
        request.setUserId("user001");
        request.setTagKey("nonexistent");
        request.setTagValue("val");

        assertThrows(BizException.class, () -> tagValueService.write(request));
    }

    @Test
    void batchWrite_success() {
        when(tagDefinitionRepository.existsByTagKey(anyString())).thenReturn(true);

        TagValueWriteRequest item1 = new TagValueWriteRequest();
        item1.setUserId("user001");
        item1.setTagKey("age");
        item1.setTagValue("25");

        TagValueWriteRequest item2 = new TagValueWriteRequest();
        item2.setUserId("user001");
        item2.setTagKey("gender");
        item2.setTagValue("male");

        TagValueBatchWriteRequest request = new TagValueBatchWriteRequest();
        request.setItems(List.of(item1, item2));

        tagValueService.batchWrite(request);

        verify(tagValueRepository).putBatch(anyList());
    }

    @Test
    void getTagValue_success() {
        when(tagValueRepository.get("user001", "age")).thenReturn(Optional.of("25"));

        TagValueDTO result = tagValueService.getTagValue("user001", "age");

        assertEquals("user001", result.getUserId());
        assertEquals("age", result.getTagKey());
        assertEquals("25", result.getTagValue());
    }

    @Test
    void getTagValue_notFound_throwsException() {
        when(tagValueRepository.get("user001", "age")).thenReturn(Optional.empty());

        assertThrows(BizException.class, () -> tagValueService.getTagValue("user001", "age"));
    }

    @Test
    void getUserTags_success() {
        when(tagValueRepository.getUserTags("user001")).thenReturn(Map.of("age", "25", "gender", "male"));

        UserTagsDTO result = tagValueService.getUserTags("user001");

        assertEquals("user001", result.getUserId());
        assertEquals(2, result.getTags().size());
        assertEquals("25", result.getTags().get("age"));
    }

    @Test
    void delete_success() {
        tagValueService.delete("user001", "age");

        verify(tagValueRepository).delete("user001", "age");
    }
}
