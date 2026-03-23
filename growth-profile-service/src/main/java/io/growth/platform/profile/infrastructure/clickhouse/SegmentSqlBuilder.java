package io.growth.platform.profile.infrastructure.clickhouse;

import io.growth.platform.profile.api.enums.BehaviorOperator;
import io.growth.platform.profile.api.enums.CompareOperator;
import io.growth.platform.profile.api.enums.ConditionOperator;
import io.growth.platform.profile.domain.model.EventPropertyFilter;
import io.growth.platform.profile.domain.model.SegmentCondition;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SegmentSqlBuilder {

    private static final String TAG_TABLE = "gp_profile_tag_wide";
    private static final String EVENT_TABLE = "gp_profile_behavior_event";
    private static final Pattern RELATIVE_TIME_PATTERN = Pattern.compile("last_(\\d+)d");
    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<Object> params = new ArrayList<>();

    public String buildCountSql(SegmentCondition root) {
        String whereClause = buildCondition(root);
        String sourceTable = containsTagCondition(root) ? TAG_TABLE : EVENT_TABLE;
        return "SELECT COUNT(DISTINCT user_id) FROM " + sourceTable + " WHERE user_id IN (" +
                "SELECT user_id FROM " + sourceTable + " WHERE " + whereClause + ")";
    }

    public String buildQuerySql(SegmentCondition root, int offset, int limit) {
        String whereClause = buildCondition(root);
        String sourceTable = containsTagCondition(root) ? TAG_TABLE : EVENT_TABLE;
        params.add(limit);
        params.add(offset);
        return "SELECT DISTINCT user_id FROM " + sourceTable + " WHERE user_id IN (" +
                "SELECT user_id FROM " + sourceTable + " WHERE " + whereClause +
                ") ORDER BY user_id LIMIT ? OFFSET ?";
    }

    public Object[] getParams() {
        return params.toArray();
    }

    private String buildCondition(SegmentCondition condition) {
        if (condition.isLeaf()) {
            if (condition.isBehaviorCondition()) {
                return buildBehaviorCondition(condition);
            }
            return buildLeafCondition(condition);
        }
        return buildBranchCondition(condition);
    }

    private String buildBranchCondition(SegmentCondition condition) {
        ConditionOperator op = condition.getOperator();
        List<SegmentCondition> children = condition.getChildren();

        if (op == ConditionOperator.NOT) {
            SegmentCondition child = children.get(0);
            if (child.isBehaviorCondition()) {
                // For NOT + behavior, delegate to buildBehaviorCondition which handles NOT IN
                String behaviorSql = buildBehaviorCondition(child);
                return "user_id NOT IN (SELECT user_id FROM " + EVENT_TABLE + " WHERE " + behaviorSql + ")";
            }
            String childSql = buildCondition(child);
            return "user_id NOT IN (SELECT user_id FROM " + TAG_TABLE + " WHERE " + childSql + ")";
        }

        String joiner = op == ConditionOperator.AND ? " AND " : " OR ";
        List<String> parts = new ArrayList<>();
        for (SegmentCondition child : children) {
            if (child.isLeaf()) {
                if (child.isBehaviorCondition()) {
                    parts.add(buildBehaviorCondition(child));
                } else {
                    parts.add("user_id IN (SELECT user_id FROM " + TAG_TABLE + " WHERE " + buildLeafCondition(child) + ")");
                }
            } else {
                parts.add(buildCondition(child));
            }
        }
        return "(" + String.join(joiner, parts) + ")";
    }

    private String buildBehaviorCondition(SegmentCondition condition) {
        BehaviorOperator behaviorOp = condition.getBehaviorOp();
        String eventName = condition.getEventName();

        StringBuilder innerSb = new StringBuilder();
        innerSb.append("event_name = ?");
        params.add(eventName);

        // Time range
        if (condition.getTimeRangeStart() != null && condition.getTimeRangeEnd() != null) {
            LocalDateTime start = resolveTime(condition.getTimeRangeStart());
            LocalDateTime end = resolveTime(condition.getTimeRangeEnd());
            innerSb.append(" AND event_time BETWEEN ? AND ?");
            params.add(start.format(DT_FORMATTER));
            params.add(end.format(DT_FORMATTER));
        }

        // Property filters
        List<EventPropertyFilter> filters = condition.getPropertyFilters();
        if (filters != null && !filters.isEmpty()) {
            for (EventPropertyFilter filter : filters) {
                appendPropertyFilter(innerSb, filter);
            }
        }

        if (behaviorOp == BehaviorOperator.DID_NOT) {
            return "user_id NOT IN (SELECT user_id FROM " + EVENT_TABLE +
                    " WHERE " + innerSb + ")";
        }

        // DID with optional count condition
        CompareOperator countOp = condition.getCountOp();
        Integer countValue = condition.getCountValue();
        if (countOp != null && countValue != null) {
            String havingOp = toSqlOperator(countOp);
            params.add(countValue);
            return "user_id IN (SELECT user_id FROM " + EVENT_TABLE +
                    " WHERE " + innerSb +
                    " GROUP BY user_id HAVING COUNT(*) " + havingOp + " ?)";
        }

        // DID without count — just check existence
        return "user_id IN (SELECT user_id FROM " + EVENT_TABLE +
                " WHERE " + innerSb + ")";
    }

    private void appendPropertyFilter(StringBuilder sb, EventPropertyFilter filter) {
        String propKey = filter.getPropertyKey();
        CompareOperator op = filter.getCompareOp();
        List<String> values = filter.getValues();

        String jsonExtract = "JSONExtractString(properties, ?)";
        params.add(propKey);

        switch (op) {
            case EQ -> {
                sb.append(" AND ").append(jsonExtract).append(" = ?");
                params.add(values.get(0));
            }
            case NE -> {
                sb.append(" AND ").append(jsonExtract).append(" != ?");
                params.add(values.get(0));
            }
            case GT -> {
                sb.append(" AND toFloat64OrNull(").append(jsonExtract).append(") > ?");
                params.add(Double.parseDouble(values.get(0)));
            }
            case LT -> {
                sb.append(" AND toFloat64OrNull(").append(jsonExtract).append(") < ?");
                params.add(Double.parseDouble(values.get(0)));
            }
            case GE -> {
                sb.append(" AND toFloat64OrNull(").append(jsonExtract).append(") >= ?");
                params.add(Double.parseDouble(values.get(0)));
            }
            case LE -> {
                sb.append(" AND toFloat64OrNull(").append(jsonExtract).append(") <= ?");
                params.add(Double.parseDouble(values.get(0)));
            }
            case IN -> {
                String placeholders = String.join(",", values.stream().map(v -> "?").toList());
                sb.append(" AND ").append(jsonExtract).append(" IN (").append(placeholders).append(")");
                params.addAll(values);
            }
            case NOT_IN -> {
                String placeholders2 = String.join(",", values.stream().map(v -> "?").toList());
                sb.append(" AND ").append(jsonExtract).append(" NOT IN (").append(placeholders2).append(")");
                params.addAll(values);
            }
            default -> throw new IllegalArgumentException("Unsupported property filter operator: " + op);
        }
    }

    private LocalDateTime resolveTime(String timeExpr) {
        if ("now".equals(timeExpr)) {
            return LocalDateTime.now();
        }
        Matcher matcher = RELATIVE_TIME_PATTERN.matcher(timeExpr);
        if (matcher.matches()) {
            int days = Integer.parseInt(matcher.group(1));
            return LocalDateTime.now().minusDays(days);
        }
        // Absolute date: try "yyyy-MM-dd" or "yyyy-MM-dd HH:mm:ss"
        if (timeExpr.length() == 10) {
            return LocalDateTime.parse(timeExpr + " 00:00:00", DT_FORMATTER);
        }
        return LocalDateTime.parse(timeExpr, DT_FORMATTER);
    }

    private String toSqlOperator(CompareOperator op) {
        return switch (op) {
            case EQ -> "=";
            case NE -> "!=";
            case GT -> ">";
            case LT -> "<";
            case GE -> ">=";
            case LE -> "<=";
            default -> throw new IllegalArgumentException("Unsupported count operator: " + op);
        };
    }

    private boolean containsTagCondition(SegmentCondition condition) {
        if (condition.isTagCondition()) {
            return true;
        }
        if (condition.getChildren() != null) {
            for (SegmentCondition child : condition.getChildren()) {
                if (containsTagCondition(child)) {
                    return true;
                }
            }
        }
        return false;
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
