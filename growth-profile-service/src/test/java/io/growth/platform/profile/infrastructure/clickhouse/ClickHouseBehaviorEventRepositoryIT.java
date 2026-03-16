package io.growth.platform.profile.infrastructure.clickhouse;

import io.growth.platform.profile.BaseClickHouseTest;
import io.growth.platform.profile.domain.model.BehaviorEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClickHouseBehaviorEventRepositoryIT extends BaseClickHouseTest {

    static JdbcTemplate jdbcTemplate;
    static ClickHouseBehaviorEventRepository repository;

    @BeforeAll
    static void setUp() {
        jdbcTemplate = createClickHouseJdbcTemplate();
        initClickHouseTables(jdbcTemplate);
        repository = new ClickHouseBehaviorEventRepository(jdbcTemplate);
    }

    @Test
    void insertAndQueryByUserId() {
        BehaviorEvent event = newEvent("user100", "page_view", "PAGE_VIEW",
                Map.of("page", "/home"), LocalDateTime.of(2024, 6, 1, 10, 0));

        repository.insert(event);

        List<BehaviorEvent> events = repository.queryByUserId("user100", 10);
        assertEquals(1, events.size());
        assertEquals("page_view", events.get(0).getEventName());
        assertEquals("PAGE_VIEW", events.get(0).getEventType());
        assertEquals("/home", events.get(0).getProperties().get("page"));
    }

    @Test
    void insertBatch() {
        BehaviorEvent e1 = newEvent("user200", "click", "CLICK",
                Map.of("button", "buy"), LocalDateTime.of(2024, 6, 1, 11, 0));
        BehaviorEvent e2 = newEvent("user200", "click", "CLICK",
                Map.of("button", "share"), LocalDateTime.of(2024, 6, 1, 11, 5));
        BehaviorEvent e3 = newEvent("user200", "page_view", "PAGE_VIEW",
                Map.of("page", "/product"), LocalDateTime.of(2024, 6, 1, 11, 10));

        repository.insertBatch(List.of(e1, e2, e3));

        List<BehaviorEvent> events = repository.queryByUserId("user200", 10);
        assertEquals(3, events.size());
    }

    @Test
    void queryWithFilters() {
        String userId = "user300";
        repository.insert(newEvent(userId, "page_view", "PAGE_VIEW",
                Map.of(), LocalDateTime.of(2024, 7, 1, 10, 0)));
        repository.insert(newEvent(userId, "click", "CLICK",
                Map.of(), LocalDateTime.of(2024, 7, 1, 11, 0)));
        repository.insert(newEvent(userId, "page_view", "PAGE_VIEW",
                Map.of(), LocalDateTime.of(2024, 7, 2, 10, 0)));

        // Filter by eventName
        List<BehaviorEvent> pageViews = repository.query(userId, "page_view", null, null, 1, 10);
        assertEquals(2, pageViews.size());

        // Filter by time range
        List<BehaviorEvent> day1Events = repository.query(userId, null,
                LocalDateTime.of(2024, 7, 1, 0, 0),
                LocalDateTime.of(2024, 7, 1, 23, 59),
                1, 10);
        assertEquals(2, day1Events.size());
    }

    @Test
    void queryPagination() {
        String userId = "user400";
        for (int i = 0; i < 5; i++) {
            repository.insert(newEvent(userId, "click", "CLICK",
                    Map.of(), LocalDateTime.of(2024, 8, 1, 10, i)));
        }

        List<BehaviorEvent> page1 = repository.query(userId, null, null, null, 1, 2);
        assertEquals(2, page1.size());

        List<BehaviorEvent> page2 = repository.query(userId, null, null, null, 2, 2);
        assertEquals(2, page2.size());

        List<BehaviorEvent> page3 = repository.query(userId, null, null, null, 3, 2);
        assertEquals(1, page3.size());
    }

    @Test
    void insertWithEmptyProperties() {
        BehaviorEvent event = newEvent("user500", "login", "LOGIN",
                Map.of(), LocalDateTime.of(2024, 9, 1, 8, 0));

        repository.insert(event);

        List<BehaviorEvent> events = repository.queryByUserId("user500", 10);
        assertEquals(1, events.size());
        assertNotNull(events.get(0).getProperties());
    }

    private BehaviorEvent newEvent(String userId, String eventName, String eventType,
                                   Map<String, String> properties, LocalDateTime eventTime) {
        BehaviorEvent event = new BehaviorEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setUserId(userId);
        event.setEventName(eventName);
        event.setEventType(eventType);
        event.setProperties(properties);
        event.setEventTime(eventTime);
        event.setCreatedTime(LocalDateTime.now());
        return event;
    }
}
