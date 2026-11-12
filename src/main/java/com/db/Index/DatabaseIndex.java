package com.db.Index;

import com.db.Model.Row;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class DatabaseIndex implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String tableName;
    private final String columnName;
    private final Map<Object, List<Row>> indexMap = new HashMap<>();

    public DatabaseIndex(String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public void addRow(Row row) {
        Object value = row.getValue(columnName);
        if (value != null) {
            indexMap.computeIfAbsent(value, _ -> new ArrayList<>()).add(row);
        }
    }

    public void removeRow(Row row) {
        Object value = row.getValue(columnName);
        if (value != null && indexMap.containsKey(value)) {
            indexMap.get(value).remove(row);
            if (indexMap.get(value).isEmpty()) {
                indexMap.remove(value);
            }
        }
    }

    public List<Row> search(Object value) {
        return indexMap.getOrDefault(value, Collections.emptyList());
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }
}