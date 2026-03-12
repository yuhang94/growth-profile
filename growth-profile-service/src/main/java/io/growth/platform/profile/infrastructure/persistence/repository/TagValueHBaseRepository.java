package io.growth.platform.profile.infrastructure.persistence.repository;

import io.growth.platform.profile.domain.model.TagValue;
import io.growth.platform.profile.domain.repository.TagValueRepository;
import io.growth.platform.profile.infrastructure.hbase.HBaseTemplate;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.*;

@Repository
public class TagValueHBaseRepository implements TagValueRepository {

    private static final Logger log = LoggerFactory.getLogger(TagValueHBaseRepository.class);

    private static final String TABLE_NAME = "gp_profile_tag_value";
    private static final String COLUMN_FAMILY = "t";

    private final HBaseTemplate hbaseTemplate;

    public TagValueHBaseRepository(HBaseTemplate hbaseTemplate) {
        this.hbaseTemplate = hbaseTemplate;
    }

    @Override
    public void put(TagValue tagValue) {
        try {
            hbaseTemplate.put(TABLE_NAME, tagValue.getUserId(), COLUMN_FAMILY, tagValue.getTagKey(), tagValue.getTagValue());
        } catch (IOException e) {
            log.error("Failed to put tag value: userId={}, tagKey={}", tagValue.getUserId(), tagValue.getTagKey(), e);
            throw new RuntimeException("Failed to write tag value to HBase", e);
        }
    }

    @Override
    public void putBatch(List<TagValue> tagValues) {
        List<Put> puts = new ArrayList<>(tagValues.size());
        for (TagValue tv : tagValues) {
            Put put = new Put(Bytes.toBytes(tv.getUserId()));
            put.addColumn(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes(tv.getTagKey()), Bytes.toBytes(tv.getTagValue()));
            puts.add(put);
        }
        try {
            hbaseTemplate.putBatch(TABLE_NAME, puts);
        } catch (IOException e) {
            log.error("Failed to batch put tag values, size={}", tagValues.size(), e);
            throw new RuntimeException("Failed to batch write tag values to HBase", e);
        }
    }

    @Override
    public Optional<String> get(String userId, String tagKey) {
        try {
            return hbaseTemplate.getString(TABLE_NAME, userId, COLUMN_FAMILY, tagKey);
        } catch (IOException e) {
            log.error("Failed to get tag value: userId={}, tagKey={}", userId, tagKey, e);
            throw new RuntimeException("Failed to read tag value from HBase", e);
        }
    }

    @Override
    public Map<String, String> getUserTags(String userId) {
        try {
            return hbaseTemplate.getRow(TABLE_NAME, userId, COLUMN_FAMILY);
        } catch (IOException e) {
            log.error("Failed to get user tags: userId={}", userId, e);
            throw new RuntimeException("Failed to read user tags from HBase", e);
        }
    }

    @Override
    public void delete(String userId, String tagKey) {
        try {
            hbaseTemplate.delete(TABLE_NAME, userId, COLUMN_FAMILY, tagKey);
        } catch (IOException e) {
            log.error("Failed to delete tag value: userId={}, tagKey={}", userId, tagKey, e);
            throw new RuntimeException("Failed to delete tag value from HBase", e);
        }
    }
}
