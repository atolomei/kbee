package kbee.web.dashboard;

import org.apache.wicket.model.IModel;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.portal6.model.PageSection;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;

public class DashboardPanel extends KBPanel {

	private static final long serialVersionUID = 1L;

	String name;
	IModel<Person> person; 
	
	public DashboardPanel(String id, String name) {
		super(id);
		this.name=name;
		Person person = ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
		this.person = new ObjectModel<Person>(person);
	}

	
	
	public void onInitialize() {
		super.onInitialize();
		
		
		/**
 		Person person = getPersonDashboard();
	
		Page -> getPage(person, key)
		page.service.build()
		add();
		PageSection p_section = getPageSection(person, this.name);
		p_section.getService(PortalPanelBuilder.class).build();
		Panel panel = p_section.build();
		add(panel);
		
		
		**/
		
	}
	
	
	public Person getPersonDashboard() {
		return this.person.getObject();
	}

	
}
