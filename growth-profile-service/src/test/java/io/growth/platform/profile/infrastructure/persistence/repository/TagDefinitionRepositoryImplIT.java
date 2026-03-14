package io.growth.platform.profile.infrastructure.persistence.repository;

import io.growth.platform.profile.api.enums.TagType;
import io.growth.platform.profile.domain.model.TagDefinition;
import org.apache.hadoop.hbase.client.Connection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("it")
class TagDefinitionRepositoryImplIT {

    @MockBean
    Connection hbaseConnection;

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("growth_profile_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @BeforeAll
    static void initSchema(@Autowired DataSource dataSource) throws SQLException {
        try (java.sql.Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/schema.sql"));
        }
    }

    @Autowired
    private TagDefinitionRepositoryImpl repository;

    @Test
    void insert_and_findByTagKey() {
        TagDefinition tag = buildTag("user_level", "用户等级", TagType.ENUM, "user");
        tag.setEnumValues(List.of("GOLD", "SILVER", "BRONZE"));

        repository.insert(tag);
        assertNotNull(tag.getId());

        Optional<TagDefinition> found = repository.findByTagKey("user_level");
        assertTrue(found.isPresent());
        assertEquals("用户等级", found.get().getTagName());
        assertEquals(TagType.ENUM, found.get().getTagType());
        assertEquals(List.of("GOLD", "SILVER", "BRONZE"), found.get().getEnumValues());
    }

    @Test
    void update_modifiesFields() {
        TagDefinition tag = buildTag("user_age", "用户年龄", TagType.LONG, "user");
        repository.insert(tag);

        tag.setTagName("用户年龄段");
        tag.setDescription("年龄段分类");
        repository.update(tag);

        Optional<TagDefinition> updated = repository.findByTagKey("user_age");
        assertTrue(updated.isPresent());
        assertEquals("用户年龄段", updated.get().getTagName());
        assertEquals("年龄段分类", updated.get().getDescription());
    }

    @Test
    void findByCategory_withPagination() {
        for (int i = 0; i < 5; i++) {
            repository.insert(buildTag("page_tag_" + i, "分页标签" + i, TagType.STRING, "page_test"));
        }

        List<TagDefinition> page1 = repository.findByCategory("page_test", 1, 3);
        assertEquals(3, page1.size());

        List<TagDefinition> page2 = repository.findByCategory("page_test", 2, 3);
        assertEquals(2, page2.size());
    }

    @Test
    void countByCategory() {
        String category = "count_test";
        for (int i = 0; i < 3; i++) {
            repository.insert(buildTag("count_tag_" + i, "计数标签" + i, TagType.STRING, category));
        }

        long count = repository.countByCategory(category);
        assertEquals(3, count);
    }

    @Test
    void existsByTagKey() {
        repository.insert(buildTag("exists_tag", "存在性测试", TagType.BOOLEAN, "misc"));

        assertTrue(repository.existsByTagKey("exists_tag"));
        assertFalse(repository.existsByTagKey("non_existent_tag"));
    }

    private TagDefinition buildTag(String tagKey, String tagName, TagType tagType, String category) {
        TagDefinition tag = new TagDefinition();
        tag.setTagKey(tagKey);
        tag.setTagName(tagName);
        tag.setTagType(tagType);
        tag.setCategory(category);
        tag.setStatus(1);
        tag.setCreatedBy("test");
        return tag;
    }
}
