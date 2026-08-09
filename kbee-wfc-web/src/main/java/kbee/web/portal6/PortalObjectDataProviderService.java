package kbee.web.portal6;

import java.util.Map;

import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.portal6.model.PortalDataProvider;
import com.novamens.portal6.model.PortalModel;
import com.novamens.portal6.model.PortalService;
import com.novamens.portal6.model.PortalViewRender;
import com.novamens.service.ObjectService;

/**
 * 
 * 
 * Standard MVC architecture:
 * 
 * {@link PortalModel}
 * {@link PortalDataProvider}
 * {@link PortalViewRender}
 * 
 *
 */
 
public interface PortalObjectDataProviderService  extends PortalService, ObjectService {

	
	public Panel getDataProviderEditor(String id);
	public Panel getDataProviderEditor(String id, Map<String, String> parameters);
	
}
