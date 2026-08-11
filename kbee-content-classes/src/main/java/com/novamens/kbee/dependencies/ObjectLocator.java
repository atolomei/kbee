package com.novamens.kbee.dependencies;

public interface ObjectLocator {
    public enum TargetObjectType {
        DATASET,
        DATASETMEMBER,
        CLASSIFIER,
        ATTRIBUTE,
        SYSTEMPARAMETER
    }

    Object resolveObject();
    String resolveId();
    TargetObjectType getTargetObjectType();
    String getDescription();
}
