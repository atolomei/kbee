package com.novamens.kbee.bulkImport;

public class RowEntityValues {
    private Integer columnIdx;
    private String columnKey;
    private String value;

    public RowEntityValues() {
    }

    public RowEntityValues(Integer columnIdx, String columnKey, String value) {
        this.columnIdx = columnIdx;
        this.columnKey = columnKey;
        this.value = value;
    }

    public Integer getColumnIdx() {
        return columnIdx;
    }

    public void setColumnIdx(Integer columnIdx) {
        this.columnIdx = columnIdx;
    }

    public String getColumnKey() {
        return columnKey;
    }

    public void setColumnKey(String columnKey) {
        this.columnKey = columnKey;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
