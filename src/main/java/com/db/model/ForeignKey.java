package com.db.model;

import java.io.Serializable;
import java.util.Objects;

public class ForeignKey implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sourceTableName;
    private String columnName;
    private String referencedTableName;
    private String referencedColumnName;

    public ForeignKey(String sourceTableName,
                      String columnName,
                      String referencedTableName,
                      String referencedColumnName) {

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
