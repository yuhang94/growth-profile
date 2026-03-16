package io.growth.platform.profile.infrastructure.clickhouse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.growth.platform.profile.domain.model.BehaviorEvent;
import io.growth.platform.profile.domain.repository.BehaviorEventRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
public class ClickHouseBehaviorEventRepository implements BehaviorEventRepository {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String INSERT_SQL =
            "INSERT INTO gp_profile_behavior_event (event_id, user_id, event_name, event_type, properties, event_time) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate clickHouseJdbcTemplate;

    public ClickHouseBehaviorEventRepository(@Qualifier("clickHouseJdbcTemplate") JdbcTemplate clickHouseJdbcTemplate) {
        this.clickHouseJdbcTemplate = clickHouseJdbcTemplate;
    }

    @Override
    public void insert(BehaviorEvent event) {
        clickHouseJdbcTemplate.update(INSERT_SQL,
                event.getEventId(), event.getUserId(), event.getEventName(),
                event.getEventType(), toJson(event.getProperties()), event.getEventTime());
    }

    @Override
    public void insertBatch(List<BehaviorEvent> events) {
        List<Object[]> batchArgs = new ArrayList<>();
        for (BehaviorEvent event : events) {
            batchArgs.add(new Object[]{
                    event.getEventId(), event.getUserId(), event.getEventName(),
                    event.getEventType(), toJson(event.getProperties()), event.getEventTime()
            });
        }
        clickHouseJdbcTemplate.batchUpdate(INSERT_SQL, batchArgs);
    }

    @Override
    public List<BehaviorEvent> query(String userId, String eventName, LocalDateTime startTime, LocalDateTime endTime,
                                     int pageNum, int pageSize) {
        StringBuilder sql = new StringBuilder(
                "SELECT event_id, user_id, event_name, event_type, properties, event_time, created_time " +
                "FROM gp_profile_behavior_event WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (userId != null && !userId.isBlank()) {
            sql.append(" AND user_id = ?");
            params.add(userId);
        }
        if (eventName != null && !eventName.isBlank()) {
            sql.append(" AND event_name = ?");
            params.add(eventName);
        }
        if (startTime != null) {
            sql.append(" AND event_time >= ?");
            params.add(startTime);
        }
        if (endTime != null) {
            sql.append(" AND event_time <= ?");
            params.add(endTime);
        }

        int offset = (pageNum - 1) * pageSize;
        sql.append(" ORDER BY event_time DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);

        return clickHouseJdbcTemplate.query(sql.toString(), new BehaviorEventRowMapper(), params.toArray());
    }

    @Override
    public List<BehaviorEvent> queryByUserId(String userId, int limit) {
        String sql = "SELECT event_id, user_id, event_name, event_type, properties, event_time, created_time " +
                "FROM gp_profile_behavior_event WHERE user_id = ? ORDER BY event_time DESC LIMIT ?";
        return clickHouseJdbcTemplate.query(sql, new BehaviorEventRowMapper(), userId, limit);
    }

    private String toJson(Map<String, String> properties) {
        if (properties == null || properties.isEmpty()) {
            return "{}";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(properties);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private static class BehaviorEventRowMapper implements RowMapper<BehaviorEvent> {
        @Override
        public BehaviorEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
            BehaviorEvent event = new BehaviorEvent();
            event.setEventId(rs.getString("event_id"));
            event.setUserId(rs.getString("user_id"));
            event.setEventName(rs.getString("event_name"));
            event.setEventType(rs.getString("event_type"));
            event.setProperties(parseJson(rs.getString("properties")));
            event.setEventTime(rs.getTimestamp("event_time").toLocalDateTime());
            event.setCreatedTime(rs.getTimestamp("created_time").toLocalDateTime());
            return event;
        }

        private Map<String, String> parseJson(String json) {
            if (json == null || json.isBlank()) {
                return Collections.emptyMap();
            }
            try {
                return OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                return Collections.emptyMap();
            }
        }
    }
}
