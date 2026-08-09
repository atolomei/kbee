package kbee.web.portal6.factory;

import org.apache.wicket.markup.html.panel.Panel;



public interface PortalObjectInternalPanelFactory {
	
	public Panel create();
	public Panel create(String id);
	
	public String 	getTitle();
	public String 	getDisplayName();
	public String 	getId();
	public String 	getKey();
	public String 	getClassName();
	
}
