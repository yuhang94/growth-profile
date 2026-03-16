package io.growth.platform.profile.infrastructure.persistence.repository;

import io.growth.platform.profile.BaseMyBatisTest;
import io.growth.platform.profile.api.enums.EventType;
import io.growth.platform.profile.domain.model.BehaviorEventDefinition;
import io.growth.platform.profile.domain.model.PropertyDefinition;
import io.growth.platform.profile.domain.repository.EventDefinitionRepository;
import io.growth.platform.profile.infrastructure.persistence.mapper.EventDefinitionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EventDefinitionRepositoryImplIT extends BaseMyBatisTest {

    @Autowired
    private EventDefinitionRepository repository;

    @Autowired
    private EventDefinitionMapper mapper;

    @BeforeEach
    void cleanUp() {
        mapper.delete(null);
    }

    @Test
    void insertAndFindByEventName() {
        BehaviorEventDefinition def = newEventDefinition("page_view", EventType.PAGE_VIEW, "页面浏览");

        repository.insert(def);

        assertNotNull(def.getId());
        Optional<BehaviorEventDefinition> found = repository.findByEventName("page_view");
        assertTrue(found.isPresent());
        assertEquals("页面浏览", found.get().getDisplayName());
        assertEquals(EventType.PAGE_VIEW, found.get().getEventType());
    }

    @Test
    void update() {
        BehaviorEventDefinition def = newEventDefinition("click", EventType.CLICK, "点击");
        repository.insert(def);

        def.setDisplayName("按钮点击");
        def.setDescription("用户点击按钮事件");
        repository.update(def);

        BehaviorEventDefinition updated = repository.findByEventName("click").orElseThrow();
        assertEquals("按钮点击", updated.getDisplayName());
        assertEquals("用户点击按钮事件", updated.getDescription());
    }

    @Test
    void existsByEventName() {
        assertFalse(repository.existsByEventName("order"));

        repository.insert(newEventDefinition("order", EventType.ORDER, "下单"));

        assertTrue(repository.existsByEventName("order"));
    }

    @Test
    void findByEventTypeAndPagination() {
        repository.insert(newEventDefinition("page_view", EventType.PAGE_VIEW, "页面浏览"));
        repository.insert(newEventDefinition("click", EventType.CLICK, "点击"));
        repository.insert(newEventDefinition("click2", EventType.CLICK, "按钮点击"));

        List<BehaviorEventDefinition> clicks = repository.findByEventType("CLICK", 1, 10);
        assertEquals(2, clicks.size());

        long clickCount = repository.countByEventType("CLICK");
        assertEquals(2, clickCount);

        List<BehaviorEventDefinition> all = repository.findByEventType(null, 1, 10);
        assertEquals(3, all.size());

        long allCount = repository.countByEventType(null);
        assertEquals(3, allCount);
    }

    @Test
    void propertiesJsonSerializationRoundTrip() {
        BehaviorEventDefinition def = newEventDefinition("search", EventType.SEARCH, "搜索");

        PropertyDefinition prop = new PropertyDefinition();
        prop.setPropertyName("keyword");
        prop.setPropertyType("STRING");
        prop.setDisplayName("搜索关键词");
        prop.setRequired(true);
        def.setProperties(List.of(prop));

        repository.insert(def);

        BehaviorEventDefinition loaded = repository.findByEventName("search").orElseThrow();
        assertNotNull(loaded.getProperties());
        assertEquals(1, loaded.getProperties().size());
        assertEquals("keyword", loaded.getProperties().get(0).getPropertyName());
        assertEquals("STRING", loaded.getProperties().get(0).getPropertyType());
        assertTrue(loaded.getProperties().get(0).isRequired());
    }

    private BehaviorEventDefinition newEventDefinition(String eventName, EventType eventType, String displayName) {
        BehaviorEventDefinition def = new BehaviorEventDefinition();
        def.setEventName(eventName);
        def.setEventType(eventType);
        def.setDisplayName(displayName);
        def.setStatus(1);
        return def;
    }
}
