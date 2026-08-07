package com.novamens.content.web.security.markup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.Role;
import com.novamens.content.security.RolesService;
import com.novamens.content.service.DomainService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.security.KbeeDomainRole;
import com.novamens.kbee.content.user.KbeeUserRole;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.console.BaseBrowser;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;


			
public class UsersBatchSetGlobalRolePanel extends ObjectEditor<Domain> {

	private static final long serialVersionUID = 1L;
																
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UsersBatchSetGlobalRolePanel.class.getName());
	
	List<String> roles = null;
	Map<String, Role> labels = null;
	
	private BaseBrowser<?> browser;
	public IModel<String> group_id = new Model<String>();
	private Boolean value = Boolean.valueOf(true);
	
	/**
	 *  Permission: 
	 *  yes/no
	 *  [cancel] [save]
	 * 
	 * @param id
	 * @param model
	 */
	
	public UsersBatchSetGlobalRolePanel(String id, IModel<Domain> model, BaseBrowser<?> browser) {
		super(id, model);
		this.browser=browser;
	}

	public IModel<String> getRole_id() {
		return group_id;
	}

	public void setGroup_id(IModel<String> group_id) {
		this.group_id = group_id;
	}
	
	public boolean getValue() {
		return value;
	}
	
	public void setValue(Boolean b) {
		this.value=b;
	}
	
	public BaseBrowser<?> getBrowser() {
		return this.browser;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		String size = String.valueOf(getBrowser().getSelection().size());
		
		List<String> exceptions = getSystemUsers();
		StringBuilder str = new StringBuilder();
		
		if (!exceptions.isEmpty()) {
			if (exceptions.size()==1)
				str.append("<br/>The following user is reserved and can't be modified: ");
			else
				str.append("<br/>The following are reserved users and can't be modified: ");
			int n = 0;
			for (String s: exceptions) {
				if (n++>0) 
						str.append(", ");
				str.append(s);
			}
			str.append(". ");
		}

		Label msg = new Label("msg", "The value will be applied to the " + ((getBrowser().getSelection().size()>1?size:"")) + " user" +(getBrowser().getSelection().size()>1?"s":"")+" selected. " + (exceptions.isEmpty()?"":str.toString()));
		msg.setEscapeModelStrings(false);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);

		add(form);
		form.add(msg);
		
		ChoiceField<String> selector = new ChoiceField<String>("group_id", 
															    group_id,
															    new PropertyModel<List<String>>(this, "roles")
		) {
			private static final long serialVersionUID = 1L;
			@Override
			protected String getDisplayValue(String value) {
				return labels.get(value).getDisplayName();
			}
		};
				
		selector.setRequired(true);
		form.add(selector);
		
		form.add(new BooleanField("value",	new PropertyModel<Boolean>(this, "value")));

		add(new EditButtonsV5<Domain>(this, false) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
 				return true;
			}
			@Override
			protected String getCancelClass() {
				return "btn btn-default btn-sm";
			}
			
			@Override
			protected String getSubmitClass() {
				return "btn btn-primary btn-sm";
			}

		});
	}
	


	@Override
	public void cancel(AjaxRequestTarget target) {
			close(target);
	}

	
	
		
	@Override
	public void update(AjaxRequestTarget target) {
		
		try {

			if ( getBrowser().getSelection().isEmpty() ||  
			     (this.getRole_id()==null) || (labels.get(this.getRole_id().getObject())==null)) 
				return;
				
				Boolean is_apply = getValue();
				Role role = labels.get(this.getRole_id().getObject());
				
				for (IModel<?> mo:  getBrowser().getSelection()) {

					Person person = (Person) mo.getObject();
					
					if (person==null) 
						continue;

					UserProfile 	user_profile = person.getProfile(UserProfile.class);
					User		 	user 		 = person.getProfile(UserProfile.class).getUser();
					
					if (user==null || user.getUserName()==null || isReservedUserName(user.getUserName()))	
						continue;
						
					if (is_apply)
						addRole(user_profile, role);
					else
						removeRole(user_profile, role);
				}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent(target, e));
		}
		finally {
			close(target);
		}
	}
		
	protected void close(AjaxRequestTarget target) {}


	public List<String> getSystemUsers() {
		List<String> list = new ArrayList<String>();
		for (IModel<?> xmodel: getBrowser().getSelection()) {
			Person person = (Person) xmodel.getObject();
			if (person!=null && person.getProfile(UserProfile.class).getUser()!=null) {
				String username = person.getProfile(UserProfile.class).getUser().getUserName();
				if ((username!=null) && username.startsWith("root@")) 
					list.add(person.getFirstLastName()+ " (" +username+")");
				else if ((username!=null) && username.startsWith(DomainService.WORKFLOW_USER+"@"))
					list.add(person.getFirstLastName()+ " (" +username+")");
			}
		}
		return list;
	}
	
	@Override
	public void onDetach() {
		roles=null;
		labels=null;
		get("form:group_id").detach();
		super.onDetach();
	}
	
	
	public List<String> getRoles() {
		
		if (roles==null) {
			roles = new ArrayList<String>();
			labels = new HashMap<String, Role>();
			for (Role role: getContentSecurityDao().getDomainRoles(getDomain())) {
				if (((KbeeDomainRole) role).getState() == ObjectState.ENABLED) {
					if (!includeSuperUser( (KbeeDomainRole) role)) {
						roles.add(role.getName());
						labels.put(role.getName(), role);
					}
				}
			}
			Collections.sort(roles);
		}
		return roles;
	}

	
	private boolean includeSuperUser(KbeeDomainRole role) {
		for(Group g: role.getGroups()) {
			if (g.getName().equals(KbeeGlobalRole.SU.getId()))
				return true;
		}
		
		return false;
			
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	

	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot( getSessionUser() );
	}
	
	
	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}
	
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	private ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");

	}
	
	
	private boolean isReservedUserName(String username) {
		
		if (username==null)
			return false;
		return 
			username.startsWith("root@") || 
			username.startsWith(DomainService.WORKFLOW_USER+"@") ||
			username.startsWith("pending@");
	}
	

	private void removeRole(UserProfile up, Role role) {
		List<UserRole> u_roles = up.getRoles();
		
		boolean found = false;
		int index= -1;
		
		for (UserRole ur: u_roles) {
			index++;
			if (ur.getRole().getId().equals(role.getId())) {
				found = true;
				break;
			}
		}
		
		if (!found)
			return;
		
		try {
			u_roles.remove(index);
			getModelObject().getService(RolesService.class).update(u_roles);

		} catch (Exception e) {
			logger.error(e);
		}
	}
	

	private void addRole(UserProfile up, Role role) {
		
		List<UserRole> u_roles = up.getRoles();
		
		for (UserRole ur: u_roles) {
			if (ur.getRole().getId().equals(role.getId()))
				return;
		}
		try {
			u_roles.add(new KbeeUserRole(role, up.getUser(), null));
			up.getPerson().getService(RolesService.class).update(u_roles);

		} catch (Exception e) {
			logger.error(e);
			
		}
	}

	
	

}
