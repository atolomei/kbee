package kbee.web.portal6;

import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.PortalService;
import com.novamens.service.BusinessSystemService;

public interface PortalMVCService extends PortalService, BusinessSystemService {

							
	public void register(String key, String viewer_class_name, String dataprovider_class_name);
	
	
	public void registerViewer(String key, String class_name);
	public void registerDataProvider(String key, String class_name);
	
	
	public Panel getViewer(String key, String id, PortalObject p_obj);
	public Panel getEditor(String key, String id, PortalObject p_obj);
	
	
	
	
	
}
