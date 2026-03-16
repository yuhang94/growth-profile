package io.growth.platform.profile.infrastructure.clickhouse;

import io.growth.platform.profile.BaseClickHouseTest;
import io.growth.platform.profile.api.enums.CompareOperator;
import io.growth.platform.profile.api.enums.ConditionOperator;
import io.growth.platform.profile.domain.model.SegmentCondition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

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
        String sql = "INSERT INTO gp_profile_tag_wide (user_id, tag_key, tag_value) VALUES (?, ?, ?)";
        // user001: age=30, gender=male, city=北京
        jdbcTemplate.update(sql, "user001", "age", "30");
        jdbcTemplate.update(sql, "user001", "gender", "male");
        jdbcTemplate.update(sql, "user001", "city", "北京");
        // user002: age=22, gender=female, city=上海
        jdbcTemplate.update(sql, "user002", "age", "22");
        jdbcTemplate.update(sql, "user002", "gender", "female");
        jdbcTemplate.update(sql, "user002", "city", "上海");
        // user003: age=35, gender=male, city=广州
        jdbcTemplate.update(sql, "user003", "age", "35");
        jdbcTemplate.update(sql, "user003", "gender", "male");
        jdbcTemplate.update(sql, "user003", "city", "广州");
        // user004: age=28, gender=female, city=北京
        jdbcTemplate.update(sql, "user004", "age", "28");
        jdbcTemplate.update(sql, "user004", "gender", "female");
        jdbcTemplate.update(sql, "user004", "city", "北京");
    }

    @Test
    void countUsers_singleCondition() {
        // age > 25 => user001(30), user003(35), user004(28) = 3
        SegmentCondition leaf = leaf("age", CompareOperator.GT, List.of("25"));
        long count = repository.countUsers(leaf);
        assertEquals(3, count);
    }

    @Test
    void countUsers_eqCondition() {
        // gender = male => user001, user003 = 2
        SegmentCondition leaf = leaf("gender", CompareOperator.EQ, List.of("male"));
        long count = repository.countUsers(leaf);
        assertEquals(2, count);
    }

    @Test
    void countUsers_andCondition() {
        // age > 25 AND gender = male => user001(30,male), user003(35,male) = 2
        SegmentCondition ageLeaf = leaf("age", CompareOperator.GT, List.of("25"));
        SegmentCondition genderLeaf = leaf("gender", CompareOperator.EQ, List.of("male"));

        SegmentCondition root = new SegmentCondition();
        root.setOperator(ConditionOperator.AND);
        root.setChildren(List.of(ageLeaf, genderLeaf));

        long count = repository.countUsers(root);
        assertEquals(2, count);
    }

    @Test
    void countUsers_orCondition() {
        // city = 北京 OR city = 上海 => user001, user002, user004 = 3
        SegmentCondition leaf1 = leaf("city", CompareOperator.EQ, List.of("北京"));
        SegmentCondition leaf2 = leaf("city", CompareOperator.EQ, List.of("上海"));

        SegmentCondition root = new SegmentCondition();
        root.setOperator(ConditionOperator.OR);
        root.setChildren(List.of(leaf1, leaf2));

        long count = repository.countUsers(root);
        assertEquals(3, count);
    }

    @Test
    void countUsers_inCondition() {
        // city IN (北京, 上海) => user001, user002, user004 = 3
        SegmentCondition leaf = leaf("city", CompareOperator.IN, List.of("北京", "上海"));
        long count = repository.countUsers(leaf);
        assertEquals(3, count);
    }

    @Test
    void countUsers_betweenCondition() {
        // age BETWEEN 25 AND 30 => user001(30), user004(28) = 2
        SegmentCondition leaf = leaf("age", CompareOperator.BETWEEN, List.of("25", "30"));
        long count = repository.countUsers(leaf);
        assertEquals(2, count);
    }

    @Test
    void queryUsers_withPagination() {
        // gender = male => user001, user003
        SegmentCondition leaf = leaf("gender", CompareOperator.EQ, List.of("male"));

        List<String> page1 = repository.queryUsers(leaf, 1, 1);
        assertEquals(1, page1.size());

        List<String> page2 = repository.queryUsers(leaf, 2, 1);
        assertEquals(1, page2.size());

        assertNotEquals(page1.get(0), page2.get(0));
    }

    @Test
    void countUsers_notCondition() {
        // NOT (gender = male) => user002, user004 = 2
        SegmentCondition genderLeaf = leaf("gender", CompareOperator.EQ, List.of("male"));

        SegmentCondition root = new SegmentCondition();
        root.setOperator(ConditionOperator.NOT);
        root.setChildren(List.of(genderLeaf));

        long count = repository.countUsers(root);
        assertEquals(2, count);
    }

    private SegmentCondition leaf(String tagKey, CompareOperator op, List<String> values) {
        SegmentCondition c = new SegmentCondition();
        c.setTagKey(tagKey);
        c.setCompareOp(op);
        c.setValues(values);
        return c;
    }
}
