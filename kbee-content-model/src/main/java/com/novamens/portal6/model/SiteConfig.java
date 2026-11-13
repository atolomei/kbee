package com.novamens.portal6.model;

import java.io.Serializable;

public interface SiteConfig extends Serializable {
	
    public Class<?> getResourceClass();
    public String getCssClass();
    
    default boolean isExplorer() {
        return false;
    }
    
    default boolean isReadOnly() {
        return false;
    }
}
