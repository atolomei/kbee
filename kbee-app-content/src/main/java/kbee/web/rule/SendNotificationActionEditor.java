package kbee.web.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.entity.Person;
import com.novamens.content.rule.ActionRule;
import com.novamens.content.rule.SendNotificationAction;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.Role;
import com.novamens.content.service.PersonService;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.rule.KbeeSendNotificationAction;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;
import kbee.web.enoti.ReceiversEditor;
//import kbee.web.enoti.RoleReceiversEditor;

@SuppressWarnings("serial")
public class SendNotificationActionEditor extends ObjectEditorPanel<ActionRule> {
	private static final long serialVersionUID = 1L;
																										
	private static Logger logger = kbee.util.logging.Logger.getLogger(SendNotificationActionEditor.class.getName());
	
	private SendNotificationAction action;
	private IModel<Role> rolemodel =  null;
	

	
	private String text;
	private String subject;

	private List<IModel<Principal>> m_receivers;
	private List<IModel<Role>> m_role_receivers;
	private List<Role> roles = null;
	
	
	public SendNotificationActionEditor(SendNotificationAction action) {
		super("editor");
		
		this.action = action;
		
		setOutputMarkupId(true);
		
		
		setSubject(((KbeeSendNotificationAction)action).getSubtitle());

		add(new TextField<String>("subject",  new PropertyModel<String>(this, "subject"), true) {
			
			@Override
			public IModel<String> getHelpLinkTitle() {
				return new StringResourceModel("subject.help-title");
			}
			@Override
			protected IModel<String> getHelpText() {
				return new Model<String>(new StringResourceModel("subject.help", 
						SendNotificationActionEditor.this, null).getObject().replace("{0}", getSessionUser().getLocale().getLanguage()).replace("{1}", ActionRule.EMAIL_TEMPLATE_KEY  ));	
			}
		});
		
		setText(((KbeeSendNotificationAction)action).getText());
		
		TextAreaField<String> te=new TextAreaField<String>("text",  new PropertyModel<String>(this, "text")) {
			@Override
			protected IModel<String> getHelpText() {					
				return new Model<String>(new StringResourceModel("text.help", SendNotificationActionEditor.this, null).getObject().replace("{0}", getSessionUser().getLocale().getLanguage()).replace("{1}", ActionRule.EMAIL_TEMPLATE_KEY ));	
			}
		};
		te.setRequired(true);
		add(te);
		
		setRole(((KbeeSendNotificationAction)action).getRole());
		add(new ChoiceField<Role>("role", new PropertyModel<Role>(this, "role"), new PropertyModel<List<Role>>(this, "roles"), true));
		
		
		
		ReceiversEditor<ActionRule> directReceivers = new ReceiversEditor<ActionRule>("directReceivers") {
			@Override
			public boolean isEnabled() {
				return true;
			}
			@Override
			protected IModel<Collection<Principal>> getPropertyModel() {
				return new PropertyModel<Collection<Principal>>(SendNotificationActionEditor.this, "directReceivers");
			}
		};
		add(directReceivers);
		
		List<Person> list = ((SendNotificationAction) this.action).getNotifyPersonList();
		m_receivers = new ArrayList<IModel<Principal>>();
		if (list!=null && (!list.isEmpty())) {
			for (Person p: list) {
				User user = p.getService(PersonService.class).getUser();
				if (user != null) {
					m_receivers.add( new ObjectModel<Principal>(user));
				}
			}
		}
		
		/**
		RoleReceiversEditor<ActionRule> roleReceivers = new RoleReceiversEditor<ActionRule>("roleReceivers") {
			@Override
			public boolean isEnabled() {
				return true;
			}
			@Override
			protected IModel<Collection<Role>> getPropertyModel() {
				return new PropertyModel<Collection<Role>>(SendNotificationActionEditor.this, "roleReceivers");
			}
		};
		add(roleReceivers);
		**/
	}
	
	public Role getRole() {
		return this.rolemodel!=null ? this.rolemodel.getObject() : null;
	}
	
	public void setRole(Role role) {
		this.rolemodel = role!=null ? new ObjectModel<Role>(role) : null;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String text) {
		this.subject = text;
	}
	
	public List<Role> getRoles() {
		
		if (this.roles!=null)
			return this.roles;
		
		this.roles = new ArrayList<Role>();
		for (Role role : getSecurityDao().getRoles(getDomain())) {
			if (role.getState()==ObjectState.ENABLED)
				this.roles.add(role);
		}
		return this.roles;
		
	}
	
	public void updateModel() {
		try {
			
			KbeeSendNotificationAction kbeeaction = (KbeeSendNotificationAction) this.action;
			
			if (getRole()!=null)
				kbeeaction.setRole(getRole());
			
			if (getDirectReceivers()!=null) {
				List<Person> list = new ArrayList<Person>();
				for (Principal p: getDirectReceivers()) {
					if (p instanceof User) {
						list.add(getUserProfile((User) p).getPerson());
					}
				}
				kbeeaction.setNotifyPersonList(list);
			}
			kbeeaction.setText(getText());
			kbeeaction.setSubtitle(getSubject());
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	

	@Override
	public void onDetach() {
		super.onDetach();
	
		if (m_receivers!=null) 
			for( IModel<Principal> m: m_receivers) m.detach();
		
		if (m_role_receivers!=null) 
			for( IModel<Role> m: m_role_receivers) m.detach();
		
		if (rolemodel!=null)
			rolemodel.detach();
		
		roles=null;
		
	}
	

	
	
//	private Domain getDomain() {
//		return	ServiceLocator.getService(UserService.class).getDomain();
//	}
//	
	private ContentSecurityDao getSecurityDao() {
		return	(ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	private KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	
	
	public void setDirectReceivers(List<Principal> list) {
		this.m_receivers = new ArrayList<IModel<Principal>>();
		for (Principal p: list) {
			this.m_receivers.add(new ObjectModel<Principal>(p));
		}
	}
	
	
	/** 
	 * Direct Receivers
	 * 
	 */
	public List<Principal> getDirectReceivers() {
		List<Principal> receivers = new ArrayList<Principal>();
		if (this.m_receivers!=null) {
			for (IModel<Principal> model : this.m_receivers) {
				receivers.add(model.getObject());
			}
		}
		return receivers;
	}

	/** 
	 * Roles
	 */
	public void setRolesReceivers(List<Role> list) {
		this.m_role_receivers = new ArrayList<IModel<Role>>();
		for (Role p: list) {
			this.m_role_receivers.add(new ObjectModel<Role>(p));
		}
	}
	
	public List<Role> getRolesReceivers() {
		List<Role> receivers = new ArrayList<Role>();
		if (this.m_role_receivers!=null) {
			for (IModel<Role> model : this.m_role_receivers) {
				receivers.add(model.getObject());
			}
		}
		return receivers;
	}
	
	protected UserProfile getUserProfile(User user) {
		return getContentDao().findUserProfileByUser(user);
	}
}
