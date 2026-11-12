package com.db.Model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class ForeignKey implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String sourceTableName;
    private final String columnName;
    private final String referencedTableName;
    private final String referencedColumnName;

    public ForeignKey(String sourceTableName, String columnName,
                      String referencedTableName, String referencedColumnName) {
        this.sourceTableName = Objects.requireNonNull(sourceTableName);
        this.columnName = Objects.requireNonNull(columnName);
        this.referencedTableName = Objects.requireNonNull(referencedTableName);
        this.referencedColumnName = Objects.requireNonNull(referencedColumnName);
    }

    public String getSourceTableName() {
        return sourceTableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getReferencedTableName() {
        return referencedTableName;
    }

    public String getReferencedColumnName() {
        return referencedColumnName;
    }
}