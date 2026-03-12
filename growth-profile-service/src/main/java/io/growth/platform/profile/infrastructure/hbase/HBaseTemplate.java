package io.growth.platform.profile.infrastructure.hbase;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class HBaseTemplate {

    private final Connection connection;

    public HBaseTemplate(Connection connection) {
        this.connection = connection;
    }

    public void put(String tableName, String rowKey, String family, String qualifier, String value) throws IOException {
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            Put put = new Put(Bytes.toBytes(rowKey));
            put.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier), Bytes.toBytes(value));
            table.put(put);
        }
    }

    public void putBatch(String tableName, List<Put> puts) throws IOException {
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            table.put(puts);
        }
    }

    public Optional<String> getString(String tableName, String rowKey, String family, String qualifier) throws IOException {
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            Get get = new Get(Bytes.toBytes(rowKey));
            get.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier));
            Result result = table.get(get);
            if (result.isEmpty()) {
                return Optional.empty();
            }
            byte[] val = result.getValue(Bytes.toBytes(family), Bytes.toBytes(qualifier));
            return val == null ? Optional.empty() : Optional.of(Bytes.toString(val));
        }
    }

    public Map<String, String> getRow(String tableName, String rowKey, String family) throws IOException {
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            Get get = new Get(Bytes.toBytes(rowKey));
            get.addFamily(Bytes.toBytes(family));
            Result result = table.get(get);
            if (result.isEmpty()) {
                return Collections.emptyMap();
            }
            Map<String, String> map = new LinkedHashMap<>();
            NavigableMap<byte[], byte[]> familyMap = result.getFamilyMap(Bytes.toBytes(family));
            if (familyMap != null) {
                familyMap.forEach((k, v) -> map.put(Bytes.toString(k), Bytes.toString(v)));
            }
            return map;
        }
    }

    public void delete(String tableName, String rowKey, String family, String qualifier) throws IOException {
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            Delete delete = new Delete(Bytes.toBytes(rowKey));
            delete.addColumns(Bytes.toBytes(family), Bytes.toBytes(qualifier));
            table.delete(delete);
        }
    }
}
