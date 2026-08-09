package kbee.web.searcher.panel;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;

public class SearcherPanel extends KBPanel {

	private static final long serialVersionUID = 1L;

	String name;
	
	public SearcherPanel(String id, String name) {
		super(id);
		this.name=name;
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	public String getName() {
		return name;
	}
	
}
