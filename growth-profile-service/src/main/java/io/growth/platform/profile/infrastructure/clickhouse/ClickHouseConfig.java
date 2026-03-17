package io.growth.platform.profile.infrastructure.clickhouse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.clickhouse.jdbc.ClickHouseDataSource;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
public class ClickHouseConfig {

    @Value("${clickhouse.url}")
    private String url;

    @Value("${clickhouse.username}")
    private String username;

    @Value("${clickhouse.password}")
    private String password;

    @Bean("clickHouseJdbcTemplate")
    public JdbcTemplate clickHouseJdbcTemplate() throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("user", username);
        properties.setProperty("password", password);
        ClickHouseDataSource dataSource = new ClickHouseDataSource(url, properties);
        return new JdbcTemplate(dataSource);
    }
}
