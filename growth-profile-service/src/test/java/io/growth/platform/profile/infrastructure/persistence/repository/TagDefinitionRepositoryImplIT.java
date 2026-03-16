package io.growth.platform.profile.infrastructure.persistence.repository;

import io.growth.platform.profile.BaseMyBatisTest;
import io.growth.platform.profile.api.enums.TagType;
import io.growth.platform.profile.domain.model.TagDefinition;
import io.growth.platform.profile.domain.repository.TagDefinitionRepository;
import io.growth.platform.profile.infrastructure.persistence.mapper.TagDefinitionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TagDefinitionRepositoryImplIT extends BaseMyBatisTest {

    @Autowired
    private TagDefinitionRepository repository;

    @Autowired
    private TagDefinitionMapper mapper;

    @BeforeEach
    void cleanUp() {
        mapper.delete(null);
    }

    @Test
    void insertAndFindByTagKey() {
        TagDefinition td = newTagDefinition("age", "年龄", TagType.LONG, "基础属性");

        repository.insert(td);

        assertNotNull(td.getId());
        Optional<TagDefinition> found = repository.findByTagKey("age");
        assertTrue(found.isPresent());
        assertEquals("年龄", found.get().getTagName());
        assertEquals(TagType.LONG, found.get().getTagType());
        assertEquals("基础属性", found.get().getCategory());
    }

    @Test
    void update() {
        TagDefinition td = newTagDefinition("gender", "性别", TagType.ENUM, "基础属性");
        repository.insert(td);

        td.setTagName("用户性别");
        td.setDescription("用户的性别");
        repository.update(td);

        TagDefinition updated = repository.findByTagKey("gender").orElseThrow();
        assertEquals("用户性别", updated.getTagName());
        assertEquals("用户的性别", updated.getDescription());
    }

    @Test
    void existsByTagKey() {
        assertFalse(repository.existsByTagKey("city"));

        repository.insert(newTagDefinition("city", "城市", TagType.STRING, "地域属性"));

        assertTrue(repository.existsByTagKey("city"));
    }

    @Test
    void findByCategoryAndPagination() {
        repository.insert(newTagDefinition("age", "年龄", TagType.LONG, "基础属性"));
        repository.insert(newTagDefinition("gender", "性别", TagType.ENUM, "基础属性"));
        repository.insert(newTagDefinition("city", "城市", TagType.STRING, "地域属性"));

        List<TagDefinition> basicPage1 = repository.findByCategory("基础属性", 1, 10);
        assertEquals(2, basicPage1.size());

        List<TagDefinition> locationPage1 = repository.findByCategory("地域属性", 1, 10);
        assertEquals(1, locationPage1.size());

        long basicCount = repository.countByCategory("基础属性");
        assertEquals(2, basicCount);

        List<TagDefinition> allPage = repository.findByCategory(null, 1, 10);
        assertEquals(3, allPage.size());
    }

    private TagDefinition newTagDefinition(String tagKey, String tagName, TagType tagType, String category) {
        TagDefinition td = new TagDefinition();
        td.setTagKey(tagKey);
        td.setTagName(tagName);
        td.setTagType(tagType);
        td.setCategory(category);
        td.setStatus(1);
        return td;
    }
}
