package kbee.web.console;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
 
public abstract class AdvancedSearchSelectorEditor<T> extends ObjectEditor<T> {
	private static final long serialVersionUID = 1L;

	private Boolean  is_domain_kbee = null;
	
	public AdvancedSearchSelectorEditor(String id) {
		super(id);
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	protected boolean isDomainKbee() {
		if (this.is_domain_kbee == null) {
			try {
				this.is_domain_kbee = Boolean.valueOf(
						getPerson().getDomain().getName().toLowerCase().trim().equals("kbee"));
			} catch (Exception e) {
				this.is_domain_kbee = Boolean.valueOf(false);
			}
		}
		return this.is_domain_kbee.booleanValue();
	}

	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}

	
	protected User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} catch (Exception e) {
			return null;
		}
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}


	protected abstract void clearAll();
	

}
