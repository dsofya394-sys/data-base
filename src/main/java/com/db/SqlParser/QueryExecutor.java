package com.db.SqlParser;

import com.db.Index.DatabaseIndex;
import com.db.Model.*;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryExecutor {

    private final DataBase db;

    public QueryExecutor(DataBase db) {
        this.db = db;
    }

    public void execute(String query) {
        query = query.trim();
        if (query.isEmpty()) return;

        try {
            String upper = query.toUpperCase();
            if (upper.startsWith("SELECT")) {
                executeSelect(query);
            } else if (upper.startsWith("INSERT")) {
                executeInsert(query);
            } else if (upper.startsWith("UPDATE")) {
                executeUpdate(query);
            } else if (upper.startsWith("DELETE")) {
                executeDelete(query);
            } else if (upper.startsWith("CREATE INDEX")) {
                executeCreateIndex(query);
            } else if (upper.startsWith("CREATE TABLE")) {
                executeCreateTable(query);
            } else if (upper.startsWith("DROP TABLE")) {
                executeDropTable(query);
            } else if (upper.startsWith("SHOW INDEXES")) {
                executeShowIndexes();
            } else if (upper.startsWith("SHOW FOREIGN KEYS")) {
                executeShowForeignKeys();
            } else {
                System.out.println("Unknown command: " + query);
            }
        } catch (Exception e) {
            System.err.println("Error executing query: " + e.getMessage());
        }
    }

    private void executeSelect(String query) {
        Pattern p = Pattern.compile(
                "SELECT\\s+(.+?)\\s+FROM\\s+(\\w+)(?:\\s+WHERE\\s+(\\w+)\\s*(=|>|<|>=|<=|!=)\\s*(.+?))?;?$",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(query);
        if (!m.find()) {
            System.out.println("Invalid SELECT syntax");
            return;
        }

        String columnsStr = m.group(1).trim();
        String tableName = m.group(2);
        String whereCol = m.group(3);
        String op = m.group(4);
        String whereValStr = m.group(5);

        Table table = db.getTable(tableName);
        if (table == null) {
            System.out.println("Table not found: " + tableName);
            return;
        }

        List<Row> rows;

        if (whereCol != null && op != null && (op.equals("=") || op.equals("=="))) {
            DatabaseIndex index = db.getIndex(tableName, whereCol);
            if (index != null) {
                System.out.println("[Using index on " + whereCol + "]");
                Object value = parseValue(whereValStr);
                rows = new ArrayList<>(index.search(value));
            } else {
                rows = table.select();
                rows = filterRows(rows, whereCol, op, parseValue(whereValStr));
            }
        } else if (whereCol != null) {
            rows = table.select();
            rows = filterRows(rows, whereCol, op, parseValue(whereValStr));
        } else {
            rows = table.select();
        }

        if (columnsStr.equals("*")) {
            table.printRows(rows);
        } else {
            printSelectedColumns(table, rows, columnsStr);
        }
    }

    private List<Row> filterRows(List<Row> rows, String col, String op, Object value) {
        List<Row> result = new ArrayList<>();
        for (Row row : rows) {
            Object cell = row.getValue(col);
            if (compare(cell, value, op)) result.add(row);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private boolean compare(Object a, Object b, String op) {
        if (a == null || b == null) return false;

        if (a instanceof Comparable && b.getClass() == a.getClass()) {
            Comparable<Object> ca = (Comparable<Object>) a;
            int cmp = ca.compareTo(b);
            switch (op) {
                case "=":
                case "==":
                    return cmp == 0;
                case "!=":
                    return cmp != 0;
                case ">":
                    return cmp > 0;
                case "<":
                    return cmp < 0;
                case ">=":
                    return cmp >= 0;
                case "<=":
                    return cmp <= 0;
            }
        }
        return op.equals("=") && Objects.equals(a, b);
    }

    private void printSelectedColumns(Table table, List<Row> rows, String columnsStr) {
        String[] cols = columnsStr.split(",");
        for (int i = 0; i < cols.length; i++) {
            cols[i] = cols[i].trim();
        }

        int[] widths = new int[cols.length];
        for (int i = 0; i < cols.length; i++) {
            widths[i] = cols[i].length();
        }
        for (Row row : rows) {
            for (int i = 0; i < cols.length; i++) {
                widths[i] = Math.max(widths[i], String.valueOf(row.getValue(cols[i])).length());
            }
        }

        StringBuilder sep = new StringBuilder(".");
        for (int w : widths) {
            sep.append("-".repeat(w + 2)).append(".");
        }
        String line = sep.toString();
        System.out.println(line);
        for (int i = 0; i < cols.length; i++) {
            System.out.printf("| %-" + widths[i] + "s ", cols[i]);
        }
        System.out.println("|");
        System.out.println(line);
        for (Row row : rows) {
            for (int i = 0; i < cols.length; i++) {
                System.out.printf("| %-" + widths[i] + "s ", String.valueOf(row.getValue(cols[i])));
            }
            System.out.println("|");
        }
        System.out.println(line);
    }

    private void executeInsert(String query) {
        Pattern p = Pattern.compile(
                "INSERT\\s+INTO\\s+(\\w+)\\s*(?:\\((.+?)\\))?\\s+VALUES\\s*\\((.+)\\);?$",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(query);
        if (!m.find()) {
            System.out.println("Invalid INSERT syntax");
            return;
        }

        String tableName = m.group(1);
        String colsStr = m.group(2);
        String valsStr = m.group(3);

        Table table = db.getTable(tableName);
        if (table == null) {
            System.out.println("Table not found: " + tableName);
            return;
        }

        List<String> colNames = new ArrayList<>();
        if (colsStr != null) {
            for (String c : colsStr.split(",")) {
                colNames.add(c.trim());
            }
        } else {
            for (Column c : table.getColumns()) {
                colNames.add(c.getName());
            }
        }

        List<String> values = splitValues(valsStr);
        if (values.size() != colNames.size()) {
            System.out.println("Column count doesn't match values count");
            return;
        }

        Row row = new Row();
        for (int i = 0; i < colNames.size(); i++) {
            String colName = colNames.get(i);
            String raw = values.get(i).trim();
            Column col = findColumn(table, colName);
            if (col == null) {
                System.out.println("Unknown column: " + colName);
                return;
            }
            row.setValue(colName, parseTypedValue(raw, col.getDataType()));
        }

        try {
            db.insert(tableName, row);
            System.out.println("1 row inserted");
        } catch (Exception e) {
            System.err.println("Insert error: " + e.getMessage());
        }
    }

    private Column findColumn(Table table, String name) {
        for (Column c : table.getColumns()) {
            if (c.getName().equals(name)) return c;
        }
        return null;
    }

    private List<String> splitValues(String s) {
        List<String> result = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (char ch : s.toCharArray()) {
            if (ch == '\'') inQuotes = !inQuotes;
            if (ch == ',' && !inQuotes) {
                result.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        result.add(cur.toString());
        return result;
    }

    private void executeUpdate(String query) {
        Pattern p = Pattern.compile(
                "UPDATE\\s+(\\w+)\\s+SET\\s+(.+?)\\s+WHERE\\s+(\\w+)\\s*=\\s*(.+?);?$",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(query);
        if (!m.find()) {
            System.out.println("Invalid UPDATE syntax");
            return;
        }

        String tableName = m.group(1);
        String setStr = m.group(2);
        String whereCol = m.group(3);
        String whereValStr = m.group(4);

        Table table = db.getTable(tableName);
        if (table == null) {
            System.out.println("Table not found");
            return;
        }

        Map<String, Object> newValues = new HashMap<>();
        for (String part : setStr.split(",")) {
            String[] kv = part.split("=", 2);
            if (kv.length != 2) {
                System.out.println("Invalid SET clause");
                return;
            }
            String colName = kv[0].trim();
            Column col = findColumn(table, colName);
            if (col == null) {
                System.out.println("Unknown column: " + colName);
                return;
            }
            newValues.put(colName, parseTypedValue(kv[1].trim(), col.getDataType()));
        }

        Column condCol = findColumn(table, whereCol);
        if (condCol == null) {
            System.out.println("Unknown column: " + whereCol);
            return;
        }

        Object condValue = parseTypedValue(whereValStr.trim(), condCol.getDataType());

        try {
            int count = db.update(tableName, whereCol, condValue, newValues);
            System.out.println(count + " rows updated");
        } catch (Exception e) {
            System.err.println("Update error: " + e.getMessage());
        }
    }

    private void executeDelete(String query) {
        Pattern p = Pattern.compile(
                "DELETE\\s+FROM\\s+(\\w+)\\s+WHERE\\s+(\\w+)\\s*=\\s*(.+?);?$",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(query);
        if (!m.find()) {
            System.out.println("Invalid DELETE syntax");
            return;
        }

        String tableName = m.group(1);
        String whereCol = m.group(2);
        String whereValStr = m.group(3);

        Table table = db.getTable(tableName);
        if (table == null) {
            System.out.println("Table not found");
            return;
        }

        Column col = findColumn(table, whereCol);
        if (col == null) {
            System.out.println("Unknown column: " + whereCol);
            return;
        }

        Object value = parseTypedValue(whereValStr.trim(), col.getDataType());

        try {
            int count = db.delete(tableName, whereCol, value);
            System.out.println(count + " rows deleted");
        } catch (Exception e) {
            System.err.println("Delete error: " + e.getMessage());
        }
    }

    private void executeCreateIndex(String query) {
        Pattern p = Pattern.compile(
                "CREATE\\s+INDEX\\s+(\\w+)\\s+ON\\s+(\\w+)\\s*\\((.+?)\\);?$",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(query);
        if (!m.find()) {
            System.out.println("Invalid CREATE INDEX syntax");
            return;
        }

        String tableName = m.group(2);
        String colName = m.group(3).trim();

        try {
            db.createIndex(tableName, colName);
            System.out.println("Index created on " + tableName + "(" + colName + ")");
        } catch (Exception e) {
            System.err.println("Index error: " + e.getMessage());
        }
    }

    private void executeCreateTable(String query) {
        Pattern p = Pattern.compile(
                "CREATE\\s+TABLE\\s+(\\w+)\\s*\\((.+)\\);?$",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(query);
        if (!m.find()) {
            System.out.println("Invalid CREATE TABLE syntax");
            return;
        }

        String tableName = m.group(1);
        String colsDef = m.group(2);

        if (db.getTable(tableName) != null) {
            System.out.println("Table already exists: " + tableName);
            return;
        }

        Table table = new Table(tableName);
        for (String def : colsDef.split(",")) {
            String[] tokens = def.trim().split("\\s+");
            if (tokens.length < 2) {
                System.out.println("Bad column def: " + def);
                return;
            }

            String colName = tokens[0];
            DataType type;
            try {
                type = DataType.valueOf(tokens[1].toUpperCase());
            } catch (Exception e) {
                System.out.println("Unknown type: " + tokens[1]);
                return;
            }

            boolean pk = def.toUpperCase().contains("PRIMARY KEY");
            boolean nullable = !pk;
            table.addColumn(new Column(colName, type, pk, nullable));
        }

        db.addTable(table);
        System.out.println("Table created: " + tableName);
    }

    private void executeDropTable(String query) {
        Pattern p = Pattern.compile("DROP\\s+TABLE\\s+(\\w+);?$", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(query);
        if (!m.find()) {
            System.out.println("Invalid DROP TABLE syntax");
            return;
        }

        try {
            db.removeTable(m.group(1));
            System.out.println("Table dropped: " + m.group(1));
        } catch (Exception e) {
            System.err.println("Drop error: " + e.getMessage());
        }
    }

    private void executeShowIndexes() {
        Map<String, DatabaseIndex> indexes = db.getIndexes();
        if (indexes.isEmpty()) {
            System.out.println("No indexes found.");
            return;
        }

        System.out.println("Indexes:");
        for (DatabaseIndex index : indexes.values()) {
            System.out.println(" - Table: " + index.getTableName() + ", Column: " + index.getColumnName());
        }
    }

    private void executeShowForeignKeys() {
        List<ForeignKey> fks = db.getForeignKeys();
        if (fks.isEmpty()) {
            System.out.println("No foreign keys found.");
            return;
        }

        System.out.println("Foreign Keys:");
        for (ForeignKey fk : fks) {
            System.out.println(" - " + fk.getSourceTableName() + "." + fk.getColumnName() +
                    " -> " + fk.getReferencedTableName() + "." + fk.getReferencedColumnName());
        }
    }

    private Object parseValue(String str) {
        str = str.trim();
        if (str.equalsIgnoreCase("TRUE") || str.equalsIgnoreCase("FALSE")) {
            return Boolean.parseBoolean(str);
        }
        if (str.startsWith("'") && str.endsWith("'")) {
            return str.substring(1, str.length() - 1);
        }
        try {
            if (str.contains(".")) return Double.parseDouble(str);
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return str;
        }
    }

    private Object parseTypedValue(String str, DataType type) {
        str = str.trim();
        if (str.startsWith("'") && str.endsWith("'")) {
            str = str.substring(1, str.length() - 1);
        }
        return switch (type) {
            case INTEGER -> Integer.parseInt(str);
            case DOUBLE -> Double.parseDouble(str);
            case STRING -> str;
            case BOOLEAN -> Boolean.parseBoolean(str);
            case DATE -> LocalDate.parse(str);
        };
    }
}