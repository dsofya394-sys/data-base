package com.db.model;

import java.io.Serializable;
import java.util.*;

public class DataBase implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private List<Table> tables;
    private List<ForeignKey> foreignKeys;

    public DataBase(String name) {
        this.name = Objects.requireNonNull(name);
        this.tables = new ArrayList<>();
        this.foreignKeys = new ArrayList<>();
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
                throw new IllegalArgumentException(
                        "Table with this name already exists"
                );
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

        if (table == null) {
            throw new IllegalArgumentException(
                    "Table not found"
            );
        }

        for (ForeignKey foreignKey : foreignKeys) {
            if (foreignKey.getSourceTableName().equals(tableName)
                    || foreignKey.getReferencedTableName().equals(tableName)) {
                throw new IllegalStateException(
                        "Cannot remove a table involved in a foreign key"
                );
            }
        }

        tables.remove(table);
    }

    public void addForeignKey(ForeignKey foreignKey) {
        Objects.requireNonNull(foreignKey);

        Table sourceTable = getTable(
                foreignKey.getSourceTableName()
        );

        Table referencedTable = getTable(
                foreignKey.getReferencedTableName()
        );

        if (sourceTable == null) {
            throw new IllegalArgumentException(
                    "Source table not found"
            );
        }

        if (referencedTable == null) {
            throw new IllegalArgumentException(
                    "Referenced table not found"
            );
        }

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

        if (sourceColumn == null) {
            throw new IllegalArgumentException(
                    "Foreign key column not found in source table"
            );
        }

        if (referencedColumn == null) {
            throw new IllegalArgumentException(
                    "Referenced column not found"
            );
        }

        if (!referencedColumn.isPrimaryKey()) {
            throw new IllegalArgumentException(
                    "Referenced column must be a primary key"
            );
        }

        if (sourceColumn.getDataType() != referencedColumn.getDataType()) {
            throw new IllegalArgumentException(
                    "Foreign key columns must have the same data type"
            );
        }

        for (ForeignKey existing : foreignKeys) {
            if (existing.getSourceTableName().equals(foreignKey.getSourceTableName())
                    && existing.getColumnName().equals(foreignKey.getColumnName())) {
                throw new IllegalArgumentException(
                        "Foreign key already exists"
                );
            }
        }

        foreignKeys.add(foreignKey);
    }

    public List<ForeignKey> getForeignKeys() {
        return Collections.unmodifiableList(foreignKeys);
    }


    public void insert(String tableName, Row row) {
        Table table = getTable(tableName);

        if (table == null) {
            throw new IllegalArgumentException(
                    "Table has not found: " + tableName
            );
        }

        for (ForeignKey foreignKey : foreignKeys) {

            if (!foreignKey.getSourceTableName().equals(tableName)) {
                continue;
            }

            Object value = row.getValue(foreignKey.getColumnName());

            if (value == null) {
                continue;
            }

            Table referencedTable = getTable(
                    foreignKey.getReferencedTableName()
            );

            boolean found = false;

            for (Row existingRow : referencedTable.getRows()) {
                Object referencedValue = existingRow.getValue(
                        foreignKey.getReferencedColumnName()
                );

                if (Objects.equals(value, referencedValue)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new IllegalArgumentException(
                        "Foreign key violation: value " + value
                                + " does not exist in table "
                                + foreignKey.getReferencedTableName()
                );
            }
        }
        table.insert(row);
    }


    public int update(String tableName,
                      String conditionColumn,
                      Object conditionValue,
                      Map<String, Object> newValues) {

        Table table = getTable(tableName);

        if (table == null) {
            throw new IllegalArgumentException(
                    "Table not found: " + tableName
            );
        }

        boolean hasMatchingRows = false;

        for (Row row : table.getRows()) {
            if (Objects.equals(
                    row.getValue(conditionColumn),
                    conditionValue
            )) {
                hasMatchingRows = true;
                break;
            }
        }

        if (!hasMatchingRows) {
            return table.update(
                    conditionColumn, conditionValue, newValues
            );
        }

        for (ForeignKey foreignKey : foreignKeys) {

            if (!foreignKey.getSourceTableName().equals(tableName)) {
                continue;
            }

            if (!newValues.containsKey(foreignKey.getColumnName())) {
                continue;
            }

            Object value = newValues.get(foreignKey.getColumnName());

            if (value == null) {
                continue;
            }

            Table referencedTable = getTable(
                    foreignKey.getReferencedTableName()
            );

            boolean found = false;

            for (Row row : referencedTable.getRows()) {
                Object referencedValue = row.getValue(
                        foreignKey.getReferencedColumnName()
                );

                if (Objects.equals(value, referencedValue)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new IllegalArgumentException(
                        "Foreign key violation: value " + value
                                + " does not exist in table "
                                + foreignKey.getReferencedTableName()
                );
            }
        }

        return table.update(
                conditionColumn, conditionValue, newValues
        );
    }


    public int delete(String tableName,
                      String conditionColumn,
                      Object conditionValue) {

        Table table = getTable(tableName);

        if (table == null) {
            throw new IllegalArgumentException(
                    "Table not found: " + tableName
            );
        }

        boolean columnExists = false;

        for (Column column : table.getColumns()) {
            if (column.getName().equals(conditionColumn)) {
                columnExists = true;
                break;
            }
        }

        if (!columnExists) {
            throw new IllegalArgumentException(
                    "Unknown column: " + conditionColumn
            );
        }

        for (ForeignKey foreignKey : foreignKeys) {

            if (!foreignKey.getReferencedTableName().equals(tableName)) {
                continue;
            }

            Table sourceTable = getTable(
                    foreignKey.getSourceTableName()
            );

            for (Row row : table.getRows()) {

                if (!Objects.equals(
                        row.getValue(conditionColumn),
                        conditionValue
                )) {
                    continue;
                }

                Object referencedValue = row.getValue(
                        foreignKey.getReferencedColumnName()
                );

                for (Row sourceRow : sourceTable.getRows()) {

                    Object sourceValue = sourceRow.getValue(
                            foreignKey.getColumnName()
                    );

                    if (Objects.equals(referencedValue, sourceValue)) {
                        throw new IllegalStateException(
                                "Cannot delete record: it is referenced by table "
                                        + sourceTable.getName()
                        );
                    }
                }
            }
        }

        return table.delete(conditionColumn, conditionValue);
    }

}
