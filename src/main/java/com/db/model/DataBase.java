package com.db.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataBase implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;
    private List<Table> tables;

    public DataBase(String name){
        this.name = name;
        this.tables = new ArrayList<>();
    }

    public String GetName(){
        return name;
    }

    public List<Table> GetTables(){
        return Collections.unmodifiableList(tables);
    }

    public void AddTable(Table table){
        for(Table existingTable: tables){
            if(existingTable.GetName().equals(table.GetName())){
                throw new IllegalArgumentException(
                        "Table with this name is already exists"
                );
            }
        }

        tables.add(table);
    }

    public Table GetTable(String TableName){
        for (Table table: tables)
            if(table.GetName().equals(TableName)){
                return table;
            }
        return null;
    }

    public void RemoveTable(String TableName){
        Table table = GetTable(TableName);
        if (table == null){
            throw new IllegalArgumentException(
                    "Table is not found"
            );
        }
        tables.remove(table);
    }
}
