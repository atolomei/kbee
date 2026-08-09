package kbee.web.portal6;

import java.util.Map;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.PortalViewMode;

public interface IAreaWebPanel extends IPortalWebPanel {

	public Panel getHeaderPanel();
	public Panel getHeaderPanel(PortalViewMode viewMode,  Map<String, String> parameters);
	
	public Panel getBodyPanel();
	public Panel getBodyPanel(PortalViewMode viewMode,  Map<String, String> parameters);
	
	public IModel<String> getClassInfo();
	
}
