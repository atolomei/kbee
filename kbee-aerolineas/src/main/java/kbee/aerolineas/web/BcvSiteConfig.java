package kbee.aerolineas.web;

import com.novamens.portal6.model.SiteConfig;

public class BcvSiteConfig implements SiteConfig {
	private static final long serialVersionUID = 1L;

	public Class<?> getResourceClass() {
    	return BcvSiteConfig.class; 
    }
    
    public String getCssClass() {
    	return "bcv";
    }
    
    public boolean isExplorer() {
        return true;
    }
    
    @Override
    public boolean isReadOnly() {
    	return false;
    }
}
