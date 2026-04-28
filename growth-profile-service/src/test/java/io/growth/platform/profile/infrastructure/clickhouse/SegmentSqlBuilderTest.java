package io.growth.platform.profile.infrastructure.clickhouse;

import io.growth.platform.profile.api.enums.CompareOperator;
import io.growth.platform.profile.api.enums.ConditionOperator;
import io.growth.platform.profile.domain.model.SegmentCondition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SegmentSqlBuilderTest {

    @Test
    void buildCountSql_singleLeaf() {
        SegmentCondition leaf = leaf("age", CompareOperator.GT, List.of("25"));

        SegmentSqlBuilder builder = new SegmentSqlBuilder();
        String sql = builder.buildCountSql(leaf);
        Object[] params = builder.getParams();

        assertTrue(sql.contains("tag_key = ?"));
        assertTrue(sql.contains("toFloat64OrNull(tag_value) > ?"));
        assertEquals(2, params.length);
        assertEquals("age", params[0]);
        assertEquals(25.0, params[1]);
    }

    @Test
    void buildCountSql_andCondition() {
        SegmentCondition leaf1 = leaf("age", CompareOperator.GT, List.of("25"));
        SegmentCondition leaf2 = leaf("gender", CompareOperator.EQ, List.of("male"));

        SegmentCondition root = new SegmentCondition();
        root.setOperator(ConditionOperator.AND);
        root.setChildren(List.of(leaf1, leaf2));

        SegmentSqlBuilder builder = new SegmentSqlBuilder();
        String sql = builder.buildCountSql(root);
        Object[] params = builder.getParams();

        assertTrue(sql.contains(" AND "));
        assertEquals(4, params.length);
        assertEquals("age", params[0]);
        assertEquals(25.0, params[1]);
        assertEquals("gender", params[2]);
        assertEquals("male", params[3]);
    }

    @Test
    void buildCountSql_orCondition() {
        SegmentCondition leaf1 = leaf("city", CompareOperator.EQ, List.of("北京"));
        SegmentCondition leaf2 = leaf("city", CompareOperator.EQ, List.of("上海"));

        SegmentCondition root = new SegmentCondition();
        root.setOperator(ConditionOperator.OR);
        root.setChildren(List.of(leaf1, leaf2));

        SegmentSqlBuilder builder = new SegmentSqlBuilder();
        String sql = builder.buildCountSql(root);

        assertTrue(sql.contains(" OR "));
    }

    @Test
    void buildCountSql_notCondition() {
        SegmentCondition leaf = leaf("status", CompareOperator.EQ, List.of("inactive"));

        SegmentCondition root = new SegmentCondition();
        root.setOperator(ConditionOperator.NOT);
        root.setChildren(List.of(leaf));

        SegmentSqlBuilder builder = new SegmentSqlBuilder();
        String sql = builder.buildCountSql(root);

        assertTrue(sql.contains("NOT IN"));
    }

    @Test
    void buildCountSql_inOperator() {
        SegmentCondition leaf = leaf("city", CompareOperator.IN, List.of("北京", "上海", "广州"));

        SegmentSqlBuilder builder = new SegmentSqlBuilder();
        String sql = builder.buildCountSql(leaf);
        Object[] params = builder.getParams();

        assertTrue(sql.contains("tag_value IN (?,?,?)"));
        assertEquals(4, params.length);
        assertEquals("city", params[0]);
        assertEquals("北京", params[1]);
        assertEquals("上海", params[2]);
        assertEquals("广州", params[3]);
    }

    @Test
    void buildCountSql_betweenOperator() {
        SegmentCondition leaf = leaf("age", CompareOperator.BETWEEN, List.of("18", "35"));

        SegmentSqlBuilder builder = new SegmentSqlBuilder();
        String sql = builder.buildCountSql(leaf);
        Object[] params = builder.getParams();

        assertTrue(sql.contains("BETWEEN ? AND ?"));
        assertEquals(3, params.length);
        assertEquals("age", params[0]);
        assertEquals(18.0, params[1]);
        assertEquals(35.0, params[2]);
    }

    @Test
    void buildCountSql_isNullOperator() {
        SegmentCondition leaf = leaf("email", CompareOperator.IS_NULL, null);

        SegmentSqlBuilder builder = new SegmentSqlBuilder();
        String sql = builder.buildCountSql(leaf);
        Object[] params = builder.getParams();

        assertTrue(sql.contains("tag_value IS NULL OR tag_value = ''"));
        assertEquals(1, params.length);
    }

    @Test
    void buildQuerySql_withPagination() {
        SegmentCondition leaf = leaf("age", CompareOperator.GT, List.of("25"));

        SegmentSqlBuilder builder = new SegmentSqlBuilder();
        String sql = builder.buildQuerySql(leaf, 20, 10);
        Object[] params = builder.getParams();

        assertTrue(sql.contains("LIMIT ? OFFSET ?"));
        assertTrue(sql.contains("ORDER BY user_id"));
        assertEquals(10, params[params.length - 2]);
        assertEquals(20, params[params.length - 1]);
    }

    @Test
    void buildCountSql_nestedCondition() {
        SegmentCondition ageLeaf = leaf("age", CompareOperator.GT, List.of("25"));
        SegmentCondition genderLeaf = leaf("gender", CompareOperator.EQ, List.of("male"));
        SegmentCondition cityLeaf = leaf("city", CompareOperator.EQ, List.of("北京"));

        SegmentCondition andGroup = new SegmentCondition();
        andGroup.setOperator(ConditionOperator.AND);
        andGroup.setChildren(List.of(ageLeaf, genderLeaf));

        SegmentCondition root = new SegmentCondition();
        root.setOperator(ConditionOperator.OR);
        root.setChildren(List.of(andGroup, cityLeaf));

        SegmentSqlBuilder builder = new SegmentSqlBuilder();
        String sql = builder.buildCountSql(root);

        assertTrue(sql.contains(" OR "));
        assertTrue(sql.contains(" AND "));
        assertNotNull(builder.getParams());
    }

    @Test
    void buildMatchSql_singleLeaf() {
        SegmentCondition leaf = leaf("age", CompareOperator.GT, List.of("25"));

        SegmentSqlBuilder builder = new SegmentSqlBuilder();
        String sql = builder.buildMatchSql(leaf, "user001");
        Object[] params = builder.getParams();

        assertTrue(sql.contains("COUNT(DISTINCT user_id)"));
        assertTrue(sql.contains("user_id = ?"));
        assertEquals("user001", params[0]);
        assertEquals("age", params[1]);
        assertEquals(25.0, params[2]);
    }

    private SegmentCondition leaf(String tagKey, CompareOperator op, List<String> values) {
        SegmentCondition c = new SegmentCondition();
        c.setTagKey(tagKey);
        c.setCompareOp(op);
        c.setValues(values);
        return c;
    }
}
