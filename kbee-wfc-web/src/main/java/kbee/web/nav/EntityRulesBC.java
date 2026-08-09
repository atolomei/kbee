package kbee.web.nav;

import com.novamens.wicket.util.BCElement;

public class EntityRulesBC extends BCElement {
	private static final long serialVersionUID = 1L;
	
	public EntityRulesBC() {
		super("bc.entityrules");
	}
	
	@Override
	public void onClick() {
	    //setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("alert-management-action-rules-page"));
	}
	
	@Override
	public boolean isEnabled() {
		return false;
	}
}