package kbee.web.dataset;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.security.Role;
import com.novamens.content.service.PersonService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.externalLogin.ExternalPlatformId;
import com.novamens.content.user.externalLogin.UserExternalLoginPlatform;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.content.repository.PersonRepository;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.CheckField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.WebSuggestion;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.form.AutoCompleteFieldV5;

@SuppressWarnings("serial")
public class PersonAccountEditor extends DomainObjectEditor<DataSetMember>  {
	private static final long serialVersionUID = 1L;
	
	final boolean is_security = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	final boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();
	
	//static private kbee.util.logging.Logger logger = new kbee.util.logging.Logger(LogManager.getLogger(PersonAccountEditor.class.getName()));
	
	private boolean is_creating=false;
	private boolean is_linking=false;
	private boolean is_editing= false;
	private boolean view=false;
	
	public class EditorFragment extends Fragment {
		private String email;
		private Boolean googleAuth;
		private Boolean facebookAuth;
		private IModel<Role> rolemodel;
		public EditorFragment(String id) {
			super(id, "editor-fragment", PersonAccountEditor.this);
			
			
			setEmail(((PersonMember)getModelObject()).getEmail());
			setRole(PersonAccountEditor.this.getMainRole()!=null?PersonAccountEditor.this.getMainRole():getDefaultRole());
			setGoogleAuth(PersonAccountEditor.this.hasAuthPlatform(ExternalPlatformId.GOOGLE));
			setFacebookAuth(PersonAccountEditor.this.hasAuthPlatform(ExternalPlatformId.FACEBOOK));
			Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
			form.add(new TextField<String>("name", () -> getUserName()) {
				public boolean isEnabled() {
					return false;
				}
			});
			form.add(new TextField<String>("email", new PropertyModel<String>(this, "email")) {
				public boolean isInputEnabled() {
					return false;
				}
			});
			form.add(new ChoiceField<Role>("role", new PropertyModel<Role>(this, "role"),new PropertyModel<List<Role>>(PersonAccountEditor.this, "roles")) {
				public boolean isEnabled() {
					return is_security;
				}
			});
			form.add(new CheckField("googleAuth", new PropertyModel<Boolean>(this, "googleAuth")));
			form.add(new CheckField("facebookAuth", new PropertyModel<Boolean>(this, "facebookAuth")));
			
			
			Label ti=new Label("title", new StringResourceModel( (is_creating ? "create-user": "edit-user"), this, null));
			add(ti);
			
			
			add(form);
			add(new AjaxLink<Void>("edit-button") {
				public void onClick(AjaxRequestTarget target) {
					EditorFragment.this.onEdit(target);
				}
				public boolean isVisible() {
					return view;
				}
			});
			add(new AjaxSubmitLink("update-button", form) {
				public void onSubmit(AjaxRequestTarget target) {
					updateUser(getAssignedRoles(), getAuthPlatforms());
					EditorFragment.this.onUpdate(target);
				}
				public boolean isVisible() {
					return is_editing;
				}
			});
			add(new AjaxSubmitLink("create-button", form) {
				public void onSubmit(AjaxRequestTarget target) {
					createUser(getAssignedRoles(), getAuthPlatforms());
					EditorFragment.this.onUpdate(target);
				}
				public boolean isVisible() {
					return is_creating;
				}
			});
			add(new AjaxLink<Void>("cancel-button") {
				public void onClick(AjaxRequestTarget target) {
					EditorFragment.this.onCancel(target);
				}
				public boolean isVisible() {
					return is_creating || is_editing;
				}
			});
		}	
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public Boolean getGoogleAuth() {
			return googleAuth;
		}
		public void setGoogleAuth(Boolean googleAuth) {
			this.googleAuth = googleAuth;
		}
		public Boolean getFacebookAuth() {
			return facebookAuth;
		}
		public void setFacebookAuth(Boolean facebookAuth) {
			this.facebookAuth = facebookAuth;
		}
		public void setRole(Role role) {
			this.rolemodel = new ObjectModel<Role>(role);
		}
		public Role getRole() {
			return rolemodel!=null ? rolemodel.getObject() : null;
		}
		public List<ExternalPlatformId> getAuthPlatforms() {
			List<ExternalPlatformId> platforms = new ArrayList<ExternalPlatformId>();
			if (getGoogleAuth()) platforms.add(ExternalPlatformId.GOOGLE);
			if (getFacebookAuth()) platforms.add(ExternalPlatformId.FACEBOOK);
			return platforms;
		}
		public List<Role> getAssignedRoles() {
			List<Role> roles = new ArrayList<Role>();
			Role role = getRole();
			if (role!=null) roles.add(role);
			return roles;
		}
		public void onUpdate(AjaxRequestTarget target) {
		}
		public void onEdit(AjaxRequestTarget target) {
		}
		public void onCancel(AjaxRequestTarget target) {
		}
	}	
	
	
	
	/** -----------------------------------------------------------
	 *
	 */
	public class UserLinkFragment extends Fragment {
		private IModel<Person> linkeduser;
		private IModel<String> helpmodel;
		public UserLinkFragment(String id) {
			super(id, "link-editor-fragment", PersonAccountEditor.this);
			
			setOutputMarkupId(true);
			
			Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
			add(form);
			
			form.add(new AutoCompleteFieldV5<Person>("linked", new PropertyModel<Person>(this, "linked"), true) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
				}
				@Override
				public List<Suggestion> getSuggestions(String pattern) {
					return UserLinkFragment.this.getSuggestions(pattern);
				}
				@Override
				public IModel<String> getHelpText() {
					if (helpmodel==null) {
						helpmodel = new Model<String>() {
							public String getObject() {
								return getLabelString(getProperty()+".help", getModelObject().getDataSet().getDisplayName(), getModelObject().getDisplayName());
							}
						};
					}
					return helpmodel;
				}
				@Override 
				public String getHistoryKey() {
					return null; 
				}
			});
			
			((AutoCompleteFieldV5<?>)form.get("linked")).setHelpTextVisible(true);
			
			add(new AjaxSubmitLink("save-button", form) {
				@Override 
				public void onSubmit(AjaxRequestTarget target) {
					if ( getLinked() != null) {
						getPersonInEdition().getService(PersonService.class).setUserFrom(getLinked());
						onUpdate(target);
					}
				}
				@Override 
				public boolean isVisible() {
					return is_linking;
				}
			});
			
			add(new AjaxLink<Void>("cancel-button") {
				public void onClick(AjaxRequestTarget target) {
					 UserLinkFragment.this.onCancel(target);
				}
				public boolean isVisible() {
					return is_linking;
				}
			});
		}		
		public Person getLinked() {
			return linkeduser!=null ? linkeduser.getObject() : null;
		}
		public void setLinked(Person user) {
			linkeduser = new ObjectModel<Person>(user);
		}
		public void onUpdate(AjaxRequestTarget target) {
		}
		public void onEdit(AjaxRequestTarget target) {
		}
		public void onCancel(AjaxRequestTarget target) {
		}
		public List<Suggestion> getSuggestions(String pattern) {
			List<Suggestion> suggestions = new ArrayList<Suggestion>();
			for (Person person : getUsersNotIncluded()) {
				boolean include = person.getLastFirstName()!=null && (pattern==null || "".equals(pattern) || 
					person.getLastFirstName().toLowerCase().contains(pattern.toLowerCase()));
				if (include) {
					IModel<Person> personmodel = new ObjectModel<Person>(person);
					WebSuggestion suggestion = new WebSuggestion(personmodel, person.getLastFirstName(), 0, false);
					suggestions.add(suggestion);
				}
			}
			return suggestions;
		}

	}	

	
	/**
	 * 
	 * @param id
	 * @param model
	 */
	public PersonAccountEditor(String id, IModel<DataSetMember> model) {
		super(id, model);
		
		setOutputMarkupId(true);
		setEditionEnabled(false);
		
		view = getProfile()!=null;
		
		add(new AjaxLink<Void>("creation-button") {
			public void onClick(AjaxRequestTarget target) {
				is_creating = true;
				is_linking=false;
				setEditionEnabled(true);
				target.add(PersonAccountEditor.this);
			}
			public boolean isVisible() {
				return !is_creating && !is_linking && getProfile()==null && is_security;
			}
		});

		add(new AjaxLink<Void>("link-user-button") {
			public void onClick(AjaxRequestTarget target) {
				is_creating = false;
				is_linking=true;
				setEditionEnabled(true);
				target.add(PersonAccountEditor.this);
			}
			public boolean isVisible() {
				return !is_creating && 
					!is_linking && 
					getProfile()==null && 
					is_security &&
					!getUsersNotIncluded().isEmpty();
			}
		});
		
		add(new EditorFragment("editor") {
			public boolean isVisible() {
				return !is_linking && (is_creating || getProfile()!=null);
			}
			public void onEdit(AjaxRequestTarget target) {
				is_creating = false; 
				is_editing=true; 
				view=false;
				setEditionEnabled(true);
				target.add(PersonAccountEditor.this);
			}
			public void onUpdate(AjaxRequestTarget target) {
				is_creating = false; 
				is_editing=false; 
				view=true;
				setEditionEnabled(false);
				target.add(PersonAccountEditor.this);
			}
			public void onCancel(AjaxRequestTarget target) {
				is_creating = false; 
				is_editing=false; 
				view=getProfile()!=null;
				setEditionEnabled(false);
				target.add(PersonAccountEditor.this);
			}
		});
		
		
		add(new UserLinkFragment("linker") {
			public boolean isVisible() {
				return is_linking && !is_creating;
			}
			public void onUpdate(AjaxRequestTarget target) {
				is_creating = false;
				is_linking=false;
				is_editing=false; 
				view=true;
				setEditionEnabled(false);
				target.add(PersonAccountEditor.this);
			}
			public void onCancel(AjaxRequestTarget target) {
				is_creating = false; 
				is_linking=false;
				is_editing=false; 
				view=getProfile()!=null;
				setEditionEnabled(false);
				target.add(PersonAccountEditor.this);
			}
		});
	}
	
	public List<Role> getRoles() {
		List<Role> roles = new ArrayList<Role>();
		for (Role role : getContentSecurityDao().getRoles(getDomain())) {
			if (!role.isCanonical() && !role.isEntity()) {
				roles.add(role);
			}
		}
		return roles;   
	}
	
	public Role getDefaultRole() {
		Role defaultrole = null;
		for (Role role : getRoles()) {
			if (defaultrole == null)
				defaultrole = role;
			if (role.getIsDefault()) {
				defaultrole = role;
				break;
			}
		}
		return defaultrole;
	}
	
	private void createUser(List<Role> roles, List<ExternalPlatformId> oauthPlatforms) {
		((PersonMember)getModelObject()).getService(PersonService.class).createUser(roles, oauthPlatforms);
	}
	
	private void updateUser(List<Role> roles, List<ExternalPlatformId> oauthPlatforms) {
		((PersonMember)getModelObject()).getService(PersonService.class).updateUser(roles, oauthPlatforms);
	}
	
	private  List<Person> getUsersNotIncluded() {
		List<Person> users = new ArrayList<Person>();
		for (Person person : ((PersonRepository)getRepository(Person.class)).findNotIn(getModelObject().getDataSet())) {
			if (!"Shared Resources".equals(person.getLastName()) && !"pending".equals(person.getLastName())) {
				User user = person.getProfile(UserProfile.class)!=null ? person.getProfile(UserProfile.class).getUser() : null;
				if (user!=null) {
					if (!user.getName().startsWith("root@") || is_root) {
						users.add(person);
					}
				}
			}	
		};
		return users;
	}
	
	private boolean hasAuthPlatform(ExternalPlatformId platform) {
		UserProfile profile = getProfile();
		if (profile==null) return false;
		for (UserExternalLoginPlatform p : profile.getUserExternalLoginPlatforms()) {
			if (p.getPlatformId() == platform.getId()) {
				return true;
			}
		}
		return false;
	}
	
	// main role assigned
	private Role getMainRole() {
		UserProfile profile = getProfile();
		if (profile==null) return null;
		for (UserRole userRole : profile.getRoles()) {
			if (!userRole.getRole().isCanonical() && !userRole.getRole().isEntity()) {
				return userRole.getRole();
			}
		}
		return null;
	}
	
	private Person getPersonInEdition() {
		return ((PersonMember)getModelObject());
	}
	
	private UserProfile getProfile() {
		Person person = ((PersonMember)getModelObject());
		UserProfile profile = person.getProfile(UserProfile.class);
		return profile;
	}
	
	private String getUserName() {
		return getPersonInEdition().getService(PersonService.class).getUserName();
	}
}
