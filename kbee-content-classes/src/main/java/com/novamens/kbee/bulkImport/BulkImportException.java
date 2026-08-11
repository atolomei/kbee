package com.novamens.kbee.bulkImport;

public class BulkImportException extends Exception {
    private Integer fieldIdx;

    public BulkImportException(String message, Integer fieldIdx) {
        super(message);
        this.fieldIdx = fieldIdx;
    }

    public Integer getField() {
        return fieldIdx;
    }
}
