package com.novamens.kbee.bulkImport;

import com.novamens.util.KeyValue;

import java.util.List;

public class EntityRowColumnsDefinition {
    private Integer index;
    private String columnKey;
    private String displayName;
    private List<KeyValue<String>> possibleValues;
    private ColumnType columnType;


    public EntityRowColumnsDefinition(Integer index, String columnKey, String displayName, ColumnType columnType) {
        this.index = index;
        this.columnKey = columnKey;
        this.displayName = displayName;
        this.columnType = columnType;
    }

    public EntityRowColumnsDefinition(Integer index, String columnKey, List<KeyValue<String>> possibleValues, ColumnType columnType) {
        this.index = index;
        this.columnKey = columnKey;
        this.possibleValues = possibleValues;
        this.columnType = columnType;
    }

    public EntityRowColumnsDefinition(Integer index, String columnKey, String displayName, List<KeyValue<String>> possibleValues, ColumnType columnType) {
        this.index = index;
        this.columnKey = columnKey;
        this.displayName = displayName;
        this.possibleValues = possibleValues;
        this.columnType = columnType;
    }

    List<KeyValue<String>> getPossibleValues() {
        return possibleValues;
    }

    String getColumnKey() {
        return columnKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ColumnType getColumnType() {
        return columnType;
    }

    public Integer getIndex() {
        return index;
    }

    public enum ColumnType {
        NATIVE(""),
        CLASSIFIER("clf."),
        ATTRIBUTE("atr."),
        ROLE("role.");
        private String columnPrefix;

        ColumnType(String columnPrefix) {
            this.columnPrefix = columnPrefix;
        }

        public String getColumnPrefix() {
            return columnPrefix;
        }
    }
}
