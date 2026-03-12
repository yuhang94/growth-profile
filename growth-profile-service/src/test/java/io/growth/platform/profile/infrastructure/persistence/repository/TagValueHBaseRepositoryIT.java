package io.growth.platform.profile.infrastructure.persistence.repository;

import io.growth.platform.profile.domain.model.TagValue;
import io.growth.platform.profile.infrastructure.hbase.HBaseClientConfig;
import io.growth.platform.profile.infrastructure.hbase.HBaseTemplate;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(classes = {
        HBaseClientConfig.class,
        HBaseTemplate.class,
        TagValueHBaseRepository.class
})
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@ActiveProfiles("it")
class TagValueHBaseRepositoryIT {

    @Container
    static GenericContainer<?> hbase = new GenericContainer<>(DockerImageName.parse("dajobe/hbase:1.1.2"))
            .withExposedPorts(2181, 16010, 16020, 16201)
            .withCommand("/opt/hbase/bin/start-hbase.sh && tail -f /opt/hbase/logs/*")
            .withStartupTimeout(java.time.Duration.ofMinutes(2));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("hbase.zookeeper.quorum", hbase::getHost);
        registry.add("hbase.zookeeper.port", () -> hbase.getMappedPort(2181));
    }

    @BeforeAll
    static void createTable(@Autowired Connection hbaseConnection) throws IOException {
        try (Admin admin = hbaseConnection.getAdmin()) {
            TableName tableName = TableName.valueOf("gp_profile_tag_value");
            if (!admin.tableExists(tableName)) {
                @SuppressWarnings("deprecation")
                HTableDescriptor tableDesc = new HTableDescriptor(tableName);
                tableDesc.addFamily(new HColumnDescriptor("t"));
                admin.createTable(tableDesc);
            }
        }
    }

    @Autowired
    private TagValueHBaseRepository repository;

    @Test
    void put_and_get() {
        TagValue tv = new TagValue("user_001", "vip_level", "GOLD");
        repository.put(tv);

        Optional<String> value = repository.get("user_001", "vip_level");
        assertTrue(value.isPresent());
        assertEquals("GOLD", value.get());
    }

    @Test
    void putBatch_and_getUserTags() {
        List<TagValue> batch = List.of(
                new TagValue("user_002", "age", "25"),
                new TagValue("user_002", "city", "Shanghai"),
                new TagValue("user_002", "gender", "M")
        );
        repository.putBatch(batch);

        Map<String, String> tags = repository.getUserTags("user_002");
        assertEquals(3, tags.size());
        assertEquals("25", tags.get("age"));
        assertEquals("Shanghai", tags.get("city"));
        assertEquals("M", tags.get("gender"));
    }

    @Test
    void get_nonExistent_returnsEmpty() {
        Optional<String> value = repository.get("non_existent_user", "some_tag");
        assertTrue(value.isEmpty());
    }

    @Test
    void delete_removesValue() {
        TagValue tv = new TagValue("user_003", "temp_tag", "temp_value");
        repository.put(tv);

        Optional<String> before = repository.get("user_003", "temp_tag");
        assertTrue(before.isPresent());

        repository.delete("user_003", "temp_tag");

        Optional<String> after = repository.get("user_003", "temp_tag");
        assertTrue(after.isEmpty());
    }

    @Test
    void getUserTags_emptyUser_returnsEmptyMap() {
        Map<String, String> tags = repository.getUserTags("empty_user_999");
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
    }
}
