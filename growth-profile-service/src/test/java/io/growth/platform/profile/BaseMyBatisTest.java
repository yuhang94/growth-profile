package io.growth.platform.profile;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import io.growth.platform.profile.config.MyBatisPlusConfig;
import io.growth.platform.profile.infrastructure.persistence.repository.EventDefinitionRepositoryImpl;
import io.growth.platform.profile.infrastructure.persistence.repository.SegmentRepositoryImpl;
import io.growth.platform.profile.infrastructure.persistence.repository.TagDefinitionRepositoryImpl;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

@SpringBootTest(classes = {
        DataSourceAutoConfiguration.class,
        MybatisPlusAutoConfiguration.class,
        MyBatisPlusConfig.class,
        BaseMyBatisTest.MapperConfig.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("it")
public abstract class BaseMyBatisTest {

    static final MySQLContainer<?> MYSQL;

    static {
        MYSQL = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("growth_profile_test")
                .withUsername("test")
                .withPassword("test")
                .withInitScript("sql/schema.sql");
        MYSQL.start();
    }

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @MapperScan("io.growth.platform.profile.infrastructure.persistence.mapper")
    @Import({
            TagDefinitionRepositoryImpl.class,
            SegmentRepositoryImpl.class,
            EventDefinitionRepositoryImpl.class
    })
    static class MapperConfig {
    }
}
