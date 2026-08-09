package kbee.web.portal6.editor;

import java.util.List;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.PortalPersistentMenu;

public class PortalMenuItemSubmenuPanel extends PortalMenuItemPanel {

	private static final long serialVersionUID = 1L;
	
	private List<Panel> panels;
	
	public PortalMenuItemSubmenuPanel(String id, IModel<PortalPersistentMenu> model) {
		super(id, model);
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		// getModel().getObject()
		
	}
	
	
	
	
}
