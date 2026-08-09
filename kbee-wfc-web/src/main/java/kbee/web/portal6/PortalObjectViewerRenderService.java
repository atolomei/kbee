package kbee.web.portal6;

import java.util.Map;

import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.portal6.model.PortalService;
import com.novamens.portal6.model.PortalViewMode;
import com.novamens.service.ObjectService;

public interface PortalObjectViewerRenderService  extends PortalService, ObjectService {

	public Panel build();
	public Panel build(String id);
	public Panel build(String id, PortalViewMode view_mode);
	public Panel build(String id, int tab_index, PortalViewMode view_mode,Map<String, String> parameters);
	public Panel getMetaInfoPanel(String string);
	
}
