package com.db.model;

import java.io.Serializable;

public class Column implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private DataType type;
    private boolean primaryKey;
    private boolean nullable;

    public Column(String name, DataType type, boolean primaryKey, boolean nullable) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Column name cannot be empty");
        }

        if (type == null) {
            throw new IllegalArgumentException("Column type cannot be null");
        }

        if (primaryKey && nullable) {
            throw new IllegalArgumentException("Primary key cannot be nullable");
        }

        this.name = name;
        this.type = type;
        this.primaryKey = primaryKey;
        this.nullable = nullable;
    }

    public String getName() {
        return name;
    }

    public DataType getDataType() {
        return type;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isNullable() {
        return nullable;
    }
}
