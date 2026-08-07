package com.novamens.content.web.security.markup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.service.DomainService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
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
import kbee.web.form.EditButtonsV5;


public class UsersBatchSetGlobalPermissionPanel extends ObjectEditor<Domain> {
			
	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(UsersBatchSetGlobalPermissionPanel.class.getName());
	
	List<String> roles = null;
	Map<String, Group> labels = null;
	
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
	public UsersBatchSetGlobalPermissionPanel(String id, IModel<Domain> model, BaseBrowser<?> browser) {
		super(id, model);
		this.browser=browser;
	}

	public IModel<String> getGroup_id() {
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
			if (!getBrowser().getSelection().isEmpty() && (this.getGroup_id()!=null) && (labels.get(this.getGroup_id().getObject())!=null)) {
				
				List<String> up = new ArrayList<String>();
				Boolean b = getValue();
				Group gp = labels.get(this.getGroup_id().getObject());
				up.add((b.booleanValue()?"add group ":"remove group ") + gp.getDisplayName());
				
				for (IModel<?> mo:  getBrowser().getSelection()) {
					
					Person person = (Person) mo.getObject();
					
				if (person!=null) {
					
					User user = person.getProfile(UserProfile.class).getUser();

					if (user!=null && user.getUserName()!=null) {
						
							String username = user.getUserName();
							
							if (!(username.startsWith("root@") || username.startsWith(DomainService.WORKFLOW_USER+"@"))) {
								if (b.booleanValue() && !user.isMember(gp)) {
									user.addGroup(gp);	
									logger.info("Adding " + gp.getDisplayName() + "  to " + user.getUserName());
									try {
										// we save the UserProfile instead of the User because we want the Person to be touched (lastmodified)
										//
										UserProfile profile = getContentDao().findUserProfileByUser(user);
										if (profile!=null)
											ServiceLocator.getService(com.novamens.content.service.SecurityContentMgmtService.class).update(profile, up);
										else
											ServiceLocator.getService(com.novamens.content.service.SecurityContentMgmtService.class).update(user, up);
									} catch (Exception e) {
										logger.error(e.getClass().getName() + " | adding " + gp.getDisplayName() + "  from " + user.getUserName());
										
									}
									
									
								}
								else if (!b.booleanValue() && user.isMember(gp)) {
									logger.info("Removing " + gp.getDisplayName() + "  from " + user.getUserName());
									user.removeGroup(gp);	
									try {
										// we save the UserProfile instead of the User because we want the Person to be touched (lastmodified)
										//
										UserProfile profile = getContentDao().findUserProfileByUser(user);
										if (profile!=null)
											ServiceLocator.getService(com.novamens.content.service.SecurityContentMgmtService.class).update(profile, up);
										else
											ServiceLocator.getService(com.novamens.content.service.SecurityContentMgmtService.class).update(user, up);
									} catch (Exception e) {
										logger.error(e.getClass().getName() + " | removing " + gp.getDisplayName() + "  from " + user.getUserName());
									}
								}
							}
						} // user != null
				} // person null
				
				}
			}
		}
		catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
		close(target);
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
	
	

	public void onDetach() {
		roles=null;
		labels=null;
		get("form:group_id").detach();
		super.onDetach();
	}
	
	
	public List<String> getRoles() {
		
		if (roles==null) {
			
			roles = new ArrayList<String>();
			labels = new HashMap<String, Group>();
			
			for (Group g: getContentSecurityDao().getCanonicalGroups(getDomain()))
			if (!
					(	g.getName().equals(KbeeGlobalRole.USER.getId())	    	||
						g.getName().equals(KbeeGlobalRole.WORKFLOW.getId())   	||
						g.getName().equals(KbeeGlobalRole.SU.getId())   		||
						g.getName().equals(KbeeGlobalRole.SUPPORT.getId())
					)
				) {
				roles.add(g.getName());
				labels.put(g.getName(), g);
			}
			
			Collections.sort(roles);
		}
		return roles;
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
	
	
	
	

}
