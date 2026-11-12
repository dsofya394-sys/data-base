package com.db.Model;

import com.db.Index.DatabaseIndex;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;


public class DataBase implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String name;
    private final List<Table> tables;
    private final List<ForeignKey> foreignKeys;
    private final Map<String, DatabaseIndex> indexes;

    public DataBase(String name) {
        this.name = Objects.requireNonNull(name);
        this.tables = new ArrayList<>();
        this.foreignKeys = new ArrayList<>();
        this.indexes = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public List<Table> getTables() {
        return Collections.unmodifiableList(tables);
    }

    public void addTable(Table table) {
        Objects.requireNonNull(table);
        for (Table existingTable : tables) {
            if (existingTable.getName().equals(table.getName())) {
                throw new IllegalArgumentException("Table with this name already exists");
            }
        }
        tables.add(table);
    }

    public Table getTable(String tableName) {
        for (Table table : tables) {
            if (table.getName().equals(tableName)) {
                return table;
            }
        }
        return null;
    }

    public void removeTable(String tableName) {
        Table table = getTable(tableName);
        if (table == null) throw new IllegalArgumentException("Table not found");
        for (ForeignKey foreignKey : foreignKeys) {
            if (foreignKey.getSourceTableName().equals(tableName)
                    || foreignKey.getReferencedTableName().equals(tableName)) {
                throw new IllegalStateException("Cannot remove a table involved in a foreign key");
            }
        }
        tables.remove(table);
        indexes.entrySet().removeIf(e -> e.getKey().startsWith(tableName + "."));
    }

    public void createIndex(String tableName, String columnName) {
        Table table = getTable(tableName);
        if (table == null) throw new IllegalArgumentException("Table not found: " + tableName);

        boolean columnExists = false;
        for (Column c : table.getColumns()) {
            if (c.getName().equals(columnName)) {
                columnExists = true;
                break;
            }
        }
        if (!columnExists) throw new IllegalArgumentException("Column not found: " + columnName);

        String key = tableName + "." + columnName;
        if (indexes.containsKey(key)) {
            System.out.println("Index already exists: " + key);
            return;
        }
        DatabaseIndex index = new DatabaseIndex(tableName, columnName);
        for (Row row : table.getRows()) {
            index.addRow(row);
        }
        indexes.put(key, index);
    }

    public DatabaseIndex getIndex(String tableName, String columnName) {
        return indexes.get(tableName + "." + columnName);
    }

    public Map<String, DatabaseIndex> getIndexes() {
        return Collections.unmodifiableMap(indexes);
    }

    public void addForeignKey(ForeignKey foreignKey) {
        Objects.requireNonNull(foreignKey);
        Table sourceTable = getTable(foreignKey.getSourceTableName());
        Table referencedTable = getTable(foreignKey.getReferencedTableName());
        if (sourceTable == null) throw new IllegalArgumentException("Source table not found");
        if (referencedTable == null) throw new IllegalArgumentException("Referenced table not found");

        Column sourceColumn = null;
        Column referencedColumn = null;
        for (Column column : sourceTable.getColumns()) {
            if (column.getName().equals(foreignKey.getColumnName())) {
                sourceColumn = column;
                break;
            }
        }
        for (Column column : referencedTable.getColumns()) {
            if (column.getName().equals(foreignKey.getReferencedColumnName())) {
                referencedColumn = column;
                break;
            }
        }
        if (sourceColumn == null) throw new IllegalArgumentException("Foreign key column not found in source table");
        if (referencedColumn == null) throw new IllegalArgumentException("Referenced column not found");
        if (!referencedColumn.isPrimaryKey())
            throw new IllegalArgumentException("Referenced column must be a primary key");
        if (sourceColumn.getDataType() != referencedColumn.getDataType()) {
            throw new IllegalArgumentException("Foreign key columns must have the same data type");
        }

        for (ForeignKey existing : foreignKeys) {
            if (existing.getSourceTableName().equals(foreignKey.getSourceTableName())
                    && existing.getColumnName().equals(foreignKey.getColumnName())) {
                throw new IllegalArgumentException("Foreign key already exists");
            }
        }
        foreignKeys.add(foreignKey);
    }

    public List<ForeignKey> getForeignKeys() {
        return Collections.unmodifiableList(foreignKeys);
    }

    public void insert(String tableName, Row row) {
        Table table = getTable(tableName);
        if (table == null) throw new IllegalArgumentException("Table has not found: " + tableName);

        for (ForeignKey foreignKey : foreignKeys) {
            if (!foreignKey.getSourceTableName().equals(tableName)) continue;
            Object value = row.getValue(foreignKey.getColumnName());
            if (value == null) continue;
            Table referencedTable = getTable(foreignKey.getReferencedTableName());
            boolean found = false;
            for (Row existingRow : referencedTable.getRows()) {
                if (Objects.equals(value, existingRow.getValue(foreignKey.getReferencedColumnName()))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalArgumentException("Foreign key violation: value " + value
                        + " does not exist in table " + foreignKey.getReferencedTableName());
            }
        }
        table.insert(row);

        for (DatabaseIndex index : indexes.values()) {
            if (index.getTableName().equals(tableName)) {
                index.addRow(row);
            }
        }
    }

    public int update(String tableName, String conditionColumn, Object conditionValue,
                      Map<String, Object> newValues) {
        Table table = getTable(tableName);
        if (table == null) throw new IllegalArgumentException("Table not found: " + tableName);

        for (ForeignKey foreignKey : foreignKeys) {
            if (!foreignKey.getSourceTableName().equals(tableName)) continue;
            if (!newValues.containsKey(foreignKey.getColumnName())) continue;
            Object value = newValues.get(foreignKey.getColumnName());
            if (value == null) continue;
            Table referencedTable = getTable(foreignKey.getReferencedTableName());
            boolean found = false;
            for (Row row : referencedTable.getRows()) {
                if (Objects.equals(value, row.getValue(foreignKey.getReferencedColumnName()))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalArgumentException("Foreign key violation: value " + value
                        + " does not exist in table " + foreignKey.getReferencedTableName());
            }
        }

        List<Row> rowsToUpdate = new ArrayList<>();
        for (Row row : table.getRows()) {
            if (Objects.equals(row.getValue(conditionColumn), conditionValue)) {
                rowsToUpdate.add(row);
            }
        }
        for (DatabaseIndex index : indexes.values()) {
            if (index.getTableName().equals(tableName)) {
                for (Row row : rowsToUpdate) index.removeRow(row);
            }
        }

        int updated = table.update(conditionColumn, conditionValue, newValues);

        for (DatabaseIndex index : indexes.values()) {
            if (index.getTableName().equals(tableName)) {
                for (Row row : rowsToUpdate) index.addRow(row);
            }
        }
        return updated;
    }

    public int delete(String tableName, String conditionColumn, Object conditionValue) {
        Table table = getTable(tableName);
        if (table == null) throw new IllegalArgumentException("Table not found: " + tableName);

        for (ForeignKey foreignKey : foreignKeys) {
            if (!foreignKey.getReferencedTableName().equals(tableName)) continue;
            Table sourceTable = getTable(foreignKey.getSourceTableName());
            for (Row row : table.getRows()) {
                if (!Objects.equals(row.getValue(conditionColumn), conditionValue)) continue;
                Object referencedValue = row.getValue(foreignKey.getReferencedColumnName());
                for (Row sourceRow : sourceTable.getRows()) {
                    if (Objects.equals(referencedValue, sourceRow.getValue(foreignKey.getColumnName()))) {
                        throw new IllegalStateException("Cannot delete record: it is referenced by table "
                                + sourceTable.getName());
                    }
                }
            }
        }

        List<Row> rowsToDelete = new ArrayList<>();
        for (Row row : table.getRows()) {
            if (Objects.equals(row.getValue(conditionColumn), conditionValue)) {
                rowsToDelete.add(row);
            }
        }

        int deleted = table.delete(conditionColumn, conditionValue);

        for (DatabaseIndex index : indexes.values()) {
            if (index.getTableName().equals(tableName)) {
                for (Row row : rowsToDelete) index.removeRow(row);
            }
        }
        return deleted;
    }
}