package kbee.web.security.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.EntitySet;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.security.DomainRole;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.security.RolesService;
import com.novamens.content.service.DataAccessService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserService;
import com.novamens.content.web.suggestion.service.UserSuggestionService;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.content.dao.Proxy;
import com.novamens.kbee.content.model.KbeeClassifierTemplate;
import com.novamens.kbee.content.user.KbeeUserRole;
import com.novamens.kbee.wicket.markup.html.console.event.EditEvent;
import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;
import kbee.web.editor.DomainObjectEditor;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.AutoCompleteFieldV5;
import kbee.web.form.EditButtonsV5;
import kbee.web.panel.AlertPanel;
import kbee.web.security.PrincipalSelector;
import kbee.web.security.PrincipalSelector.EntityNode;
import kbee.web.security.PrincipalSelector.PrincipalNode;
import kbee.web.security.PrincipalSelector.RoleNode;
import kbee.web.service.ApplicationSiteMapService;

/**
 * 
 */
@SuppressWarnings("serial")
public class UserRolesEditor extends DomainObjectEditor<Person>  {
	
	static private Logger logger = new Logger(LogManager.getLogger(UserRolesEditor.class.getName()));

	final boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();
	
	final boolean is_admin	= is_root || 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());;
		
	final boolean is_security = is_root ||
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.SECURITY.getId());;
		
	final boolean is_federated_security = 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.FEDERATED_SECURITY.getId());;
			
	private List<IModel<UserRole>> roles = new ArrayList<>();
	private List<IModel<UserRole>> defaultRoles = new ArrayList<>();
	private IModel<Role> rolemodel;
	private IModel<EntityMember> entitymodel;
	private boolean updated = false;
	private Boolean is_enabled = null;

	private IModel<User> copyRoles;

	static int MAX_ROLE_USER = 150;
	
	static {
		try {
			MAX_ROLE_USER = Integer.valueOf( ((ContentDao)ServiceLocator
				.getService(BeansService.class)
				.getBean("contentDao"))
				.findSystemParameterValueByKey("max-roles-per-user", "150"));
		} 
		catch (Exception e) {
			logger.error(e);
			MAX_ROLE_USER=100;	
		}
	}
	

	
	public UserRolesEditor(String id) {
		super(id);
		setOutputMarkupId(true);
		setEditionEnabled(false);
	}
	
	
	public void setUserRoles(List<UserRole> roles) {
		this.roles.clear();
		this.defaultRoles.clear();
		for (UserRole role : roles) {
			if (role.getRole().isDefault()) {
				this.defaultRoles.add(new ObjectModel<UserRole>(role));
			}
			else {
				this.roles.add(new ObjectModel<UserRole>(role));
			}
		}	
	}
	
	public List<IModel<UserRole>> getUserRoles() {
		return roles;
	}
	
	public List<IModel<UserRole>> getDefaultRoles() {
		return defaultRoles;
	}

	public IModel<User> getCopyRoles() {
		return this.copyRoles;
	}
	
	public void setCopyRoles(IModel<User> roles) {
		this.copyRoles =  roles;
	}

	public List<Role> getRoles() {
		
		List<Role> allroles =  getContentSecurityDao().getRoles(getDomain());
		List<Role> roles = null;
		
		if (!is_security) {
			// solo es un administrador de un subconjunto de usuarios
			roles = new ArrayList<Role>();
			for (Role role : allroles) {
				if (role.isEntity()) {
					roles.add((Role)getContentDao().reload(role));
				}	
			}
		}
		else {
			roles = new ArrayList<Role>();
			for (Role role : allroles) {
				if (!role.isEntity()) {
					if ((!role.isAdministrator() || is_admin) && 
						ObjectState.ENABLED.equals(role.getState())) {
						roles.add((Role)getContentDao().reload(role));
					}
				}
				else
				if ((!role.isOnlyRootEditable() || is_root) &&
					ObjectState.ENABLED.equals(role.getState())) {
					roles.add((Role)getContentDao().reload(role));
				}	
			}
		}
		
		Collections.sort(roles, new Comparator<Role>() {
			@Override
			public int compare(Role a, Role b) {
				try {
				if (a.getName()!=null && b.getName()!=null)
					return a.getName().trim().toLowerCase().compareTo(b.getName().trim().toLowerCase());
				return 0;
				} catch (Exception e)  {
					logger.error(e);
					return 0;
				}
			}
		}); 
		
		return roles;
	}

		
	@Override
	public boolean isEnabled() {

		if (is_enabled!=null) {
			return this.is_enabled.booleanValue();
		}	
		
		// only root can edit roles for Canonical users
		if  (((UserProfile)getModelObject().getProfile(UserProfile.class)).getUser().isCanonical())
			return is_root;
		
		// only root can edit root roles
		if  (((UserProfile)getModelObject().getProfile(UserProfile.class)).getUser().getUserName().startsWith("root@"))
			return is_root;
		
		is_enabled = is_root || 
			is_admin || 
			is_security || 
			(is_federated_security 
				&& ServiceLocator
				.getService(UserService.class)
				.isUserAdmin());
		
		return this.is_enabled.booleanValue(); 
	}
	
	/**
	 * 
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setUserRoles();
		
		final Form<?> form = new Form<Void>("form", Disposition.VERTICAL) {
			public boolean isVisible() {
				return true; 
			}
		};
		
		add(form);
		
		AlertPanel<Void> pa=new AlertPanel<Void>("roles-text",AlertPanel.INFO,  null, 
				null, 
				getLabel("roles-text"));
		pa.setIcon(AlertPanel.HELP_INFO);
		
		form.add(pa);
		
		form.add(new InvisiblePanel("admin-message"));
		
		form.add(new WebMarkupContainer("note100-container") {
			@Override
			public boolean isVisible() {
				return isEditionEnabled() && getUserRoles().size()>0;
			}
		});
		
		form.add(new Label("total","("+ String.valueOf(getUserRoles().size())+")"));
		
		form.add(getDefaultRolesPanel());
		form.add(getRolesListPanel());
 		
		form.add(getRoleSelectorTool());
		
		add(form);
		
		add(new EditButtonsV5<Person>(this) {
			@Override
			public boolean isVisible() {
				return UserRolesEditor.this.isEnabled();
			}
			@Override
			public boolean isEnabled() {
				return UserRolesEditor.this.isEnabled();
			}
		});
		
		add(new WicketEventListener<EditEvent<Person>>() {
			@Override
			public void onEvent(EditEvent<Person> event) {
				setUserRoles();
			}
		});
		
		add(new WicketEventListener<AddRoleEvent>() {
			@Override
			public void onEvent(AddRoleEvent event) {
				setSelectedRole(null);
				setSelectedEntity(null);
				((Field<?>)form.get("add-role-container:role")).setModel(null);
				((Field<?>)form.get("add-role-container:role")).setValue(null);
				((Field<?>)form.get("add-role-container:role")).clearInput();
				((Field<?>)form.get("add-role-container:entity")).setModel(null);
				((Field<?>)form.get("add-role-container:entity")).setValue(null);
				((AutoCompleteFieldV5<?>)form.get("add-role-container:entity")).setStringValue(null);
				((Field<?>)form.get("add-role-container:entity")).clearInput();
				event.getRequestTarget().add(UserRolesEditor.this);			}
		});

	}
	
	/**
	 * 
	 */
	public void update(AjaxRequestTarget target) {
		try {
			if (this.updated) {
				List<UserRole> roles = new ArrayList<UserRole>();
 				for (IModel<UserRole> model : getUserRoles()) 
					roles.add(model.getObject());
 				for (IModel<UserRole> model : getDefaultRoles()) 
					roles.add(model.getObject());
				getModelObject().getService(RolesService.class).update(roles);
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<Void>(target, e));
		}
	}
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		for (IModel<UserRole> model : roles)
			model.detach();
		if (this.copyRoles!=null)
			this.copyRoles.detach();
		if (rolemodel!=null) 
			rolemodel.detach();
		if (entitymodel!=null) 
			entitymodel.detach();
	}
	
	public boolean existsUserRole(UserRole urole) {
		if (urole==null || urole.getRole()==null)
			return false;
		// Domain Role
		if (!urole.getRole().isEntity()) {
			for (IModel<UserRole> model : roles) {
				if (model.getObject().getRole().getId().equals(urole.getRole().getId()))
					return true;
			}
			return false;
		}
		// Entity
		else {
			for (IModel<UserRole> model : roles) {
				if (model.getObject().getRole().getId().equals(urole.getRole().getId()) &&
					model.getObject().getEntity().getId().equals(urole.getEntity().getId()))
					return true;
			}
			return false;
		}
	}
	
	public void setSelectedRole(Role role) {
		this.rolemodel = role!=null ? new ObjectModel<Role>(role) : null;
	}

	public Role getSelectedRole() {
		return this.rolemodel!=null ? this.rolemodel.getObject() : null;  
	}

	public void setSelectedEntity(EntityMember member) {
		this.entitymodel = member!=null ? new ObjectModel<EntityMember>(member) : null;
	}

	public EntityMember getSelectedEntity() {
		return this.entitymodel!=null ? this.entitymodel.getObject() : null;  
	}
	
	protected String getLabel(UserRole userRole) {
		StringBuilder label = new StringBuilder();
		label.append(userRole.getRole().getName());

		if (userRole.getEntity()!=null) 
			label.append("  <span class=\"iql-group-start\">( <span class=\"iql-value\">" + 
				getDisplayName(userRole.getEntity())+
				"</span><span class=\"iql-group-end\"> ) </span>");
		
		return label.toString();
	}
	
	protected void setUserRoles() {
		List<UserRole> roles = ((UserProfile)getModelObject().getProfile(UserProfile.class)).getRoles();
		roles.sort( new Comparator<UserRole>() {
			@Override
			public int compare(UserRole a, UserRole b) {
				try {
					int x = a.getRole().getDisplayName().compareToIgnoreCase(b.getRole().getDisplayName());
					if (x!=0)return x;
					return a.getEntity().getDisplayName().compareToIgnoreCase(b.getEntity().getDisplayName());
				} 
				catch (Exception e) {
					return 0;
				}
			}
		});

		setUserRoles(roles);
	}
	
	protected Component getDefaultRolesPanel() {
		return new ListView<IModel<UserRole>>("default-roles", new PropertyModel<List<IModel<UserRole>>>(this, "defaultRoles")) {
			public boolean isVisible() {
				if (isEditionEnabled())
					return true;
				if (getDefaultRoles().isEmpty())
					return false;
				return true;
			}
			@Override
			public void populateItem(final ListItem<IModel<UserRole>> item) {
				UserRole userrole = ((IModel<UserRole>) item.getModelObject()).getObject();
				String label = getLabel(userrole);
				item.add((new Label("role", label)).setEscapeModelStrings(false));
			}
		};
	}

	
	protected Component getRolesListPanel() {
		return new ListView<IModel<UserRole>>("roles", new PropertyModel<List<IModel<UserRole>>>(this, "userRoles")) {
			public boolean isVisible() {
				if (isEditionEnabled())
					return true;
				if (getUserRoles().isEmpty())
					return false;
				return true;
			}
			@Override
			public void populateItem(final ListItem<IModel<UserRole>> item) {
	
				UserRole userrole = ((IModel<UserRole>) item.getModelObject()).getObject();
			
				String label = getLabel(userrole);
				
				Link<Void> rolelink = new Link<Void>("role-link") {
					public void onClick() {
						try {
							UserRole userrole = ((IModel<UserRole>) item.getModelObject()).getObject();
							if (userrole!=null) {
								Role role = userrole.getRole();
								if (role!=null) {
				                    PageParameters pageParameters = new PageParameters();
				                    pageParameters.set("id", role.getId().toString());
									setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("security-role-page", pageParameters));
									return;
								}
							}
							setResponsePage(new ApplicationErrorPage<Void>(new Model<String>("Application Error"), new Model<String>("UserRole or Role is null")));
						} 
						catch (Exception e) {
								logger.error(e);
								setResponsePage(new ApplicationErrorPage<Void>(new Model<String>(e.getClass().getSimpleName()), new Model<String>(e.getMessage())));
						}
					}
				};
				
				rolelink.add((new Label("role", label.toString())).setEscapeModelStrings(false));
				rolelink.add(new AttributeModifier("title", isEnabled() ? "Open Role" : "Your account does not have rigths to Open Role"));
				item.add(rolelink);
				
				item.add(new AjaxLink<Void>("deleterole-button") {
					public void onClick(AjaxRequestTarget target) {
						removeRole(item.getModelObject().getObject());
						target.add(UserRolesEditor.this);
					}
					
					@Override
					public boolean isVisible() {
						return isEditionEnabled() && 
							!item.getModelObject().getObject().getRole().isDefault();
					}
				});
			}
		};
	}
	
	protected Component getCopyFromUserTool() {
		WebMarkupContainer aru = new WebMarkupContainer("add-role-from-user-container") {
			@Override
			public boolean isVisible() {
				return is_admin;
			}
		};
		AutoCompleteFieldV5<User> usel = new AutoCompleteFieldV5<User>("copy-user", 
				new PropertyModel<User>(this, "copyRoles"), false) {
			@Override
			public int getMaxHistory() {
				return 3;
			}
			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				return ServiceLocator
					.getService(UserSuggestionService.class)
					.getSuggestions(pattern);
			}
			@Override
			public String getHistoryKey() {
				return "user-roles-copy-from";
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
				
				if (getValue()==null) {
					return;
				}
				
				User user = getValue();
						
				UserProfile up = getContentDao().findUserProfileByUser(user);
						
				for (UserRole u_role: up.getRoles()) {
							
					Role re= getContentSecurityDao().findRoleById((Long) u_role.getRole().getId());
							
					if (re!=null) {
						if (re.isEntity()) {
							if (!existsUserRole(u_role)) {
								UserRolesEditor.this.updated = true;
								UserRolesEditor.this.roles.add(
									new NewUserRoleModel(
										new ObjectModel<Role>(u_role.getRole()), 
										new ObjectModel<EntityMember>(u_role.getEntity())));
							}
						}
						else {
							if ((u_role.getRole().getAlias()==null) || (!u_role.getRole().getAlias().equals("superuser"))) {
								if (!existsUserRole(u_role)) { 
									UserRolesEditor.this.updated = true;
									UserRolesEditor.this.roles.add(
										new NewUserRoleModel(
											new ObjectModel<Role>(u_role.getRole()), 
											null));
								}
							}
						}
					}
				}
				
				fire(new AddRoleEvent(target));
			}
		};
		
		aru.add(usel);		
		
		return aru;
	}

	protected Component getRoleSelectorTool() {
 		WebMarkupContainer arc = new WebMarkupContainer("add-role-container") {
			public boolean isVisible() {
				return isEditionEnabled() && 
					getUserRoles().size()< MAX_ROLE_USER; 
			}
		};
		arc.setOutputMarkupId(true);
		arc.add(getCopyFromUserTool());
		
 		WebMarkupContainer choice = new WebMarkupContainer("choice-container") {
			public boolean isVisible() {
				return (is_admin || is_security);  
			}
		};


		choice.add(new ChoiceField<Role>("role", 
				new PropertyModel<Role>(this, "selectedRole"), 
				new PropertyModel<List<Role>>(this, "roles"), false) {
			
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				setSelectedRole(getValue());
				if (getSelectedRole()!=null && getSelectedRole() instanceof EntityRole) {
					if (((EntityRole)getSelectedRole()).getClassifier()!=null) {
						String name = ((EntityRole)getSelectedRole()).getClassifier().getName();
						((TextField<?>) UserRolesEditor.this
								.get("form:add-role-container:entity"))
								.setLabel(new Model<String>(name)); 
					}
				}
				if (getSelectedRole()!=null && getSelectedRole() instanceof DomainRole) {
					for (IModel<UserRole> mrole: getUserRoles()) {
						if (mrole.getObject().getRole().getId().equals(getSelectedRole().getId())) {
							target.add(UserRolesEditor.this);
							return;
						}
					}
					assignSelectedRole();
					fire(new AddRoleEvent(target));
				}
				target.add(UserRolesEditor.this);
			}
			@Override
			protected String getDisplayValue(Role role) {
				return role.getDisplayName();
			}
		});
		
		choice.add(new AutoCompleteFieldV5<EntityMember>("entity", 
				new PropertyModel<EntityMember>(this, "selectedEntity"), 
				true) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				if (getValue()==null) {
					target.add(UserRolesEditor.this);
					return;
				}
				setSelectedEntity(getValue());
				if (getSelectedRole()!=null && getSelectedRole() instanceof EntityRole) {
					for (IModel<UserRole> mrole: getUserRoles()) {
						if (mrole.getObject().getRole().getId().equals(getSelectedRole().getId())) {
							if (mrole.getObject().getEntity().getId().equals(getSelectedEntity().getId())) {
								target.add(UserRolesEditor.this);
								return;
							}
						}
					}
					assignSelectedRole();
					fire(new AddRoleEvent(target));
				}
				target.add(UserRolesEditor.this);
			}
			@Override
	 		public List<Suggestion> getSuggestions(String pattern) {
				if (getSelectedRole()!=null && getSelectedRole() instanceof EntityRole) {
					if (((EntityRole)getSelectedRole()).getClassifier()!=null) {
						KbeeClassifierTemplate relation = new KbeeClassifierTemplate(((EntityRole)getSelectedRole()).getClassifier());
						relation.setAccessibility(AccessStrategy.Managed);
						return relation.getService(DataAccessService.class).getSuggestions(pattern);
					}
				}
				return null; 
			}
			@Override
			public boolean isVisible() {
				return getSelectedRole()!=null && getSelectedRole() instanceof EntityRole;
			}
			protected String getInfo(Suggestion suggestion) {
				EntityMember object = (EntityMember)((IModel<?>)suggestion.getObject()).getObject();
				ExtractionRule rule = ((DataSetMember)object).getDataSet().getSublineRule();
				String label = rule!=null ? (String)rule.extract((DataSetMember)object) : null;
				return label;
			}
			protected String getTemplate() {
				return "function(data) {  "+
					"var value = '<div class=\"list-group-item\" style=\"border:none;\"><span class=\"list-group-item-heading\">' + data.value; " +
					"if (data.info) { value = value + '</span> - <span class=\"list-group-item-text\" >' + data.info + '</span></div>'; } else { value = value + '</span></div>' };" +
					"return value;}";
			}
			@Override 
			public boolean isEnabledAdvancedOptions(){
				return true;
			}
			@Override 
			public String getHistoryKey() {
				if (getSelectedRole()==null)
					return "entity-userrole";
				return "entity-userrole-rid-"+ getSelectedRole().getId().toString(); 
			}
			@Override
			public IModel<String> getLabel() {
				try {
					if (getSelectedRole() instanceof EntityRole) {
						if (((EntityRole) getSelectedRole()).getClassifier() != null) {
							return new Model<String>(((EntityRole) getSelectedRole()).getClassifier().getDisplayName());
						}
					}
					return new Model<String>("entity");
				}
			 	catch (Exception e) {
					logger.error(e);
					return new Model<String>("entity");
				}
			}
		});
		
		
		choice.add(new AjaxLink<Void>("addrole-button") {
			public void onClick(AjaxRequestTarget target) {
				assignSelectedRole();
				fire(new AddRoleEvent(target));
			}
			@Override
			public boolean isEnabled() {
				return validSelection();
			}
			@Override
			public boolean isVisible() {
				return false;
			}
		});
		
		arc.add(choice);
		
		arc.add(new WebMarkupContainer("selector-button") {
			public void onInitialize() {
				super.onInitialize();
				add(new AjaxLink<>("button") {
					public void onClick(AjaxRequestTarget target) {
						arc.get("selector").setVisible(true);
						target.add(arc);
					}
				});
			}
			public boolean isVisible() {
				return arc.get("selector").isEnabled(); 
			}
		});
		
		arc.add(new PrincipalSelector("selector") {
			protected void onSelect(AjaxRequestTarget target, PrincipalNode node) {
				assingRoleFromNode(node);
				setSelectedRole(null);
				setSelectedEntity(null);
				super.setVisible(false);
				super.onClose(target);
			}
			protected void onClose(AjaxRequestTarget target) {
				target.add(UserRolesEditor.this);
				super.onClose(target);
			}
		});
		
		return arc;
	}

	private boolean validSelection() {
		if (rolemodel!=null) {
			Role role = rolemodel.getObject();
			if (role instanceof EntityRole && entitymodel==null)
				return false;
			for (IModel<UserRole> model : roles) {
				UserRole assigneduserrole = model.getObject();
				Role assignedrole = assigneduserrole.getRole();
				if (assignedrole.equals(role)) {
					if (role instanceof EntityRole && entitymodel!=null && assigneduserrole.getEntity()!=null) {
						if (entitymodel.getObject().equals(assigneduserrole.getEntity())) {
							return false;
						}
					}
				}
			}
			return true;
		}
		return false;
	}
	
	protected String getDisplayName(EntityMember entity) {
		try {
			ExtractionRule rule = entity.getDataSet().getSublineRule();
			String name = entity.getDisplayName();
			if (rule!=null) {
				name += " - " + (String)rule.extract(entity); 
			}
			return name;
		}
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	private void assingRoleFromNode(PrincipalNode node) {
		if (node instanceof RoleNode) {
			Role role = ((RoleNode)node).getRole();
			if (!role.isDefault()) {
				rolemodel = new ObjectModel<Role>(role);
				EntityMember entity = ((RoleNode)node).getEntity();
				entitymodel = new ObjectModel<EntityMember>(entity);
				assignSelectedRole();
			}
		}	
		else {
			EntityMember entity = ((EntityNode)node).getEntity();
			entitymodel = new ObjectModel<EntityMember>(entity);
			EntitySet entitySet = (EntitySet)Proxy.Unproxy(entity.getDataSet());
			List<Role> roles = getContentSecurityDao()
				.getRolesByEntitySet(entitySet);
			if (roles.size()==1 && !roles.get(0).isDefault()) {
				rolemodel = new ObjectModel<Role>(roles.get(0));
				assignSelectedRole();
			}
		}
	}

	private void assignSelectedRole() {
		if (validSelection()) {
			this.updated = true;
			this.roles.add(new NewUserRoleModel(this.rolemodel, this.entitymodel));
		}
	}
	
	private void removeRole(UserRole userrole) {
		for (IModel<UserRole> model : roles) {
			if (model.getObject().equals(userrole)) {
				this.updated = true;
				this.roles.remove(model);
				break;
			}
		}
	}
	
	private class AddRoleEvent extends AbstractWicketAjaxEvent {
		public AddRoleEvent() {
			super(null);
		}
		public AddRoleEvent(AjaxRequestTarget target) {
			super(target);
		}
	}	
	
	private class NewUserRoleModel implements IModel<UserRole> {
		private IModel<Role> rolemodel;
		private IModel<EntityMember> entitymodel;
		private UserRole userRole;
		public NewUserRoleModel(IModel<Role> rolemodel, IModel<EntityMember> entitymodel) {
			this.rolemodel =  rolemodel;
			this.entitymodel = entitymodel;
		}
		public UserRole getObject() {
			if (userRole==null) { 
				KbeeUserRole ur = new KbeeUserRole();
				if (entitymodel!=null)
				ur.setEntity(entitymodel.getObject());
				if (rolemodel!=null)
				ur.setRole(rolemodel.getObject());
				userRole = ur;
			}
			return userRole;
		}
		public void setObject(UserRole role) {
		}
		public void detach() {
			userRole = null;
			if (rolemodel!=null) rolemodel.detach();
			if (entitymodel!=null) entitymodel.detach();
		}
	}
}
