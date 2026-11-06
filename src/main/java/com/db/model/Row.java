package com.db.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Row implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<String, Object> values;

    public Row() {
        values = new LinkedHashMap<>();
    }

    public void setValue(String columnName, Object value) {
        values.put(columnName, value);
    }

    public Object getValue(String columnName) {
        return values.get(columnName);
    }

    public Map<String, Object> getValues() {
        return Collections.unmodifiableMap(values);
    }
}
