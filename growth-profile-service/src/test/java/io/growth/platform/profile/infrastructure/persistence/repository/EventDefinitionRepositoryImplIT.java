package io.growth.platform.profile.infrastructure.persistence.repository;

import io.growth.platform.profile.BaseMyBatisTest;
import io.growth.platform.profile.api.dto.FieldMapping;
import io.growth.platform.profile.api.enums.EventType;
import io.growth.platform.profile.api.enums.ExtractStrategy;
import io.growth.platform.profile.api.enums.SourceType;
import io.growth.platform.profile.domain.model.BehaviorEventDefinition;
import io.growth.platform.profile.domain.model.MqSourceConfig;
import io.growth.platform.profile.domain.model.PropertyDefinition;
import io.growth.platform.profile.domain.repository.EventDefinitionRepository;
import io.growth.platform.profile.infrastructure.persistence.dataobject.EventDefinitionDO;
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

    @Test
    void sourceType_defaultsToSdk() {
        BehaviorEventDefinition def = newEventDefinition("sdk_event", EventType.CUSTOM, "SDK事件");
        repository.insert(def);

        BehaviorEventDefinition loaded = repository.findByEventName("sdk_event").orElseThrow();
        assertEquals(SourceType.SDK, loaded.getSourceType());
    }

    @Test
    void sourceType_mqWithConfig_roundTrip() {
        BehaviorEventDefinition def = newEventDefinition("mq_event", EventType.CUSTOM, "MQ事件");
        def.setSourceType(SourceType.MQ);

        MqSourceConfig config = new MqSourceConfig();
        config.setTopic("order-topic");
        config.setTag("pay");
        config.setConsumerGroup("profile-order-cg");

        FieldMapping mapping = new FieldMapping();
        mapping.setTargetField("userId");
        mapping.setStrategy(ExtractStrategy.JSON_PATH);
        mapping.setExpression("$.uid");
        config.setFieldMappings(List.of(mapping));

        def.setMqSourceConfig(config);
        repository.insert(def);

        BehaviorEventDefinition loaded = repository.findByEventName("mq_event").orElseThrow();
        assertEquals(SourceType.MQ, loaded.getSourceType());
        assertNotNull(loaded.getMqSourceConfig());
        assertEquals("order-topic", loaded.getMqSourceConfig().getTopic());
        assertEquals("pay", loaded.getMqSourceConfig().getTag());
        assertEquals("profile-order-cg", loaded.getMqSourceConfig().getConsumerGroup());
        assertEquals(1, loaded.getMqSourceConfig().getFieldMappings().size());
        assertEquals("userId", loaded.getMqSourceConfig().getFieldMappings().get(0).getTargetField());
        assertEquals(ExtractStrategy.JSON_PATH, loaded.getMqSourceConfig().getFieldMappings().get(0).getStrategy());
        assertEquals("$.uid", loaded.getMqSourceConfig().getFieldMappings().get(0).getExpression());
    }

    @Test
    void findByEventType_legacyFieldMappingPayload_ignoresUnknownProperties() {
        EventDefinitionDO dataObject = new EventDefinitionDO();
        dataObject.setEventName("legacy_mq_event");
        dataObject.setEventType(EventType.CUSTOM.name());
        dataObject.setDisplayName("历史MQ事件");
        dataObject.setSourceType(SourceType.MQ.name());
        dataObject.setStatus(1);
        dataObject.setMqSourceConfigJson("""
                {
                  "topic": "order-topic",
                  "tag": "pay",
                  "consumerGroup": "profile-order-cg",
                  "fieldMappings": [
                    {
                      "targetField": "userId",
                      "strategy": "JSON_PATH",
                      "expression": "$.uid",
                      "sourceType": "STRING"
                    }
                  ]
                }
                """);
        mapper.insert(dataObject);

        List<BehaviorEventDefinition> loaded = repository.findByEventType(EventType.CUSTOM.name(), 1, 10);

        assertEquals(1, loaded.size());
        assertEquals("legacy_mq_event", loaded.get(0).getEventName());
        assertNotNull(loaded.get(0).getMqSourceConfig());
        assertEquals(1, loaded.get(0).getMqSourceConfig().getFieldMappings().size());
        assertEquals("userId", loaded.get(0).getMqSourceConfig().getFieldMappings().get(0).getTargetField());
        assertEquals(ExtractStrategy.JSON_PATH, loaded.get(0).getMqSourceConfig().getFieldMappings().get(0).getStrategy());
        assertEquals("$.uid", loaded.get(0).getMqSourceConfig().getFieldMappings().get(0).getExpression());
    }

    @Test
    void findAllBySourceTypeAndStatus() {
        BehaviorEventDefinition sdkDef = newEventDefinition("sdk_event", EventType.CUSTOM, "SDK事件");
        repository.insert(sdkDef);

        BehaviorEventDefinition mqDef = newEventDefinition("mq_event", EventType.CUSTOM, "MQ事件");
        mqDef.setSourceType(SourceType.MQ);
        MqSourceConfig config = new MqSourceConfig();
        config.setTopic("test-topic");
        config.setConsumerGroup("test-cg");
        FieldMapping mapping = new FieldMapping();
        mapping.setTargetField("userId");
        mapping.setStrategy(ExtractStrategy.JSON_PATH);
        mapping.setExpression("$.uid");
        config.setFieldMappings(List.of(mapping));
        mqDef.setMqSourceConfig(config);
        repository.insert(mqDef);

        BehaviorEventDefinition mqDisabled = newEventDefinition("mq_disabled", EventType.CUSTOM, "MQ禁用");
        mqDisabled.setSourceType(SourceType.MQ);
        mqDisabled.setStatus(0);
        mqDisabled.setMqSourceConfig(config);
        repository.insert(mqDisabled);

        List<BehaviorEventDefinition> mqEnabled = repository.findAllBySourceTypeAndStatus(SourceType.MQ, 1);
        assertEquals(1, mqEnabled.size());
        assertEquals("mq_event", mqEnabled.get(0).getEventName());

        List<BehaviorEventDefinition> allMq = repository.findAllBySourceTypeAndStatus(SourceType.MQ, null);
        assertEquals(2, allMq.size());
    }

    private BehaviorEventDefinition newEventDefinition(String eventName, EventType eventType, String displayName) {
        BehaviorEventDefinition def = new BehaviorEventDefinition();
        def.setEventName(eventName);
        def.setEventType(eventType);
        def.setDisplayName(displayName);
        def.setSourceType(SourceType.SDK);
        def.setStatus(1);
        return def;
    }
}
