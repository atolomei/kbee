package com.novamens.portal6.model;

import java.io.Serializable;

public interface PortalConfig extends Serializable {
    public Class<?> getResourceClass();
    public String getCssClass();
}
