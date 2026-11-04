package com.db.model;

import java.io.Serializable;

public class Column implements java.io.Serializable {

    private static final long SerialVersionUID = 1L;
    private String name;
    private DataType type;
    private boolean PrimaryKey;
    private boolean nullable;

    public Column(String name, DataType type, boolean PrimaryKey, boolean nullable){
        this.name = name;
        this.type = type;
        this.PrimaryKey = PrimaryKey;
        this.nullable = nullable;
    }

    public String GetName(){
        return name;
    }

    public DataType GetDataType(){
        return type;
    }

    public boolean IsPrimaryKey(){
        return PrimaryKey;
    }

    public boolean IsNullable(){
        return nullable;
    }


}
