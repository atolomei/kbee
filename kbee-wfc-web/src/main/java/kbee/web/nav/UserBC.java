package kbee.web.nav;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
//import com.novamens.content.web.entity.markup.UserPage;
//import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

@SuppressWarnings("serial")
public class UserBC extends BCElement {
	private static final long serialVersionUID = 1L;
	
	private IModel<User> model;

	public UserBC(User user) {
		model = new ObjectModel<User>(user);
	}
	
	public UserBC() {
		model = new ObjectModel<User>(getSessionUser());
	}
	
	@Override
	public void onClick() {
		// UserProfile profile = getContentDao().findUserProfileByUser(getUserModel().getObject());
		//if (profile!=null) {
			//Person person = (Person)profile.getEntity();
			PageParameters pa= new PageParameters();
		    //pa.add("id", person.getProfile(UserProfile.class).getUser().getId().toString());
			
			pa.add("id", getUserModel().getObject().getId().toString());
		    setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("security-user-page", pa));
		// }
	}
	
	public IModel<User> getUserModel() {
		return model;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		model.detach();
	}
	
	@Override
	protected IModel<String> newLabel() {
		return new Model<String>() {
			public String getObject() {
				return UserBC.this.getUserModel().getObject().getLastFirstName();
			}
		};
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
