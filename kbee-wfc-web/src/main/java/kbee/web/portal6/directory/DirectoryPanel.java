package kbee.web.portal6.directory;

import org.apache.wicket.model.IModel;

import com.novamens.content.entity.Person;

import kbee.web.portal6.panel.PortalPanel;

public class DirectoryPanel extends PortalPanel<Person> {
			
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger =  kbee.util.logging.Logger.getLogger(DirectoryPanel.class.getName());

	public DirectoryPanel(String id, IModel<Person> model) {
		super(id);
		setModel(model);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		add(new DirectoryTagSelector("tagspanel"));
		add(new DirectoryListPanel("listpanel", getModel()));
	}
}
