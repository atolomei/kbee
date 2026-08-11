package com.novamens.kbee.content.command;

public enum TagOperation {
    add,
    replace,
    remove;

    public String getOperationDescription(String tagChanged, String tagValue){
        switch (this) {
            case add:
                return  String.format("Tag Tool [ %s ]. %s ->  %s", "add", tagChanged, tagValue);
            case replace:
                return  String.format("Tag Tool [ %s ]. %s ->  %s", "set", tagChanged, tagValue);
            case remove:
                return  String.format("Tag Tool [ %s ]. %s", "remove", tagChanged);
            default:
                throw new RuntimeException("Operation Description not available.");
        }
    }
}
