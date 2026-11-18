package kbee.pjsf.web;

import com.novamens.portal6.model.SiteConfig;

public class PjsfSiteConfig implements SiteConfig {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Class<?> getResourceClass() {
    	return PjsfSiteConfig.class; 
    }
    
    public String getCssClass() {
    	return "pjsf";
    }
    
    @Override
    public boolean isReadOnly() {
    	return true;
    }
}
