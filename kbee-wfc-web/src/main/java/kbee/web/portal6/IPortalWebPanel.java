package kbee.web.portal6;

import java.util.Map;

/**
 * All Rendered panels managed by Portals 
 * must implement this interface
 * 
 * 
 */
public interface IPortalWebPanel {
	
	
	public Map<String, String> getParameters();
	public void setParameters(Map<String, String> map);
	
	
}
