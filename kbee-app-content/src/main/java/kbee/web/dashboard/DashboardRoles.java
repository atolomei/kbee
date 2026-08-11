package kbee.web.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.EntityMember;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.security.RolesService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.web.suggestion.service.UserSuggestionService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.content.user.KbeeUserRole;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.form.AutoCompleteFieldV5;

@SuppressWarnings("serial")
public class DashboardRoles extends DashboardWidgetBasePanel {
	private static final long serialVersionUID = 1L;
	
	final boolean is_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_entities_write = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());

	private boolean editionInProgress = false, remotioninProgress = false;
	IModel<EntityMember> entitymodel;
	IModel<Classifier> classifiermodel;
	
	public class UserRoleEditor extends Fragment {
		private IModel<Role> rolemodel;
		private IModel<User> usermodel;
		public UserRoleEditor(String id) {
			super(id, "user-role-editor", DashboardRoles.this);
			setOutputMarkupId(true);
			Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
			form.setOutputMarkupId(true);
			form.add(new AutoCompleteFieldV5<User>("user", new PropertyModel<User>(this, "user"), true) {
				@Override
				public int getMaxHistory() {
					return 3;
				}
				@Override
				public List<Suggestion> getSuggestions(String pattern) {
					return ServiceLocator.getService(UserSuggestionService.class).getSuggestions(pattern);
				}
				@Override
				public String getHistoryKey() {
					return "user-role"+getEntity().getId();
				}
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					
				}
			});
			form.add(new ChoiceField<Role>("role", new PropertyModel<Role>(this, "role"), () -> getRoles(), true) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					
				}
				@Override
				public String getIdValue(Role role) {
					return String.valueOf(role.getId());
				}
			});
			add(form);
			add(new AjaxSubmitLink("add-button", form) {
				public void onSubmit(AjaxRequestTarget target) {
					getPerson().getService(RolesService.class).add(new KbeeUserRole(getRole(), getUser(), getEntity()));
					target.add(DashboardRoles.this);
					editionInProgress = false;
				}
				public void onError(AjaxRequestTarget target) {
					target.add(UserRoleEditor.this);
				}
			});
			add(new AjaxLink<Void>("cancel-button") {
				public void onClick(AjaxRequestTarget target) {
					editionInProgress = false;
					target.add(DashboardRoles.this);
				}
			});
		}
		@Override
		public void onInitialize() {
			super.onInitialize();
		}
		@Override
		@SuppressWarnings("unchecked")
		public void onBeforeRender() {
			super.onBeforeRender();
			if (getRoles().size()==1) {
				setRole(getRoles().get(0));
				((ChoiceField<Role>)get("form:role")).setValue(getRole());
			}
		}
		public void setUser(User user) {
			this.usermodel =  new ObjectModel<User>(user);
		}
		public User getUser() {
			return usermodel!=null ? usermodel.getObject() : null;
		}
		public Person getPerson() {
			UserProfile profile = getContentDao().findUserProfileByUser(getUser());
			Person person = (Person)profile.getEntity();
			return person;
		}
		public void setRole(Role role) {
			this.rolemodel =  new ObjectModel<Role>(role);
		}
		public Role getRole() {
			return rolemodel!=null ? rolemodel.getObject() : null;
		}
	}
	
	public class RemoveConfirmation extends Fragment {
		private IModel<UserRole> userrolemodel;
		public RemoveConfirmation(UserRole userrole) {
			super("remove-confirmation", "remove-confirmation-fragment", DashboardRoles.this);
			userrolemodel = userrole!=null ? new ObjectModel<UserRole>(userrole) : null;
			add(new Label("remove-confirmation-message", getLabel("remove-confirmation", userrole!=null?userrole.getUser().getDisplayName():"")));
			add(new AjaxLink<Void>("remove-button") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					getPerson().getService(RolesService.class).remove(userrolemodel.getObject());
					target.add(DashboardRoles.this);
					remotioninProgress = false;
				}
				@Override
				public boolean isVisible() {
					return isEditionEnabled();
				}
			});
			add(new AjaxLink<Void>("cancel-button") {
				public void onClick(AjaxRequestTarget target) {
					remotioninProgress = false;
					target.add(DashboardRoles.this);
				}
			});
		}
		public User getUser() {
			return userrolemodel.getObject().getUser();
		}
		public Person getPerson() {
			UserProfile profile = getContentDao().findUserProfileByUser(getUser());
			Person person = (Person)profile.getEntity();
			return person;
		}
	}	


	public DashboardRoles(String id) {
		super(id, "roles");
		setTitle(getLabel("title"));
		setHelp(true);
		EntityMember entity = (EntityMember)getContentDao().findMemberById((long)145551);
		entitymodel = new ObjectModel<EntityMember>(entity);
		for (Classifier classifier : getContentDao().getClassifiers(entity.getDomain())) {
			if ("person".equals(classifier.getAlias())) {
				classifiermodel = new ObjectModel<Classifier>(classifier);
				break;
			}
		}
	}
	
	public DashboardRoles(String id, EntityMember entity, Classifier classifier) {
		super(id, "roles");
		setTitle(getLabel("title"));
		setHelp(true);
		this.entitymodel = new ObjectModel<EntityMember>(entity);
		this.classifiermodel = new ObjectModel<Classifier>(classifier);
	}
	
	public DashboardRoles(String id, IModel<EntityMember> entitymodel, IModel<Classifier> classifiermodel) {
		super(id, "roles");
		setTitle(getLabel("title"));
		this.entitymodel = entitymodel;
		this.classifiermodel = classifiermodel;
	}
	
	/** TODO */
	@Override
	protected void onClickCollapse(AjaxRequestTarget target) {
		//main_container.setVisible(!main_container.isVisible());
		refresh(target);
	}
	@Override
	protected void onHelp(AjaxRequestTarget target) {
		refresh(target);
	}

	
	
	public List<UserRole> getUserRoles() {
		List<UserRole> userRoles = new ArrayList<UserRole>();
		for (UserRole userRole :  getSecurityDao().findUserRolesByEntityMember(getEntity())) {
			EntityRole role = (EntityRole)getContentDao().unproxy(userRole.getRole()); 
			if (role.getClassifier().equals(getClassifier())) {
				userRoles.add(userRole);
			}
		}
		return userRoles;
	}
	
	public EntityMember getEntity() {
		return entitymodel.getObject();
	}
	
	public Classifier getClassifier() {
		return classifiermodel.getObject();
	}
	
	public List<Role> getRoles() {
		List<Role> roles = new ArrayList<Role>();
		for (Role role :  getSecurityDao().getRoles(getEntity().getDomain())) {
			if (role.isEntity()) {
				EntityRole entityrole = (EntityRole)getContentDao().unproxy(role); 
				if (entityrole.getClassifier().equals(getClassifier())) {
					roles.add(role);
				}
			}
		}
		return roles;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		add(new ListView<UserRole>("userrole", () -> getUserRoles()) {
			public void populateItem(ListItem<UserRole> item) {
				final int roleindex = item.getIndex();
				UserRole userRole = item.getModelObject();
				item.add(new Label("user", userRole.getUser().getDisplayName()));
				item.add(new Label("role", userRole.getRole().getDisplayName()));
				item.add(new AjaxLink<Void>("remove-link") {
					public void onClick(AjaxRequestTarget target) {
						openRemoveConfirmation(roleindex);
						target.add(DashboardRoles.this);
					}
				});
			}
		});
		
		add(new AjaxLink<Void>("add-link") {
			public void onClick(AjaxRequestTarget target) {
				editionInProgress = true;
				target.add(DashboardRoles.this);
			}
			public boolean isVisible() {
				return !editionInProgress && !remotioninProgress && isEditionEnabled();
			}
		});
		
		add(new UserRoleEditor("editor") {
			public boolean isVisible() {
				return editionInProgress;
			}
		});
		
		add(new RemoveConfirmation(null) {
			public boolean isVisible() {
				return remotioninProgress;
			}
		});
	}	
	
	
	@Override
	protected void onTitleClick() {
	}
	
	
	protected boolean isEditionEnabled() {
		return is_admin || is_entities_write || isAdministrator(getEntity()) ;
	}
	
	private boolean isAdministrator(EntityMember entity) {
		return getUserProfile().getPerson().getService(RolesService.class).isAdministrator(entity);
	}
	
	private void openRemoveConfirmation(int roleindex) {
		UserRole userRole = getUserRoles().get(roleindex);
		remotioninProgress = true;
		addOrReplace(new RemoveConfirmation(userRole) {
			public boolean isVisible() {
				return remotioninProgress;
			}
		});
	}
	
	protected UserProfile getUserProfile() {
		return getContentDao().findUserProfileByUser(getSessionUser());
	}
		
	private ContentSecurityDao getSecurityDao() {
		return	(ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
}