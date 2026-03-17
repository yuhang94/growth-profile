package io.growth.platform.profile;

import org.apache.hadoop.hbase.client.Connection;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.clickhouse.ClickHouseContainer;
import org.testcontainers.containers.MySQLContainer;

/**
 * 冒烟测试：验证完整 Spring 上下文能正常启动，
 * 确保不存在 bean 冲突（如多个 DataSource）。
 */
@SpringBootTest
@ActiveProfiles("it")
class ApplicationContextIT {

    static final MySQLContainer<?> MYSQL;
    static final ClickHouseContainer CLICKHOUSE;

    static {
        MYSQL = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("growth_profile_test")
                .withUsername("test")
                .withPassword("test")
                .withInitScript("sql/schema.sql");
        MYSQL.start();

        CLICKHOUSE = new ClickHouseContainer("clickhouse/clickhouse-server:24.3");
        CLICKHOUSE.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("clickhouse.url", CLICKHOUSE::getJdbcUrl);
        registry.add("clickhouse.username", CLICKHOUSE::getUsername);
        registry.add("clickhouse.password", CLICKHOUSE::getPassword);
    }

    @MockitoBean
    Connection hbaseConnection;

    @MockitoBean
    RocketMQTemplate rocketMQTemplate;

    @Test
    void contextLoads() {
        // 完整上下文能启动即表明没有 bean 冲突
    }
}
