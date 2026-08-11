package kbee.web.enoti;

import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.content.web.security.markup.GroupStandAlonePage;
import com.novamens.content.web.suggestion.service.UserSuggestionService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.form.RelationEditor;
import kbee.web.security.service.PrincipalSuggestionService;
import kbee.web.security.user.UserStandAlonePage;

@SuppressWarnings("serial")
public class ReceiversEditor<T> extends RelationEditor<T, Principal> {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(ReceiversEditor.class.getName());
	
	public ReceiversEditor(String id) {
		super(id, "receivers");
	}
	
	@Override
	protected String getStringValue(Object value) {
		if (value instanceof KbeeUser) {
			return ((KbeeUser) value).getLastFirstName();
		}
		else  if (value instanceof KbeeGroup) {
			return ((KbeeGroup) value).getDisplayName();
		}
		return super.getStringValue(value);
	}
	
	/**
	 * Provider of candidate elements
	 **/
	public List<Suggestion> getSuggestions(String pattern) {
		//return ServiceLocator.getService(PrincipalSuggestionService.class).getSuggestions(pattern); 
		return ServiceLocator.getService(UserSuggestionService.class).getSuggestions(pattern); 
	}
	
	@Override
	public boolean ordered() {
		return true;
	}
	
	protected String getPropertyLabel(String property_name) {
		return new StringResourceModel("user-or-group", ReceiversEditor.this, null).getObject();
	}

	@Override
	protected Property<?> getKey() {
		return new Property<Principal>() {
			public String getName() {
				return "receivers";
			}
			public boolean isAutocomplete() {
				return true;
			}
			public List<Suggestion> getSuggestions(String pattern) {
				return ReceiversEditor.this.getSuggestions(pattern);
			}
			@Override
			public String getHistoryKey() {
				return null;
			}
		};
	}
	
	@Override
	protected int compare(IModel<Principal> a, IModel<Principal> b) {
		try {
			if (a.getObject().getName()==null)
				return (b.getObject().getName()!=null?1:0);
			else if(b.getObject().getName()==null)
				return -1;
			return a.getObject().getName().compareToIgnoreCase(b.getObject().getName());
		} 
		catch (Exception e) {
			logger.error(e);
			return 0;
		}
	}
	
	@Override
	protected void onValueClick(IModel<Principal> model) {
							
		final boolean is_root	 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
		final boolean role_admin 	= is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		final boolean role_security	= role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
		
		if (role_security) {
			if (model.getObject() instanceof User) {
				User user = (User) model.getObject();
				UserProfile up = getContentDao().findUserProfileByUser(user);
				setResponsePage(new UserStandAlonePage( new ObjectModel<Person>(up.getPerson())));
			}
			else if (model.getObject() instanceof Group) {
				Group group = (Group) model.getObject();
				setResponsePage(new GroupStandAlonePage(new ObjectModel<Group>(group)));
			}
			else													
				setResponsePage(new ApplicationErrorPage<Object>( new Model<String>("Invalid Type"), new Model<String>("Principal must be User or Group")));
		}
		else
			setResponsePage(new ApplicationErrorPage<Object>( new Model<String>("Your user account doesn't have rights to read " + model.getObject().getName()), new Model<String>("Groups")));
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}