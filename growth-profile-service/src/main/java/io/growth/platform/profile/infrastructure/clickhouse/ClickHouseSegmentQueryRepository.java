package io.growth.platform.profile.infrastructure.clickhouse;

import io.growth.platform.profile.domain.model.SegmentCondition;
import io.growth.platform.profile.domain.repository.SegmentQueryRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ClickHouseSegmentQueryRepository implements SegmentQueryRepository {

    private final JdbcTemplate clickHouseJdbcTemplate;

    public ClickHouseSegmentQueryRepository(@Qualifier("clickHouseJdbcTemplate") JdbcTemplate clickHouseJdbcTemplate) {
        this.clickHouseJdbcTemplate = clickHouseJdbcTemplate;
    }

    @Override
    public long countUsers(SegmentCondition rootCondition) {
        SegmentSqlBuilder builder = new SegmentSqlBuilder();
        String sql = builder.buildCountSql(rootCondition);
        Long count = clickHouseJdbcTemplate.queryForObject(sql, Long.class, builder.getParams());
        return count != null ? count : 0;
    }

    @Override
    public List<String> queryUsers(SegmentCondition rootCondition, int pageNum, int pageSize) {
        SegmentSqlBuilder builder = new SegmentSqlBuilder();
        int offset = (pageNum - 1) * pageSize;
        String sql = builder.buildQuerySql(rootCondition, offset, pageSize);
        return clickHouseJdbcTemplate.queryForList(sql, String.class, builder.getParams());
    }
}
