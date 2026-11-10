package com.db.Model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

public class Table implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String name;
    private final List<Column> columns;
    private final List<Row> rows;

    public Table(String name) {
        this.name = name;
        this.columns = new ArrayList<>();
        this.rows = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Column> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public List<Row> getRows() {
        return Collections.unmodifiableList(rows);
    }

    public void addColumn(Column column) {
        Objects.requireNonNull(column, "Column cannot be null");
        for (Column existing : columns) {
            if (existing.getName().equals(column.getName())) {
                throw new IllegalArgumentException("Column already exists: " + column.getName());
            }
        }
        if (column.isPrimaryKey()) {
            for (Column existing : columns) {
                if (existing.isPrimaryKey()) {
                    throw new IllegalArgumentException("Table already has a primary key");
                }
            }
        }
        if (!rows.isEmpty()) {
            throw new IllegalStateException("Cannot add columns to a non-empty table");
        }
        columns.add(column);
    }

    public void insert(Row row) {
        Objects.requireNonNull(row, "Row cannot be null");
        Map<String, Object> values = row.getValues();

        for (String columnName : values.keySet()) {
            boolean columnExists = false;
            for (Column column : columns) {
                if (column.getName().equals(columnName)) {
                    columnExists = true;
                    break;
                }
            }
            if (!columnExists) {
                throw new IllegalArgumentException("Unknown column: " + columnName);
            }
        }

        for (Column column : columns) {
            Object value = values.get(column.getName());
            if (value == null) {
                if (!column.isNullable() || column.isPrimaryKey()) {
                    throw new IllegalArgumentException("Column cannot be empty: " + column.getName());
                }
                continue;
            }

            boolean correctType = switch (column.getDataType()) {
                case INTEGER -> value instanceof Integer;
                case DOUBLE -> value instanceof Double;
                case STRING -> value instanceof String;
                case DATE -> value instanceof LocalDate;
                case BOOLEAN -> value instanceof Boolean;
            };
            if (!correctType) {
                throw new IllegalArgumentException("Incorrect data type for column: " + column.getName());
            }
            if (column.isPrimaryKey()) {
                for (Row existingRow : rows) {
                    Object existingValue = existingRow.getValue(column.getName());
                    if (Objects.equals(existingValue, value)) {
                        throw new IllegalArgumentException("Duplicate primary key: " + value);
                    }
                }
            }
        }
        rows.add(row);
    }

    public int update(String conditionColumn, Object conditionValue, Map<String, Object> newValues) {
        Objects.requireNonNull(newValues, "New values cannot be null");
        if (newValues.isEmpty()) {
            throw new IllegalArgumentException("No values to update");
        }

        Column condition = null;
        for (Column column : columns) {
            if (column.getName().equals(conditionColumn)) {
                condition = column;
                break;
            }
        }
        if (condition == null) {
            throw new IllegalArgumentException("Unknown condition column: " + conditionColumn);
        }

        for (Map.Entry<String, Object> entry : newValues.entrySet()) {
            Column column = null;
            for (Column existing : columns) {
                if (existing.getName().equals(entry.getKey())) {
                    column = existing;
                    break;
                }
            }
            if (column == null) {
                throw new IllegalArgumentException("Unknown column: " + entry.getKey());
            }
            if (column.isPrimaryKey()) {
                throw new IllegalArgumentException("Primary key cannot be updated");
            }
            Object value = entry.getValue();
            if (value == null) {
                if (!column.isNullable()) {
                    throw new IllegalArgumentException("Column cannot be empty: " + column.getName());
                }
                continue;
            }
            boolean correctType = switch (column.getDataType()) {
                case INTEGER -> value instanceof Integer;
                case DOUBLE -> value instanceof Double;
                case STRING -> value instanceof String;
                case DATE -> value instanceof LocalDate;
                case BOOLEAN -> value instanceof Boolean;
            };
            if (!correctType) {
                throw new IllegalArgumentException("Incorrect data type for column: " + column.getName());
            }
        }

        int updatedCount = 0;
        for (Row row : rows) {
            if (Objects.equals(row.getValue(conditionColumn), conditionValue)) {
                for (Map.Entry<String, Object> entry : newValues.entrySet()) {
                    row.setValue(entry.getKey(), entry.getValue());
                }
                updatedCount++;
            }
        }
        return updatedCount;
    }

    public int delete(String conditionColumn, Object conditionValue) {
        boolean columnExists = false;
        for (Column column : columns) {
            if (column.getName().equals(conditionColumn)) {
                columnExists = true;
                break;
            }
        }
        if (!columnExists) {
            throw new IllegalArgumentException("Unknown column: " + conditionColumn);
        }

        int deletedCount = 0;
        Iterator<Row> iterator = rows.iterator();
        while (iterator.hasNext()) {
            Row row = iterator.next();
            if (Objects.equals(row.getValue(conditionColumn), conditionValue)) {
                iterator.remove();
                deletedCount++;
            }
        }
        return deletedCount;
    }

    public List<Row> select() {
        return new ArrayList<>(rows);
    }

    public void printRows(List<Row> selectedRows) {
        if (columns.isEmpty()) {
            System.out.println("(empty table)");
            return;
        }

        int[] widths = new int[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            widths[i] = columns.get(i).getName().length();
        }
        for (Row row : selectedRows) {
            for (int i = 0; i < columns.size(); i++) {
                String value = String.valueOf(row.getValue(columns.get(i).getName()));
                widths[i] = Math.max(widths[i], value.length());
            }
        }

        StringBuilder separator = new StringBuilder(".");
        for (int width : widths) {
            separator.append("-".repeat(width + 2)).append(".");
        }
        String line = separator.toString();
        System.out.println(line);
        for (int i = 0; i < columns.size(); i++) {
            System.out.printf("| %-" + widths[i] + "s ", columns.get(i).getName());
        }
        System.out.println("|");
        System.out.println(line);
        for (Row row : selectedRows) {
            for (int i = 0; i < columns.size(); i++) {
                String value = String.valueOf(row.getValue(columns.get(i).getName()));
                System.out.printf("| %-" + widths[i] + "s ", value);
            }
            System.out.println("|");
        }
        System.out.println(line);
    }
}