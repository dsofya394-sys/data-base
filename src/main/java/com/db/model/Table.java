package com.db.model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Table implements Serializable {

    private static final long SerialVersionUID = 1L;
    private String name;
    private List<Column> columns;
    private List<Row> rows;

    public Table(String name){
        this.name = name;
        this.columns = new ArrayList<>();
        this.rows = new ArrayList<>();
    }

    public String GetName(){
        return name;
    }

    public List<Column> GetColumns(){
        return Collections.unmodifiableList(columns);
    }

    public List<Row> GetRows(){
        return Collections.unmodifiableList(rows);
    }

    public void AddColumn(Column column){
        for (Column existing: columns) {
            if(existing.GetName().equals(column.GetName())){
                throw new IllegalArgumentException(
                        "Column is already exists: " + column.GetName()
                );
            }
        }

        if (!rows.isEmpty()){
            throw new IllegalStateException(
                    "You can not add columns in non empty table"
            );
        }

        columns.add(column);
    }

    public void insert(Row row) {
        rows.add(row);
    }
}
