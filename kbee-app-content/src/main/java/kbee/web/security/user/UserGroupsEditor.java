package kbee.web.security.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.security.user.UserMainPanel.NullUser;

@SuppressWarnings("serial")
public class UserGroupsEditor extends ObjectEditor<User> {
				
	private static final long serialVersionUID = 1L;
	
	static private Logger logger = LogManager.getLogger(UserGroupsEditor.class.getName());

	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_security_admin			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());

	
	public UserGroupsEditor(String id, IModel<User> model, final boolean readonly) {
		super(id, model);
		
		setOutputMarkupId(true);
		setEditionEnabled(false);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new GroupsEditor());
		
		add(form);
		
		add(new EditButtonsV5<User>(this) {
			@Override
			public boolean isVisible() {
				
				if (getPerson(UserGroupsEditor.this.getModel().getObject()).getState()==ObjectState.DELETED)
					return false;
				
				// only root can edit root values
				if  (UserGroupsEditor.this.getModel().getObject().getUserName().startsWith("root@"))
					return getSessionUser().getUserName().startsWith("root@");

				
				return !readonly && !(getModelObject() instanceof NullUser);
			}
			
			@Override
			public boolean isEnabled() {
				return is_root || is_domain_admin || is_security_admin; 
			}
		});
		
		add( new WebMarkupContainer("readonly") {
			public boolean isVisible() {
				return readonly;
			}
		});
	}
	
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				ServiceLocator.getService(SecurityContentMgmtService.class).update(getModelObject(), getUpdatedParts());
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent(target, e));

		}
	}
	
	protected Person getPerson(User user) {
		return getContentDao().findUserProfileByUser(user).getPerson();
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected KbeeUser getSessionUser() {
		try {
			return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
 		} catch (Exception e) {
			logger.error(" {} | {} | {} | {}", "getSessionUser() gave the error", e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
			return null;
		}
	}

	
}
