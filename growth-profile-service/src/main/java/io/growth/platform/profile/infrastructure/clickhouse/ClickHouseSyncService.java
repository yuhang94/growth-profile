package io.growth.platform.profile.infrastructure.clickhouse;

import io.growth.platform.profile.domain.event.TagValueChanged;
import io.growth.platform.profile.infrastructure.hbase.HBaseTemplate;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

@Service
public class ClickHouseSyncService {

    private static final Logger log = LoggerFactory.getLogger(ClickHouseSyncService.class);
    private static final String HBASE_TABLE = "gp_profile_tag_value";
    private static final String COLUMN_FAMILY = "t";
    private static final String INSERT_SQL = "INSERT INTO gp_profile_tag_wide (user_id, tag_key, tag_value) VALUES (?, ?, ?)";

    private final JdbcTemplate clickHouseJdbcTemplate;
    private final HBaseTemplate hBaseTemplate;

    @Value("${sync.hbase-to-clickhouse.batch-size:5000}")
    private int batchSize;

    public ClickHouseSyncService(@Qualifier("clickHouseJdbcTemplate") JdbcTemplate clickHouseJdbcTemplate,
                                 HBaseTemplate hBaseTemplate) {
        this.clickHouseJdbcTemplate = clickHouseJdbcTemplate;
        this.hBaseTemplate = hBaseTemplate;
    }

    @Scheduled(cron = "${sync.hbase-to-clickhouse.cron}")
    public void fullSync() {
        log.info("Starting full HBase to ClickHouse sync");
        try {
            Scan scan = new Scan();
            TableName tableName = TableName.valueOf(HBASE_TABLE);
            ResultScanner scanner = hBaseTemplate.getConnection().getTable(tableName).getScanner(scan);

            List<Object[]> batch = new ArrayList<>();
            int totalRows = 0;

            for (Result result : scanner) {
                String userId = Bytes.toString(result.getRow());
                NavigableMap<byte[], byte[]> familyMap = result.getFamilyMap(Bytes.toBytes(COLUMN_FAMILY));
                if (familyMap == null) {
                    continue;
                }
                for (Map.Entry<byte[], byte[]> entry : familyMap.entrySet()) {
                    String tagKey = Bytes.toString(entry.getKey());
                    String tagValue = Bytes.toString(entry.getValue());
                    batch.add(new Object[]{userId, tagKey, tagValue});

                    if (batch.size() >= batchSize) {
                        flushBatch(batch);
                        totalRows += batch.size();
                        batch.clear();
                    }
                }
            }

            if (!batch.isEmpty()) {
                flushBatch(batch);
                totalRows += batch.size();
            }

            scanner.close();
            log.info("Full sync completed, total rows synced: {}", totalRows);
        } catch (IOException e) {
            log.error("Failed to perform full HBase to ClickHouse sync", e);
        }
    }

    @EventListener
    public void onTagValueChanged(TagValueChanged event) {
        if (event.getChangeType() == TagValueChanged.ChangeType.PUT) {
            try {
                clickHouseJdbcTemplate.update(INSERT_SQL,
                        event.getUserId(), event.getTagKey(), event.getTagValue());
            } catch (Exception e) {
                log.error("Failed to sync tag value to ClickHouse: userId={}, tagKey={}",
                        event.getUserId(), event.getTagKey(), e);
            }
        }
    }

    private void flushBatch(List<Object[]> batch) {
        clickHouseJdbcTemplate.batchUpdate(INSERT_SQL, batch);
    }
}
