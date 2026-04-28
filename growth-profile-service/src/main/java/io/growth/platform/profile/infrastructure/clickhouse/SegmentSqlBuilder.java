package io.growth.platform.profile.infrastructure.clickhouse;

import io.growth.platform.profile.api.enums.CompareOperator;
import io.growth.platform.profile.api.enums.ConditionOperator;
import io.growth.platform.profile.domain.model.SegmentCondition;

import java.util.ArrayList;
import java.util.List;

public class SegmentSqlBuilder {

    private static final String TABLE = "gp_profile_tag_wide";

    private final List<Object> params = new ArrayList<>();

    public String buildCountSql(SegmentCondition root) {
        String whereClause = buildCondition(root);
        return "SELECT COUNT(DISTINCT user_id) FROM " + TABLE + " WHERE user_id IN (" +
                "SELECT user_id FROM " + TABLE + " WHERE " + whereClause + ")";
    }

    public String buildQuerySql(SegmentCondition root, int offset, int limit) {
        String whereClause = buildCondition(root);
        params.add(limit);
        params.add(offset);
        return "SELECT DISTINCT user_id FROM " + TABLE + " WHERE user_id IN (" +
                "SELECT user_id FROM " + TABLE + " WHERE " + whereClause +
                ") ORDER BY user_id LIMIT ? OFFSET ?";
    }

    public String buildMatchSql(SegmentCondition root, String userId) {
        params.add(userId);
        String whereClause = buildCondition(root);
        return "SELECT COUNT(DISTINCT user_id) FROM " + TABLE +
                " WHERE user_id = ? AND user_id IN (" +
                "SELECT user_id FROM " + TABLE + " WHERE " + whereClause + ")";
    }

    public Object[] getParams() {
        return params.toArray();
    }

    private String buildCondition(SegmentCondition condition) {
        if (condition.isLeaf()) {
            return buildLeafCondition(condition);
        }
        return buildBranchCondition(condition);
    }

    private String buildBranchCondition(SegmentCondition condition) {
        ConditionOperator op = condition.getOperator();
        List<SegmentCondition> children = condition.getChildren();

        if (op == ConditionOperator.NOT) {
            String child = buildCondition(children.get(0));
            return "user_id NOT IN (SELECT user_id FROM " + TABLE + " WHERE " + child + ")";
        }

        String joiner = op == ConditionOperator.AND ? " AND " : " OR ";
        List<String> parts = new ArrayList<>();
        for (SegmentCondition child : children) {
            if (child.isLeaf()) {
                parts.add("user_id IN (SELECT user_id FROM " + TABLE + " WHERE " + buildLeafCondition(child) + ")");
            } else {
                parts.add(buildCondition(child));
            }
        }
        return "(" + String.join(joiner, parts) + ")";
    }

    private String buildLeafCondition(SegmentCondition condition) {
        String tagKey = condition.getTagKey();
        CompareOperator compareOp = condition.getCompareOp();
        List<String> values = condition.getValues();

        StringBuilder sb = new StringBuilder();
        sb.append("tag_key = ?");
        params.add(tagKey);

        switch (compareOp) {
            case EQ -> {
                sb.append(" AND tag_value = ?");
                params.add(values.get(0));
            }
            case NE -> {
                sb.append(" AND tag_value != ?");
                params.add(values.get(0));
            }
            case GT -> {
                sb.append(" AND toFloat64OrNull(tag_value) > ?");
                params.add(Double.parseDouble(values.get(0)));
            }
            case LT -> {
                sb.append(" AND toFloat64OrNull(tag_value) < ?");
                params.add(Double.parseDouble(values.get(0)));
            }
            case GE -> {
                sb.append(" AND toFloat64OrNull(tag_value) >= ?");
                params.add(Double.parseDouble(values.get(0)));
            }
            case LE -> {
                sb.append(" AND toFloat64OrNull(tag_value) <= ?");
                params.add(Double.parseDouble(values.get(0)));
            }
            case IN -> {
                String placeholders = String.join(",", values.stream().map(v -> "?").toList());
                sb.append(" AND tag_value IN (").append(placeholders).append(")");
                params.addAll(values);
            }
            case NOT_IN -> {
                String placeholders2 = String.join(",", values.stream().map(v -> "?").toList());
                sb.append(" AND tag_value NOT IN (").append(placeholders2).append(")");
                params.addAll(values);
            }
            case BETWEEN -> {
                sb.append(" AND toFloat64OrNull(tag_value) BETWEEN ? AND ?");
                params.add(Double.parseDouble(values.get(0)));
                params.add(Double.parseDouble(values.get(1)));
            }
            case IS_NULL -> sb.append(" AND (tag_value IS NULL OR tag_value = '')");
            case IS_NOT_NULL -> sb.append(" AND tag_value IS NOT NULL AND tag_value != ''");
        }

        return sb.toString();
    }
}
