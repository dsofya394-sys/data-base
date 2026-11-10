package com.db.Model;

import java.io.Serial;
import java.io.Serializable;

public class Column implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String name;
    private final DataType type;
    private final boolean primaryKey;
    private final boolean nullable;

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