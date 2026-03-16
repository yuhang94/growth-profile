package io.growth.platform.profile;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.clickhouse.ClickHouseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import com.clickhouse.jdbc.ClickHouseDataSource;
import java.sql.SQLException;
import java.util.Properties;

@Testcontainers
public abstract class BaseClickHouseTest {

    @Container
    static final ClickHouseContainer CLICKHOUSE = new ClickHouseContainer("clickhouse/clickhouse-server:24.3");

    @DynamicPropertySource
    static void clickHouseProperties(DynamicPropertyRegistry registry) {
        registry.add("clickhouse.url", CLICKHOUSE::getJdbcUrl);
        registry.add("clickhouse.username", CLICKHOUSE::getUsername);
        registry.add("clickhouse.password", CLICKHOUSE::getPassword);
    }

    protected static JdbcTemplate createClickHouseJdbcTemplate() {
        try {
            Properties props = new Properties();
            props.setProperty("user", CLICKHOUSE.getUsername());
            props.setProperty("password", CLICKHOUSE.getPassword());
            DataSource ds = new ClickHouseDataSource(CLICKHOUSE.getJdbcUrl(), props);
            return new JdbcTemplate(ds);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create ClickHouse JdbcTemplate", e);
        }
    }

    protected static void initClickHouseTables(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS gp_profile_tag_wide (
                    user_id      String,
                    tag_key      String,
                    tag_value    String,
                    updated_time DateTime DEFAULT now()
                ) ENGINE = ReplacingMergeTree(updated_time)
                ORDER BY (user_id, tag_key)
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS gp_profile_behavior_event (
                    event_id     String,
                    user_id      String,
                    event_name   String,
                    event_type   String,
                    properties   String,
                    event_time   DateTime,
                    created_time DateTime DEFAULT now()
                ) ENGINE = MergeTree()
                PARTITION BY toYYYYMM(event_time)
                ORDER BY (user_id, event_name, event_time)
                """);
    }
}
