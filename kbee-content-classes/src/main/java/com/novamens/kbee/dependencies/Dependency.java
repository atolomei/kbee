package com.novamens.kbee.dependencies;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.service.ServiceLocator;

import java.io.Serializable;


public class Dependency implements Serializable {
    public enum SourceObjectType{
        REPORT
    }

    private String dependencyName;
    private String dependancyDescription;
    private SourceObjectType sourceType;

    private ObjectLocator targetLocator;

    public Dependency(String dependencyName, String dependancyDescription, ObjectLocator targetLocator) {
        this.dependencyName = dependencyName;
        this.dependancyDescription = dependancyDescription;
        this.targetLocator = targetLocator;
    }

    public String getDependencyName() {
        return dependencyName;
    }

    public String getDependancyDescription() {
        return dependancyDescription;
    }

    public SourceObjectType getSourceType() {
        return sourceType;
    }

    public ObjectLocator getTargetLocator() {
        return targetLocator;
    }
}
