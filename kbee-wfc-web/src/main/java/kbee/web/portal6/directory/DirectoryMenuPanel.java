package kbee.web.portal6.directory;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.entity.Person;

import kbee.web.portal6.panel.PortalPanel;

public class DirectoryMenuPanel extends PortalPanel<Person> {
	private static final long serialVersionUID = 1L;
	
	public DirectoryMenuPanel(String id, IModel<Person> model) {
		super(id);
		setModel(model);
		this.setOutputMarkupId(true);
	}

	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		add(getPanel("sites"));
	}

	protected Panel getPanel(String panelId) {
		return new DirectoryPanel(panelId, getModel());
	}
}
