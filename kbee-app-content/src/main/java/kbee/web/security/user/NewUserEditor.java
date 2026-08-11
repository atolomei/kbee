package kbee.web.security.user;



import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.EmailAddressValidator;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.UserSet;
import com.novamens.content.model.UserSubset;
import com.novamens.content.notes.UserNotesService;
import com.novamens.content.resource.KBFile;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.IQLRule;
import com.novamens.content.security.Role;
import com.novamens.content.security.RolesService;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.service.UserImagesService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserService;
import com.novamens.content.web.security.markup.RulePage;
import com.novamens.content.web.suggestion.service.UserSuggestionService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.email.EmailService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.content.user.KbeeUserRole;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.util.KeyValue;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.Form;

import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.model.ObjectModel;
import kbee.email.EmailBuilderWelcomeMessagePremium;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.AutoCompleteFieldV5;
import kbee.web.form.EditButtonsV5;

import kbee.web.security.UsernameValidator;

import com.novamens.wicket.markup.html.form.Form.Disposition;

@SuppressWarnings("serial")
public class NewUserEditor extends ObjectEditor<NewUserData> {
	
	public static String SPECIALS = "[()|!@#\\$%\\^-_ ']";
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(NewUserEditor.class.getName());
	
	private boolean usernameupdated = false;
	private IModel<Person> usermodel;
	private IModel<User> clone_roles_model;
	private KeyValue<String> startPage = null;
	private List<KeyValue<String>> list =null;
	private IModel<User> base_user;

	private boolean is_clone_user = false;
	
	final boolean is_root		= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_admin		= is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	/**
	 * @param id
	 * @param model
	 * 
	 */
	public NewUserEditor(String id, IModel<NewUserData> model, IModel<User> b) {
		super(id, model);

		setOutputMarkupId(true);
		setEditionEnabled(true);
		
		this.setBaseUser(b);

		String start_p = null;
		
		is_clone_user = (this.getBaseUser()!=null);
		
		if (this.getBaseUser()!=null) 
			start_p=getContentDao().findUserProfileByUser(this.getBaseUser().getObject()).getStartPage();
		
		if (start_p==null) {
			if (isDomainKbee())
				start_p="domains";
			else
				start_p=getDomain().getDomainType()!=DomainType.EXPRESS ? "mytasks" : "library";
		}
			
		//MenuBreadCrumbPanel<Void> panel = new MenuBreadCrumbPanel<Void>("breadcrumb", null, new SecurityBC(), new UsersBC());
		//add(panel);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		WebMarkupContainer clo=new  WebMarkupContainer("user-clone-alert");
		form.add(clo);
		clo.setVisible(this.getBaseUser()!=null);
		String uclone = "";
		if (this.getBaseUser()!=null && this.getBaseUser().getObject()!=null) {
			StringResourceModel str = new StringResourceModel("clone-message", this);
			String url = getServerUrl( getDomain() ) +"/security/users/"+ this.getBaseUser().getObject().getId().toString();
			uclone = str.getObject().replace("{0}", this.getBaseUser().getObject().getDisplayName()).replace("{1}", url);
		}
		clo.add((new Label("clone", uclone)).setEscapeModelStrings(false));
		
		String url=getServerUrl()+"/"+getDomain().getId().toString()+"/settings";
		
		WebMarkupContainer dpc=new  WebMarkupContainer("defaultpassword-container");
		
		dpc.setVisible(getDomain().getDefaultPassword()!=null && getDomain().getDefaultPassword().length()>0);
		
		String defaultpassword = getDomain().getDefaultPassword()!=null ?  getDomain().getDefaultPassword() : "";
		dpc.add( 
					(new Label("defaultpassword", 
					(new StringResourceModel("defaultpassword", this, null).getObject().replace("{0}", defaultpassword)).replace("{1}", url)
					)
				).setEscapeModelStrings(false));
		form.add(dpc);
		
		
		for (KeyValue<String> p:getStartPages()) {
			if (p.getDisplayName()!=null && p.getDisplayName().equals(start_p)) {
				setStartPage(p);
				break;
			}
		}
	
		/**
		if(this.getStartPage() == null) {
			if (isDomainKbee()) {
				this.setStartPage(getStartPages().get(0));
			}
			else {
				this.setStartPage(getStartPages().get(0));
			}
		}
	
		form.add(new ChoiceField<XAddrray>("startpage", new PropertyModel<XArggray>(this, "startPage"),
				new PropertyModel<List<XAddrray>>(this, "startPages"), true) {
			@Override
			protected String getDisplayValue(XArddray value) {
				return value.getValue();
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				setUpdatedPart("Start page: " + getValue().getValue());
			}
			
			@Override
			public boolean isVisible() {
				return getDomain().getDomainType()!=DomainType.FREE;
			}
			
			@Override
			public boolean isEnabled() {
				return getDomain().getDomainType()!=DomainType.FREE;
			}
		});
*/
		
		form.add(new TextField<String>("firstName") {
			public void onUpdate(AjaxRequestTarget target) {
				NewUserEditor.this.onUpdate(target);
			}
		});
		
		form.add(new TextField<String>("lastName", true) {
			public void onUpdate(AjaxRequestTarget target) {
				NewUserEditor.this.onUpdate(target);
			}
		});
		
		form.add(new TextField<String>("email", true, EmailAddressValidator.getInstance()));
		
		form.add(new BooleanField("resetPassword"));
		
		form.add(new TextField<String>("userName", true, new UsernameValidator()) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				usernameupdated = true;
				if (getValue()!=null)
					setValue(getValue().replaceAll("\\s", ""));
			}
			@Override
			protected IModel<String> getHelpText() {
				StringResourceModel model = new StringResourceModel("userName.help", NewUserEditor.this);
				model.setParameters(NewUserEditor.this.getDomain().getName());
				return model;
			}
		});
		

		AutoCompleteFieldV5<User> usel = new AutoCompleteFieldV5<User>("clone-user-roles", new PropertyModel<User>(this, "cloneRolesUser"), false) {
			private static final long serialVersionUID = 1L;
						
			@Override
			protected IModel<String> getHelpText() {
				StringResourceModel model = new StringResourceModel("same-as.help", NewUserEditor.this);
				return model;
			}
			
			@Override
			public IModel<String> getLabel() {
				return new StringResourceModel("same-as", NewUserEditor.this);
			}
			
			@Override
			public boolean isVisible() {
				return !is_clone_user;
			}
			
			@Override
			public boolean isEnabled() {
				return !is_clone_user;
			}

			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				return ServiceLocator.getService(UserSuggestionService.class).getSuggestions(pattern);
			}

			@Override
			public String getHistoryKey() {
				return "new-user";
			}

			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
					if (getValue() != null) {
						setCloneRolesUser(new ObjectModel<User>(getValue()));
						setBaseUser(getCloneRolesUser());
					}
				}
			};
		
		form.add(usel);
		
		form.add(new NewUserGroupsEditor() {
			@Override
			public boolean isVisible() {
				return false;
			}
		});

	 

		add(form);
		
		add(new EditButtonsV5<NewUserData>(this) {
			@Override
			public boolean isVisible() {
				
				if (!isEditionEnabled())
					return false;
				
				if (is_admin)
					return true;
				
				 
				
				return true;
			}
		});
		
		WebMarkupContainer feedbackcontainer = new WebMarkupContainer("feedback-container") {
			@Override
			public boolean isVisible() {
				return !isEditionEnabled();
			}
		};

		feedbackcontainer.add( (new Label("new-user-info", new Model<String>() {
			public String getObject() {
				return getNewUserInfo().getObject();
			}
		})).setEscapeModelStrings(false));
		
		feedbackcontainer.add(new Link<Void>("create-link") {
			public void onClick() {
				Page page = new NewUserPage(new Model<NewUserData>(new NewUserData()), null);
				setResponsePage(page);
			}
		});
		
		feedbackcontainer.add(new Link<Void>("edit-link") {
			public void onClick() {
				onEdit(getUserModel());
			} 
		});
		
		add(feedbackcontainer);
	}
	
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				
				long ustart=System.currentTimeMillis();
				
				String full_username = getModelObject().getUserName().replaceAll("\\s", "").toLowerCase() + "@"+ NewUserEditor.this.getDomain().getName();

				logger.debug(full_username);
				
				List<KbeeGlobalRole> list = new ArrayList<KbeeGlobalRole>();
				List<Role> u_role=new ArrayList<Role>();
				
				if (getModelObject().getDomain_admin().booleanValue()) {
					List<Role> canonical_roles = getContentSecurityDao().getCanonicalRoles(getDomain());
					for (Role role: canonical_roles) {
						if (role.getAlias().equals("domain-admin")) 
							u_role.add(role);
					}
				}
				
				// ----------------
				//
				// Create User
				//
				//
				Person person = (Person) ServiceLocator.getService(ObjectFactoryService.class).createUser( getModelObject().getFirstName(),
																										   getModelObject().getLastName(),
																										   getModelObject().getEmail(),
																										   full_username,
																										   ObjectState.ENABLED,
																										   getModelObject().getUser_email().booleanValue(),
																										   getModelObject().getGroups(),
																										   list,
																										   u_role);
				setUserModel(new ObjectModel<Person>(person));
				
				
				// -------------
				//
				// if there is a Base User -> clone groups (except Domain Admin)
				//
				if 	(this.getBaseUser()!=null) {
					
					UserProfile up=getContentDao().findUserProfileByUser(this.getBaseUser().getObject());
					List<UserRole> u_list = up.getRoles();
					
					List<UserRole> u_dest_list = new ArrayList<UserRole>();

					u_dest_list.addAll(person.getProfile(UserProfile.class).getRoles());
					
					for (UserRole ur: u_list) {
					
						if (is_admin || !isDomainAdmin(ur.getRole())) {
							
							Role role =  getContentSecurityDao().findRoleById((Long) ur.getRole().getId());
							
							logger.debug(role.getClass().getName());
							
							if (ur.getRole()!=null && (!ur.getRole().isDefault())) {							
								if (ur.getEntity() != null) {
											KbeeUserRole k_ur = new KbeeUserRole(
											getContentSecurityDao().findEntityRoleById((Long) ur.getRole().getId()), 
											person.getProfile(UserProfile.class).getUser(), ur.getEntity());
											u_dest_list.add(k_ur);
								}
								else 
								{	
									KbeeUserRole k_ur = new KbeeUserRole(
									getContentSecurityDao().findGeneralRoleById((Long) ur.getRole().getId()), 
									person.getProfile(UserProfile.class).getUser(), ur.getEntity());
									u_dest_list.add(k_ur);
	
								}
							}
						}
					}

					person.getService(RolesService.class).update(u_dest_list);
				}
				
				if (logger.isDebugEnabled()) {
					long end=System.currentTimeMillis();
					logger.debug("User creation : " + String.valueOf(end-ustart) + " ms");
				}
	
				// Welcome Message DEPRECATED
				//
				//
				if (getModelObject().getResetPassword())  {
					// DomainType type=person.getDomain().getDomainType();
					//if (type==DomainType.COMPLIANCE){
					//	EmailBuilderWelcomeMessageCompliance builder = new EmailBuilderWelcomeMessageCompliance(person, getModelObject().getEmail(), person.getFirstLastName()); 
					//	ServiceLocator.getService(EmailService.class).send(builder);
					//}
					//else {
						
					EmailBuilderWelcomeMessagePremium builder = new EmailBuilderWelcomeMessagePremium(person, getModelObject().getEmail(), person.getFirstLastName()); 
 					ServiceLocator.getService(EmailService.class).send(builder);
 					
					// }
				}

				
				/**
				try {
					
					String title;
					String text;
					
					if (getDomain().getLocale().getLanguage().equals("es")) {
						title = getContentDao().findSystemParameterValueByKey("welcome-note.title_es", "Qué es Mis Notas ?");
						text = getContentDao().findSystemParameterValueByKey("welcome-note.text_es", "<p>Es un panel donde podés crear notas personales simples.</p><p>Las notas pueden incluir <a href=\"http://kbee.io\">links</a> y formatos como <strong>negrita</strong>"
								+ " o <em>itálicas</em>.</p><p>&nbsp;</p><p>Las Notas son privadas, nadie más puede leerlas o editarlas.</p>");
					}
					else {
						title = getContentDao().findSystemParameterValueByKey("welcome-note.title", "What is My Notepad ?");
						text  = getContentDao().findSystemParameterValueByKey("welcome-note.text", "<p>The Notepad is a panel easily accesible from the toolbar where you can create and manage simple notes.</p><p>Notes can include&nbsp;<a href=\"http://kbee.io\">links</a> and formats like <strong>bold</strong> or <em>italic</em>.</p><p>&nbsp;</p><p>Notes are private, no one else can read or edit them.</p>");
					}
					
					((KbeeUser) person.getProfile(UserProfile.class).getUser()).getService(UserNotesService.class).createUserNote(title, text);
				
				} catch (Exception e) {
					logger.error(e);
				}**/
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
		finally {
			
			
		}
	}
	
	
	private boolean isDomainAdmin(Role role) {
			return (role.getAlias().equals(KbeeGlobalRole.DOMAIN_ADMIN.getId()));
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		try {
			List<KBFile> lf = ServiceLocator.getService(UserImagesService.class).getImages();
			logger.info("Default Images loaded to fill cache. Total: " + String.valueOf(lf.size()));
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	
	public void setUserModel(IModel<Person> model) {
		usermodel = model;
	}
	
	
	public IModel<Person> getUserModel() {
		return usermodel;
	}
	
	
	public void cancel(AjaxRequestTarget target) {
		super.cancel(target);
		onCancel(target);
	}
	
	
	public void onCancel(AjaxRequestTarget target) {
	}
	
	
	public void onEdit(IModel<Person> model) {
	}
	
					
	public void onNewRule(IModel<Person> model) {
		
		// create Rule
		try {
			IQLRule rule = ServiceLocator.getService(com.novamens.content.service.SecurityContentMgmtService.class).createRule(SecurityRule.RULE_WIZARD_IQL, getDomain(), model.getObject().getProfile(UserProfile.class).getUser());
			Page page = new RulePage(new ObjectModel< IQLRule>(rule), true);
			setResponsePage(page);
			
		}
		catch (ContentCreationException | ContentMgmtException e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
		}
	}

	
	
	public UserSet getUserSet() {
		UserSet userset= null;
		for (DataSet dataset : getDataSets()) {
			if (dataset instanceof UserSet && !(dataset instanceof UserSubset)) {
				userset = (UserSet)dataset;
				break;
			}
		}
		return userset;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (usermodel!=null)
			usermodel.detach();
		
		if (this.base_user!=null)
			this.base_user.detach();
		
		if (this.clone_roles_model!=null)
			this.clone_roles_model.detach();
	}
	
	
	@SuppressWarnings("unchecked")
	protected void onUpdate(AjaxRequestTarget target) {
		if (!usernameupdated) {
			
			String firstName = ((TextField<String>)getForm().get("firstName")).getValue();
			String lastName  = ((TextField<String>)getForm().get("lastName")).getValue();
			
			String userName = "";
			
			if (firstName!=null && !"".equals(firstName)) 
				userName += firstName.toLowerCase().charAt(0);
			
			if (lastName!=null && !"".equals(lastName)) 
				userName += lastName.toLowerCase().trim().replaceAll(SPECIALS, "");
			
			if (!"".equals(userName)) {
				getModelObject().setUserName(userName);
				((TextField<String>)getForm().get("userName")).setValue(userName);
				target.add(getForm());
			}
		}
	}
	
	
	protected List<DataSet> getDataSets() {
		return getContentDao().getDataSets(getDomain());
	}
	
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	

	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected boolean isFreeVersion() {
		return getDomain().getDomainType()==DomainType.EXPRESS;
	}
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	private boolean isDomainKbee() {
		return getDomain().getName().equals("kbee");
			
	}
	

	public KeyValue<String> getStartPage() {
		return this.startPage;
	}
	
	public void setStartPage(KeyValue<String> pair) {
		this.startPage=pair;
	}
	

	public IModel<User> getCloneRolesUser() {
		return this.clone_roles_model;
	}
	
	
	public void setCloneRolesUser(IModel<User> model_clone) {
		this.clone_roles_model=model_clone;
	}


	public List<KeyValue<String>> getStartPages() {

		if (list!=null)
			return list;
				
		list = new ArrayList<KeyValue<String>>();

		if (isDomainKbee()) {		
			list.add(new KeyValue<String>("domains", new StringResourceModel( "p_domains", this, null).getString()));
			list.add(new KeyValue<String>("dashboard", new StringResourceModel( "p_dashboard", this,null).getString()));
			list.add(new KeyValue<String>("api dashboard", new StringResourceModel( "p_api_dashboard", this,null).getString()));
			list.add(new KeyValue<String>("api reports", new StringResourceModel( "p_api_reports", this,null).getString()));
		}

		if (getDomain().getDomainType()!=DomainType.EXPRESS) {
			list.add(new KeyValue<String>("mytasks", new StringResourceModel( "p_mytasks", this,null).getString()));
			list.add(new KeyValue<String>("monitor", new StringResourceModel( "p_monitor", this,null).getString()));
			list.add(new KeyValue<String>("library", new StringResourceModel( "p_library", this,null).getString()));
			
			for (Site site: getPortalDao().getSitesPublic(getDomain())) {
				if (site.getState()==ObjectState.ENABLED && !site.isExternal())
					list.add(new KeyValue<String>(site.getKey(), "Portal - " + site.getTitle()));
			}
		}
		else {
			list.add(new KeyValue<String>("library", new StringResourceModel( "p_library", this,null).getString()));
		}
		
		

		Collections.sort(list, new Comparator<KeyValue<String>>() {
			@Override
			public int compare(KeyValue<String> a, KeyValue<String> b) {
				return a.getValue().compareToIgnoreCase(b.getValue());
			}
			
		});
		return list;
	}
	
	protected PortalDao getPortalDao() {
		return (PortalDao)ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}
	
	protected IModel<String> getNewUserInfo() {
		StringBuilder str = new StringBuilder();
		if (getUserModel()!=null && getUserModel().getObject()!=null) {
			str.append("<br /><b>"+ getLabelString("name")+"</b>: " + getUserModel().getObject().getFirstLastName());
			str.append("<br /><b>"+getLabelString("username")+"</b>: " + getUserModel().getObject().getProfile(UserProfile.class).getUser().getUserName());
			if (getDomain().getDefaultPassword()!=null) {
				str.append("<br /><b>"+getLabel("password").getObject()+"</b>. " + getDomain().getDefaultPassword());
			}
			str.append("<br /><b>"+getLabelString("email")+"</b>: " + getUserModel().getObject().getEmail());
			str.append("<br /><b>"+getLabelString("language")+"</b>: " + getUserModel().getObject().getProfile(UserProfile.class).getUser().getLocale().getLanguage());
			str.append("<br /><b>"+getLabelString("timezone")+"</b>: " + getUserModel().getObject().getProfile(UserProfile.class).getUser().getTimeZone());
			str.append("<br /><b>"+getLabelString("roles")+"</b>: ");
			int n= 0;
			for (UserRole up: getUserModel().getObject().getProfile(UserProfile.class).getRoles()) {
				if (n++>0)	str.append(" | ");	
				String value = up.getDisplayName();
				str.append(value);
			}
		}
		
		return new Model<String>(str.toString());
	}
	
	private String getServerUrl(Domain domain) {
		return getServerUrl();
	}
	
	public  IModel<User> getBaseUser() {
		return base_user;
	}
	
	public  void  setBaseUser(IModel<User> b) {
		this.base_user=b;
	}
}
