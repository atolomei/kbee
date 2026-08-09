package kbee.web.portal6.editor;

import java.util.Map;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.PortalPersistentMenu;

import kbee.web.portal6.panel.PortalPanel;

public class PortalMenuItemPanel extends PortalPanel<PortalPersistentMenu> {
	
	private static final long serialVersionUID = 1L;

	public PortalMenuItemPanel(String id, IModel<PortalPersistentMenu> model) {
		super(id, model);
	}
	
	Label title;
	Label href;
	
	public void onInitialize() {
		super.onInitialize();
		
		title = new Label("title");
		href =  new Label("href");
		
	}

}
