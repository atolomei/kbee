package kbee.web.portal6;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public interface IPageWebPanel extends IPortalWebPanel {

	//public Panel getHeaderPanel();
	//public Panel getHeaderPanel(PortalViewMode viewMode);
	//public Panel getBodyPanel();
	//public Panel getBodyPanel(PortalViewMode viewMode);
	
	public IModel<String> getClassInfo();

}
