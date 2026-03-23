package io.growth.platform.profile.infrastructure.clickhouse;

import io.growth.platform.profile.BaseClickHouseTest;
import io.growth.platform.profile.api.enums.BehaviorOperator;
import io.growth.platform.profile.api.enums.CompareOperator;
import io.growth.platform.profile.api.enums.ConditionOperator;
import io.growth.platform.profile.domain.model.EventPropertyFilter;
import io.growth.platform.profile.domain.model.SegmentCondition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClickHouseSegmentQueryRepositoryIT extends BaseClickHouseTest {

    static JdbcTemplate jdbcTemplate;
    static ClickHouseSegmentQueryRepository repository;

    @BeforeAll
    static void setUp() {
        jdbcTemplate = createClickHouseJdbcTemplate();
        initClickHouseTables(jdbcTemplate);
        repository = new ClickHouseSegmentQueryRepository(jdbcTemplate);
        insertTestData();
    }

    static void insertTestData() {
        // --- Tag data ---
        String tagSql = "INSERT INTO gp_profile_tag_wide (user_id, tag_key, tag_value) VALUES (?, ?, ?)";
        // user001: age=30, gender=male, city=北京
        jdbcTemplate.update(tagSql, "user001", "age", "30");
        jdbcTemplate.update(tagSql, "user001", "gender", "male");
        jdbcTemplate.update(tagSql, "user001", "city", "北京");
        // user002: age=22, gender=female, city=上海
        jdbcTemplate.update(tagSql, "user002", "age", "22");
        jdbcTemplate.update(tagSql, "user002", "gender", "female");
        jdbcTemplate.update(tagSql, "user002", "city", "上海");
        // user003: age=35, gender=male, city=广州
        jdbcTemplate.update(tagSql, "user003", "age", "35");
        jdbcTemplate.update(tagSql, "user003", "gender", "male");
        jdbcTemplate.update(tagSql, "user003", "city", "广州");
        // user004: age=28, gender=female, city=北京
        jdbcTemplate.update(tagSql, "user004", "age", "28");
        jdbcTemplate.update(tagSql, "user004", "gender", "female");
        jdbcTemplate.update(tagSql, "user004", "city", "北京");

        // --- Behavior event data ---
        String eventSql = "INSERT INTO gp_profile_behavior_event (event_id, user_id, event_name, event_type, properties, event_time) VALUES (?, ?, ?, ?, ?, ?)";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(fmt);
        String threeDaysAgo = LocalDateTime.now().minusDays(3).format(fmt);
        String tenDaysAgo = LocalDateTime.now().minusDays(10).format(fmt);
        String sixtyDaysAgo = LocalDateTime.now().minusDays(60).format(fmt);

        // user001: 3 purchases recently (amount=150,200,50), 1 refund
        jdbcTemplate.update(eventSql, "evt001", "user001", "purchase", "CUSTOM", "{\"amount\":\"150\",\"category\":\"electronics\"}", threeDaysAgo);
        jdbcTemplate.update(eventSql, "evt002", "user001", "purchase", "CUSTOM", "{\"amount\":\"200\",\"category\":\"electronics\"}", threeDaysAgo);
        jdbcTemplate.update(eventSql, "evt003", "user001", "purchase", "CUSTOM", "{\"amount\":\"50\",\"category\":\"food\"}", tenDaysAgo);
        jdbcTemplate.update(eventSql, "evt004", "user001", "refund", "CUSTOM", "{\"amount\":\"50\"}", threeDaysAgo);

        // user002: 1 purchase recently (amount=80)
        jdbcTemplate.update(eventSql, "evt005", "user002", "purchase", "CUSTOM", "{\"amount\":\"80\",\"category\":\"food\"}", threeDaysAgo);

        // user003: 2 purchases recently (amount=300,120), no refund
        jdbcTemplate.update(eventSql, "evt006", "user003", "purchase", "CUSTOM", "{\"amount\":\"300\",\"category\":\"electronics\"}", threeDaysAgo);
        jdbcTemplate.update(eventSql, "evt007", "user003", "purchase", "CUSTOM", "{\"amount\":\"120\",\"category\":\"clothing\"}", tenDaysAgo);

        // user004: 1 purchase 60 days ago (outside last_30d window)
        jdbcTemplate.update(eventSql, "evt008", "user004", "purchase", "CUSTOM", "{\"amount\":\"500\",\"category\":\"electronics\"}", sixtyDaysAgo);
    }

    // ===================== Existing tag condition tests =====================

    @Test
    void countUsers_singleCondition() {
        // age > 25 => user001(30), user003(35), user004(28) = 3
        SegmentCondition leaf = tagLeaf("age", CompareOperator.GT, List.of("25"));
        long count = repository.countUsers(leaf);
        assertEquals(3, count);
    }

    @Test
    void countUsers_eqCondition() {
        // gender = male => user001, user003 = 2
        SegmentCondition leaf = tagLeaf("gender", CompareOperator.EQ, List.of("male"));
        long count = repository.countUsers(leaf);
        assertEquals(2, count);
    }

    @Test
    void countUsers_andCondition() {
        // age > 25 AND gender = male => user001(30,male), user003(35,male) = 2
        SegmentCondition ageLeaf = tagLeaf("age", CompareOperator.GT, List.of("25"));
        SegmentCondition genderLeaf = tagLeaf("gender", CompareOperator.EQ, List.of("male"));

        SegmentCondition root = new SegmentCondition();
        root.setOperator(ConditionOperator.AND);
        root.setChildren(List.of(ageLeaf, genderLeaf));

        long count = repository.countUsers(root);
        assertEquals(2, count);
    }

    @Test
    void countUsers_orCondition() {
        // city = 北京 OR city = 上海 => user001, user002, user004 = 3
        SegmentCondition leaf1 = tagLeaf("city", CompareOperator.EQ, List.of("北京"));
        SegmentCondition leaf2 = tagLeaf("city", CompareOperator.EQ, List.of("上海"));

        SegmentCondition root = new SegmentCondition();
        root.setOperator(ConditionOperator.OR);
        root.setChildren(List.of(leaf1, leaf2));

        long count = repository.countUsers(root);
        assertEquals(3, count);
    }

    @Test
    void countUsers_inCondition() {
        // city IN (北京, 上海) => user001, user002, user004 = 3
        SegmentCondition leaf = tagLeaf("city", CompareOperator.IN, List.of("北京", "上海"));
        long count = repository.countUsers(leaf);
        assertEquals(3, count);
    }

    @Test
    void countUsers_betweenCondition() {
        // age BETWEEN 25 AND 30 => user001(30), user004(28) = 2
        SegmentCondition leaf = tagLeaf("age", CompareOperator.BETWEEN, List.of("25", "30"));
        long count = repository.countUsers(leaf);
        assertEquals(2, count);
    }

    @Test
    void queryUsers_withPagination() {
        // gender = male => user001, user003
        SegmentCondition leaf = tagLeaf("gender", CompareOperator.EQ, List.of("male"));

        List<String> page1 = repository.queryUsers(leaf, 1, 1);
        assertEquals(1, page1.size());

        List<String> page2 = repository.queryUsers(leaf, 2, 1);
        assertEquals(1, page2.size());

        assertNotEquals(page1.get(0), page2.get(0));
    }

    @Test
    void countUsers_notCondition() {
        // NOT (gender = male) => user002, user004 = 2
        SegmentCondition genderLeaf = tagLeaf("gender", CompareOperator.EQ, List.of("male"));

        SegmentCondition root = new SegmentCondition();
        root.setOperator(ConditionOperator.NOT);
        root.setChildren(List.of(genderLeaf));

        long count = repository.countUsers(root);
        assertEquals(2, count);
    }

    // ===================== Behavior condition tests =====================

    @Test
    void countUsers_behaviorDid() {
        // DID purchase in last 30d => user001, user002, user003 = 3 (user004's purchase was 60d ago)
        SegmentCondition cond = behaviorLeaf(BehaviorOperator.DID, "purchase", "last_30d", "now");
        long count = repository.countUsers(cond);
        assertEquals(3, count);
    }

    @Test
    void countUsers_behaviorDidNot() {
        // DID_NOT refund in last 30d => everyone except user001 who had a refund
        // user001 had refund, so DID_NOT refund = user002, user003, user004 = 3
        // But user004 has no events in last 30d, so user004 is not in event table user_ids for last 30d
        // DID_NOT uses NOT IN subquery — users not having refund event
        // user002, user003 have events but no refund; user004 has no recent events so also not in refund subquery
        SegmentCondition cond = behaviorLeaf(BehaviorOperator.DID_NOT, "refund", "last_30d", "now");
        long count = repository.countUsers(cond);
        // From event table: distinct users = user001, user002, user003, user004
        // user001 has refund, so NOT IN = user002, user003, user004 = 3
        assertEquals(3, count);
    }

    @Test
    void countUsers_behaviorWithPropertyFilter() {
        // DID purchase with amount > 100 in last 30d => user001(150,200), user003(300,120) = 2
        EventPropertyFilter filter = new EventPropertyFilter();
        filter.setPropertyKey("amount");
        filter.setCompareOp(CompareOperator.GT);
        filter.setValues(List.of("100"));

        SegmentCondition cond = behaviorLeaf(BehaviorOperator.DID, "purchase", "last_30d", "now");
        cond.setPropertyFilters(List.of(filter));

        long count = repository.countUsers(cond);
        assertEquals(2, count);
    }

    @Test
    void countUsers_behaviorWithCountCondition() {
        // DID purchase >= 2 times in last 30d => user001(3 purchases), user003(2 purchases) = 2
        SegmentCondition cond = behaviorLeaf(BehaviorOperator.DID, "purchase", "last_30d", "now");
        cond.setCountOp(CompareOperator.GE);
        cond.setCountValue(2);

        long count = repository.countUsers(cond);
        assertEquals(2, count);
    }

    @Test
    void countUsers_behaviorWithPropertyAndCountCondition() {
        // DID purchase with amount > 100, >= 2 times in last 30d
        // user001: 2 purchases with amount > 100 (150, 200)
        // user003: 2 purchases with amount > 100 (300, 120)
        EventPropertyFilter filter = new EventPropertyFilter();
        filter.setPropertyKey("amount");
        filter.setCompareOp(CompareOperator.GT);
        filter.setValues(List.of("100"));

        SegmentCondition cond = behaviorLeaf(BehaviorOperator.DID, "purchase", "last_30d", "now");
        cond.setPropertyFilters(List.of(filter));
        cond.setCountOp(CompareOperator.GE);
        cond.setCountValue(2);

        long count = repository.countUsers(cond);
        assertEquals(2, count);
    }

    @Test
    void countUsers_tagAndBehaviorMixed() {
        // gender = male AND DID purchase in last 30d
        // male: user001, user003; purchase in 30d: user001, user002, user003
        // Intersection: user001, user003 = 2
        SegmentCondition tagCond = tagLeaf("gender", CompareOperator.EQ, List.of("male"));
        SegmentCondition behaviorCond = behaviorLeaf(BehaviorOperator.DID, "purchase", "last_30d", "now");

        SegmentCondition root = new SegmentCondition();
        root.setOperator(ConditionOperator.AND);
        root.setChildren(List.of(tagCond, behaviorCond));

        long count = repository.countUsers(root);
        assertEquals(2, count);
    }

    @Test
    void countUsers_pureBehaviorConditions() {
        // DID purchase AND DID_NOT refund in last 30d
        // purchase in 30d: user001, user002, user003
        // refund in 30d: user001
        // purchase AND NOT refund: user002, user003 = 2
        SegmentCondition purchaseCond = behaviorLeaf(BehaviorOperator.DID, "purchase", "last_30d", "now");
        SegmentCondition refundCond = behaviorLeaf(BehaviorOperator.DID_NOT, "refund", "last_30d", "now");

        SegmentCondition root = new SegmentCondition();
        root.setOperator(ConditionOperator.AND);
        root.setChildren(List.of(purchaseCond, refundCond));

        long count = repository.countUsers(root);
        assertEquals(2, count);
    }

    @Test
    void countUsers_behaviorWithAbsoluteTimeRange() {
        // DID purchase with absolute time range covering all data
        String startDate = LocalDateTime.now().minusDays(90).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endDate = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        SegmentCondition cond = behaviorLeaf(BehaviorOperator.DID, "purchase", startDate, endDate);
        long count = repository.countUsers(cond);
        // All 4 users have purchase events (including user004's 60-day-ago purchase)
        assertEquals(4, count);
    }

    @Test
    void countUsers_behaviorPropertyFilterCategory() {
        // DID purchase with category = electronics in last 30d
        // user001: 2 electronics purchases, user003: 1 electronics purchase = 2 users
        EventPropertyFilter filter = new EventPropertyFilter();
        filter.setPropertyKey("category");
        filter.setCompareOp(CompareOperator.EQ);
        filter.setValues(List.of("electronics"));

        SegmentCondition cond = behaviorLeaf(BehaviorOperator.DID, "purchase", "last_30d", "now");
        cond.setPropertyFilters(List.of(filter));

        long count = repository.countUsers(cond);
        assertEquals(2, count);
    }

    @Test
    void queryUsers_pureBehavior_withPagination() {
        // DID purchase in last 30d => user001, user002, user003
        SegmentCondition cond = behaviorLeaf(BehaviorOperator.DID, "purchase", "last_30d", "now");

        List<String> page1 = repository.queryUsers(cond, 1, 2);
        assertEquals(2, page1.size());

        List<String> page2 = repository.queryUsers(cond, 2, 2);
        assertEquals(1, page2.size());
    }

    // ===================== Helper methods =====================

    private SegmentCondition tagLeaf(String tagKey, CompareOperator op, List<String> values) {
        SegmentCondition c = new SegmentCondition();
        c.setTagKey(tagKey);
        c.setCompareOp(op);
        c.setValues(values);
        return c;
    }

    private SegmentCondition behaviorLeaf(BehaviorOperator behaviorOp, String eventName,
                                          String timeRangeStart, String timeRangeEnd) {
        SegmentCondition c = new SegmentCondition();
        c.setBehaviorOp(behaviorOp);
        c.setEventName(eventName);
        c.setTimeRangeStart(timeRangeStart);
        c.setTimeRangeEnd(timeRangeEnd);
        return c;
    }
}
