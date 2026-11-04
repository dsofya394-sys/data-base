package com.db.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Row implements Serializable{

    private static final long SerialVersionUID = 1L;
    private final Map<String, Object> values;

    public Row(){
        values = new LinkedHashMap<>();
    }

    public void SetValue(String ColumnName, Object value){
        values.put(ColumnName, value);
    }

    public Object GetValue (String ColumnName){
        return values.get(ColumnName);
    }

    public Map<String, Object> getValues(){
        return Collections.unmodifiableMap(values);
    }
}
