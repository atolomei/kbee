package kbee.web.query;

import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.hibernate.query.HibernateQuery;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

public class DomainHQLQuery extends HibernateQuery {

	private static final long serialVersionUID = 8848870258962741966L;

	public DomainHQLQuery() {
	}
	
	public Domain getDomain() {
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		Domain domain = profile.getDomain();
		return domain;
	}
	
	public User getUser() {
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		return profile.getUser();
	}
	
}
