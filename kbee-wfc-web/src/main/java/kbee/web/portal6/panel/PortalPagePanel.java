package kbee.web.portal6.panel;

import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.Page;

import kbee.web.portal6.IPageWebPanel;

public class PortalPagePanel extends PortalPanel<Page> implements IPageWebPanel {

	private static final long serialVersionUID = 1L;

	public PortalPagePanel(String id, IModel<Page> model) {
		super(id, model);
	}

	@Override
	public IModel<String> getClassInfo() {
		return null;
	}

}
