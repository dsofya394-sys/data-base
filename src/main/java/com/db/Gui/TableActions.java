package com.db.Gui;

import com.db.Model.Column;
import com.db.Model.DataBase;
import com.db.Model.DataType;
import com.db.Model.Row;
import com.db.Model.Table;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;

public class TableActions {
    private final MainWindow window;

    public TableActions(MainWindow window) {
        this.window = window;
    }

    private DataBase db() { return window.getDatabase(); }
    private String selectedTableName() { return window.getTableList().getSelectedValue(); }

    public void createTable() {
        String tableName;
        while (true) {
            tableName = JOptionPane.showInputDialog(window, "Enter table name:", "Create table", JOptionPane.PLAIN_MESSAGE);
            if (tableName == null) return;
            tableName = tableName.trim();
            if (!tableName.matches("[a-zA-Zа-яА-ЯёЁ0-9_]+")) {
                JOptionPane.showMessageDialog(window, "Invalid table name. Use letters, numbers and _");
                continue;
            }
            if (db().getTable(tableName) != null) {
                JOptionPane.showMessageDialog(window, "A table with this name already exists!");
                continue;
            }
            break;
        }

        int count;
        while (true) {
            String countText = JOptionPane.showInputDialog(window, "Enter number of columns:");
            if (countText == null) return;
            try {
                count = Integer.parseInt(countText);
                if (count < 1) {
                    JOptionPane.showMessageDialog(window, "At least one column is required!");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(window, "Enter a valid number!");
            }
        }

        Table newTable = new Table(tableName);
        Set<String> columnNames = new HashSet<>();
        boolean hasPrimaryKey = false;
        String[] types = {"INTEGER", "DOUBLE", "STRING", "DATE", "BOOLEAN"};

        for (int i = 0; i < count; i++) {
            String columnName;
            while (true) {
                columnName = JOptionPane.showInputDialog(window, "Name of column " + (i + 1) + ":");
                if (columnName == null) return;
                columnName = columnName.trim();
                if (!columnName.matches("[a-zA-Zа-яА-ЯёЁ0-9_]+")) {
                    JOptionPane.showMessageDialog(window, "Invalid column name. Use letters, numbers and _");
                    continue;
                }
                if (columnNames.contains(columnName)) {
                    JOptionPane.showMessageDialog(window, "This column already exists!");
                    continue;
                }
                break;
            }

            String selectedType = (String) JOptionPane.showInputDialog(window, "Choose type for column " + columnName + ":", "Column type", JOptionPane.PLAIN_MESSAGE, null, types, types[0]);
            if (selectedType == null) return;
            DataType type = DataType.valueOf(selectedType);

            boolean primaryKey;
            while (true) {
                primaryKey = JOptionPane.showConfirmDialog(window, "Is " + columnName + " a primary key?", "Primary key", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
                if (primaryKey && hasPrimaryKey) {
                    JOptionPane.showMessageDialog(window, "The table already has a primary key!");
                    continue;
                }
                if (i == count - 1 && !hasPrimaryKey && !primaryKey) {
                    JOptionPane.showMessageDialog(window, "The table must have a primary key. Choose YES.");
                    continue;
                }
                break;
            }

            boolean nullable = false;
            if (!primaryKey) {
                nullable = JOptionPane.showConfirmDialog(window, "Can column " + columnName + " be empty?", "Nullable", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
            }

            try {
                newTable.addColumn(new Column(columnName, type, primaryKey, nullable));
                columnNames.add(columnName);
                if (primaryKey) hasPrimaryKey = true;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(window, "Error: " + e.getMessage() + "\nPlease try again.");
                i--;
            }
        }

        try {
            db().addTable(newTable);
            window.saveDatabase();
            window.getTableListModel().addElement(tableName);
            window.getTableList().setSelectedValue(tableName, true);
            JOptionPane.showMessageDialog(window, "Table successfully created!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(window, "Failed to create table:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteTable() {
        String tableName = selectedTableName();
        if (tableName == null) {
            JOptionPane.showMessageDialog(window, "Choose a table first!");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(window, "Delete table '" + tableName + "'?", "Confirm deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;

        try {
            db().removeTable(tableName);
            window.saveDatabase();
            window.getTableListModel().removeElement(tableName);

            if (!window.getTableListModel().isEmpty()) {
                window.getTableList().setSelectedIndex(0);
            } else {
                window.getDataTable().setModel(new javax.swing.table.DefaultTableModel(new Object[0][1], new String[]{"Choose a table"}));
                window.getDataLabel().setText("Table's data");
            }
            JOptionPane.showMessageDialog(window, "Table successfully deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(window, "Failed to delete table:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void addRow() {
        String tableName = selectedTableName();
        if (tableName == null) {
            JOptionPane.showMessageDialog(window, "Choose a table first!");
            return;
        }

        Table table = db().getTable(tableName);
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(new EmptyBorder(10, 10, 10, 10));
        form.setBackground(Color.WHITE);

        Map<String, JComponent> fields = new LinkedHashMap<>();

        for (Column column : table.getColumns()) {
            JLabel label = new JLabel(column.getName());
            JComponent input = column.getDataType() == DataType.BOOLEAN
                    ? new JComboBox<>(new String[]{"true", "false"})
                    : new JTextField(15);
            if (column.getDataType() == DataType.DATE) {
                ((JTextField) input).setToolTipText("Format: yyyy-MM-dd");
            }
            fields.put(column.getName(), input);
            form.add(label);
            form.add(input);
        }

        while (true) {
            int option = JOptionPane.showConfirmDialog(window, form, "Add record to " + tableName, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (option != JOptionPane.OK_OPTION) return;

            Row newRow = new Row();
            List<String> errors = new ArrayList<>();

            for (Column column : table.getColumns()) {
                String columnName = column.getName();
                JComponent input = fields.get(columnName);
                String value = input instanceof JComboBox<?> cb ? (String) cb.getSelectedItem() : ((JTextField) input).getText().trim();

                try {
                    Object parsedValue;
                    if (value.isEmpty()) {
                        if (column.isNullable()) parsedValue = null;
                        else {
                            errors.add("Field '" + columnName + "' cannot be empty.");
                            continue;
                        }
                    } else {
                        switch (column.getDataType()) {
                            case INTEGER:
                                int intValue = Integer.parseInt(value);
                                if (columnName.equalsIgnoreCase("year") && intValue > LocalDate.now().getYear()) {
                                    errors.add("Field '" + columnName + "': year cannot be greater than the current year.");
                                    continue;
                                }
                                parsedValue = intValue;
                                break;
                            case DOUBLE:
                                double doubleValue = Double.parseDouble(value);
                                if (!Double.isFinite(doubleValue)) {
                                    errors.add("Field '" + columnName + "': enter a finite number.");
                                    continue;
                                }
                                parsedValue = doubleValue;
                                break;
                            case STRING:
                                parsedValue = value;
                                break;
                            case DATE:
                                LocalDate date = LocalDate.parse(value);
                                if (date.isAfter(LocalDate.now())) {
                                    errors.add("Field '" + columnName + "': date cannot be later than today.");
                                    continue;
                                }
                                parsedValue = date;
                                break;
                            case BOOLEAN:
                                if (!value.equals("true") && !value.equals("false")) {
                                    errors.add("Field '" + columnName + "': enter true or false.");
                                    continue;
                                }
                                parsedValue = Boolean.valueOf(value);
                                break;
                            default:
                                errors.add("Field '" + columnName + "': unsupported data type.");
                                continue;
                        }
                    }
                    newRow.setValue(columnName, parsedValue);
                } catch (NumberFormatException e) {
                    errors.add("Field '" + columnName + "': invalid number.");
                } catch (DateTimeParseException e) {
                    errors.add("Field '" + columnName + "': invalid date. Use yyyy-MM-dd.");
                } catch (Exception e) {
                    errors.add("Field '" + columnName + "': " + e.getMessage());
                }
            }

            if (!errors.isEmpty()) {
                JOptionPane.showMessageDialog(window, "Please fix the following errors:\n\n" + String.join("\n", errors), "Validation errors", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            try {
                db().insert(tableName, newRow);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(window, "Failed to add record:\n" + e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            window.saveDatabase();
            window.showTableData(tableName);
            JOptionPane.showMessageDialog(window, "Record successfully added!", "Success", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    }

    public void deleteRow() {
        String tableName = selectedTableName();
        if (tableName == null) {
            JOptionPane.showMessageDialog(window, "Choose a table first!");
            return;
        }

        int selectedRow = window.getDataTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(window, "Select a record to delete!");
            return;
        }

        Table table = db().getTable(tableName);
        Column primaryKey = null;
        for (Column column : table.getColumns()) {
            if (column.isPrimaryKey()) {
                primaryKey = column;
                break;
            }
        }
        if (primaryKey == null) {
            JOptionPane.showMessageDialog(window, "Primary key not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int modelRow = window.getDataTable().convertRowIndexToModel(selectedRow);
        Object primaryKeyValue = window.getDataTable().getModel().getValueAt(modelRow, table.getColumns().indexOf(primaryKey));

        int confirmation = JOptionPane.showConfirmDialog(window, "Are you sure you want to delete record with ID " + primaryKeyValue + "?", "Confirm deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION) return;

        try {
            int deleted = db().delete(tableName, primaryKey.getName(), primaryKeyValue);
            if (deleted == 0) {
                JOptionPane.showMessageDialog(window, "Record was not found!");
                return;
            }
            window.saveDatabase();
            window.showTableData(tableName);
            JOptionPane.showMessageDialog(window, "Record successfully deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(window, "Failed to delete record:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void editRow() {
        String tableName = selectedTableName();
        if (tableName == null) {
            JOptionPane.showMessageDialog(window, "Choose a table first!");
            return;
        }

        int selectedRow = window.getDataTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(window, "Select a record to edit!");
            return;
        }

        Table table = db().getTable(tableName);
        int modelRow = window.getDataTable().convertRowIndexToModel(selectedRow);

        Column primaryKey = null;
        for (Column column : table.getColumns()) {
            if (column.isPrimaryKey()) {
                primaryKey = column;
                break;
            }
        }
        if (primaryKey == null) {
            JOptionPane.showMessageDialog(window, "Primary key not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Object primaryKeyValue = window.getDataTable().getModel().getValueAt(modelRow, table.getColumns().indexOf(primaryKey));

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(new EmptyBorder(10, 10, 10, 10));
        form.setBackground(Color.WHITE);

        Map<String, JComponent> fields = new LinkedHashMap<>();

        for (int i = 0; i < table.getColumns().size(); i++) {
            Column column = table.getColumns().get(i);
            String columnName = column.getName();
            Object currentValue = window.getDataTable().getModel().getValueAt(modelRow, i);

            JLabel label = new JLabel(columnName);
            JComponent input = column.getDataType() == DataType.BOOLEAN
                    ? new JComboBox<>(new String[]{"true", "false", ""})
                    : new JTextField(15);

            if (column.getDataType() == DataType.BOOLEAN) {
                ((JComboBox<?>) input).setSelectedItem(currentValue == null ? "" : currentValue.toString());
            } else {
                ((JTextField) input).setText(currentValue == null ? "" : currentValue.toString());
                if (column.getDataType() == DataType.DATE) {
                    ((JTextField) input).setToolTipText("Format: yyyy-MM-dd");
                }
            }

            if (column.isPrimaryKey()) input.setEnabled(false);
            fields.put(columnName, input);
            form.add(label);
            form.add(input);
        }

        int option = JOptionPane.showConfirmDialog(window, form, "Edit record in " + tableName, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) return;

        try {
            Map<String, Object> updatedValues = new LinkedHashMap<>();

            for (Column column : table.getColumns()) {
                if (column.isPrimaryKey()) continue;

                String columnName = column.getName();
                JComponent input = fields.get(columnName);
                String value = input instanceof JComboBox<?> cb ? (String) cb.getSelectedItem() : ((JTextField) input).getText().trim();

                Object parsedValue;
                if (value.isEmpty() && column.isNullable()) {
                    parsedValue = null;
                } else {
                    if (value.isEmpty()) throw new IllegalArgumentException("Field '" + columnName + "' cannot be empty!");

                    switch (column.getDataType()) {
                        case INTEGER:
                            int yearValue = Integer.parseInt(value);
                            if (column.getName().equalsIgnoreCase("year") && yearValue > LocalDate.now().getYear()) {
                                throw new IllegalArgumentException("Year cannot be greater than the current year!");
                            }
                            parsedValue = yearValue;
                            break;
                        case DOUBLE:
                            parsedValue = Double.valueOf(value);
                            break;
                        case STRING:
                            parsedValue = value;
                            break;
                        case DATE:
                            LocalDate date = LocalDate.parse(value);
                            if (date.isAfter(LocalDate.now())) {
                                throw new IllegalArgumentException("Date cannot be later than today!");
                            }
                            parsedValue = date;
                            break;
                        case BOOLEAN:
                            if (!value.equals("true") && !value.equals("false")) {
                                throw new IllegalArgumentException("Enter true or false in " + columnName);
                            }
                            parsedValue = Boolean.valueOf(value);
                            break;
                        default:
                            throw new IllegalArgumentException("Unsupported data type: " + column.getDataType());
                    }
                }
                updatedValues.put(columnName, parsedValue);
            }

            int updated = db().update(tableName, primaryKey.getName(), primaryKeyValue, updatedValues);
            if (updated == 0) {
                JOptionPane.showMessageDialog(window, "Record was not found!");
                return;
            }

            window.saveDatabase();
            window.showTableData(tableName);
            JOptionPane.showMessageDialog(window, "Record successfully updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(window, "Failed to edit record:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void createIndex() {
        String tableName = selectedTableName();
        if (tableName == null) {
            JOptionPane.showMessageDialog(window, "Choose a table first!");
            return;
        }
        Table table = db().getTable(tableName);
        if (table == null) return;

        String[] columns = table.getColumns().stream().map(Column::getName).toArray(String[]::new);
        if (columns.length == 0) return;

        String column = (String) JOptionPane.showInputDialog(window, "Choose column for index:", "Create Index", JOptionPane.QUESTION_MESSAGE, null, columns, columns[0]);

        if (column != null) {
            try {
                db().createIndex(tableName, column);
                window.saveDatabase();
                JOptionPane.showMessageDialog(window, "Index created on " + column);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(window, "Error: " + ex.getMessage());
            }
        }
    }
}