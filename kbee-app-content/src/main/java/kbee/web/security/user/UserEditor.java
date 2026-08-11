package kbee.web.security.user;

import java.time.OffsetDateTime;
import java.time.ZoneId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.entity.Person;
import com.novamens.content.service.DomainService;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserProfileType;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.user.KbeeUserProfile;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.event.EditEvent;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.security.ReservedUsername;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.BrandingService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KeyValue;
import com.novamens.util.PropertiesFactory;
import com.novamens.wicket.markup.html.form.BooleanSwitchField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.DateTimeField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.PasswordField;
import com.novamens.wicket.markup.html.form.StaticField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.util.logging.Logger;
import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.form.LocaleField;
import kbee.web.form.ZoneIdField;
import kbee.web.panel.AlertPanel;

/**
 * <p>
 * NOTE: We can not use getDomain() from the Session User because this Editor is
 * also used from the {@link DomainConsole} to edit root user of all domains.
 * </p>
 * 
 * <p>
 * My Account User from same Domain edits Account User from Domain kbee edits
 * Account
 * </p>
 * 
 * 
 * root@kbee / root
 * password:  3b6144f35f3e2f80a1f9446fafc389dd
 * 
 * 
 * 1. Send email to Reset Password to all users / selected users
 * 2. 
 * 
 */

@SuppressWarnings("serial")
public class UserEditor extends DomainObjectEditor<UserProfile> {
	private static final long serialVersionUID = 1L;
			
	private static Logger logger = Logger.getLogger(UserEditor.class.getName());
	
	
	final boolean is_root = ServiceLocator
		.getService(SecurityService.class)
		.isRoot(); 
	final boolean role_admin = is_root || ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_security = role_admin || 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.SECURITY.getId());
	final boolean is_external = !is_root && !role_admin && 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.EXTERNAL_USER.getId());

	private static final int MINIMUM_LENGTH = 8;

	private boolean editingpasswords = false;
	private String password1, password2 = "";
	
	private KeyValue<String> startPage = null;
	
	private List<KeyValue<String>> list =null;
	private List<String> li = null;
	
	private OffsetDateTime validityaccessdate;
	
	private IModel<ZoneId> zoneIdModel = null;
	
	private static String UserProfileTypes =
			PropertiesFactory
				.getInstance("kbee")
				.getProperties()
				.getProperty("kbee.user.profile.types", null);
	
	class UsernameValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			String username = validatable.getValue();
			if (username.contains("@")) {
				validatable.error(new ValidationError(this, "characters"));
				return;
			}
			String fullname = username + "@" + UserEditor.this.getModel().getObject().getDomain().getName();
			String reserved_candidate = username.toLowerCase() + "@"
					+ UserEditor.this.getModel().getObject().getDomain().getName();
			if (reserved_candidate.startsWith("suroot@") || reserved_candidate.startsWith("root@")
					|| reserved_candidate.startsWith("pending@")
					|| reserved_candidate.startsWith("supending@")
					|| reserved_candidate.startsWith(DomainService.WORKFLOW_USER+"@")
					|| reserved_candidate.startsWith(ReservedUsername.PUBLICRESOURCES.getUserName() + "@")
					|| reserved_candidate.startsWith("suworkflow@")) {
				validatable.error(new ValidationError(this, "reservedname"));
			}
			User user = ServiceLocator.getService(com.novamens.service.SecurityService.class)
					.findUserByUsername(fullname);
			if (user != null) {
				if (!user.getId().equals(getUser().getId())) {
					validatable.error(new ValidationError(this, "uniqueness"));
				}
			}
		}
	}

	/**
	 * 
	 * 
	 */
	class PasswordsValidator implements IFormValidator {

		public String SPECIALS = "[!@#\\$%\\^-_]";

		public boolean hasNumber(String pwd) {
			return pwd.matches(".*[0-9].*");
		}

		public boolean hasSpecials(String pwd) {
			return pwd.matches(SPECIALS);
		}

		public boolean hasCapitalLetter(String pwd) {
			return pwd.matches(".*[A-Z].*");
		}

		@Override
		public void validate(org.apache.wicket.markup.html.form.Form<?> form) {

			if (!UserEditor.this.editingpasswords)
				return;

			String pwd1 = (String) getPassword1Field().getInput().getDefaultModelObject();

			if (((String) getPassword1Field().getInput().getDefaultModelObject()) == null
					|| ((String) getPassword2Field().getInput().getDefaultModelObject()) == null) {
				ValidationError error = new ValidationError();
				error.addKey(getClass().getSimpleName());
				getPassword1Field().setError(error);
				getPassword2Field().setError(error);
				return;
			}

			else if (((String) getPassword1Field().getInput().getDefaultModelObject()).length() < MINIMUM_LENGTH) {
				ValidationError error = new ValidationError();
				error.addKey("minimunlength");
				getPassword1Field().setError(error);
				return;
			}

			else if (!hasNumber(pwd1)) {
				ValidationError error = new ValidationError();
				error.addKey("musthavedigit");
				getPassword1Field().setError(error);
				return;
			}

			else if (!hasCapitalLetter(pwd1)) {
				ValidationError error = new ValidationError();
				error.addKey("musthavecapitalletter");
				getPassword1Field().setError(error);
				return;
			}

			else {
				String password1 = (String) getPassword1Field().getInput().getDefaultModelObject();
				String password2 = (String) getPassword2Field().getInput().getDefaultModelObject();
				if (!password1.equals(password2)) {
					ValidationError error = new ValidationError();
					error.addKey(getClass().getSimpleName());
					getPassword2Field().setError(error);
					return;
				}
			}
		}

		@Override
		public FormComponent<?>[] getDependentFormComponents() {
			return new FormComponent<?>[0];
		}
	}
	
	
	
	
	
	

	/**
	 * Domain Admin can edit his/her account Root can edit his/her accoutn
	 * 
	 * 
	 */
	public UserEditor(String id, IModel<UserProfile> model, final boolean ismyaccount, final boolean portalmode) {
		super(id, model);

		setOutputMarkupId(true);
		setEditionEnabled(false);
		
		validityaccessdate = getModel().getObject().getUser().getValidityAccessDate();
		
		final boolean is_edit_person_enabled = (!ismyaccount) || 
				isRoot() || 
				isAdminSessionUser() || 
				getModel().getObject().isEditPersonEnabled(); // if it is my account and edit person is enabled

		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);

		WebMarkupContainer info = new WebMarkupContainer("account-disabled");
		info.setVisible(!is_edit_person_enabled);
		form.add(info);

		if (getModel().getObject().getUser().getUserName().startsWith("root@") ||getModel().getObject().getUser().isCanonical()) {

			StringResourceModel s_title=new StringResourceModel("system-user-title", this, null);
			StringResourceModel s_text=new StringResourceModel("system-user-text", this, null);
			AlertPanel<UserProfile> a=new AlertPanel<UserProfile>("system-user",  AlertPanel.INFO, getModel(), s_title, s_text);
			a.setIcon("fa-duotone fa-user-lock");
			form.add(a);
			
		}
		else {
			form.add( new InvisiblePanel("system-user"));	
		}
		
		

		
		String un = getUser().getUserName();

		if (un != null && un.trim().toLowerCase().startsWith("root@")) {
			form.add(new StaticField<String>("userName", new Model<String>() {
				public String getObject() {
					return "root";
				}
			}));
		} 
		
		else if (un != null && un.trim().toLowerCase().startsWith("workflow@")) {
			form.add(new StaticField<String>("userName", new Model<String>() {
				public String getObject() {
					return "workflow";
				}
			}));
		} 
		

		else if (un != null && un.trim().toLowerCase().startsWith(ReservedUsername.PUBLICRESOURCES.getUserName() + "@" )) {
			form.add(new StaticField<String>("userName", new Model<String>() {
				public String getObject() {
					return "publicresources";
				}
			}));
		} 

		
		else {
			TextField<String> username = new TextField<String>("userName", new Model<String>() {
				public String getObject() {
					String username = getUser().getUserName();
					username = username != null && username.contains("@") ? username.substring(0, username.indexOf("@"))
							: username;
					if (username == null) {
						UserProfile profile = UserEditor.this.getModel().getObject();
						if (profile != null) {
							if (profile.getPerson() != null && (profile.getPerson().getFirstName() != null
									|| profile.getPerson().getLastName() != null)) {
								Person person = profile.getPerson();
								if (person.getLastName() != null) {
									username = person.getLastName().toLowerCase().trim();
								}
								if (person.getFirstName() != null && !"".equals(person.getFirstName().trim())) {
									username = person.getFirstName().toLowerCase().trim().substring(0, 1) + username;
								}
							} else
								username = String.valueOf(System.currentTimeMillis());
						} else
							username = String.valueOf(System.currentTimeMillis());
					}
					return username;
				}

				public void setObject(String value) {
					((KbeeUser) getUser())
							.setUserName(value + "@" + UserEditor.this.getModel().getObject().getDomain().getName());
				}
			}, true, new UsernameValidator());

			if (ismyaccount) {
				username.setEnabled(false);
			}

			else {

				if (ReservedUsername.isReserved(getModel().getObject().getUser().getUserName())) 
					username.setEnabled(false);
				
				
				if (!this.isAdminSessionUser())
					username.setEnabled(false);
				else
					username.setEnabled(true);
			}
			form.add(username);
		}

		form.add((new StaticField<String>("domain", new Model<String>() {
			public String getObject() {
				return "@" + UserEditor.this.getModel().getObject().getDomain().getName();
			}
		})));

		
		String s=getModel().getObject().getStartPage();
		
		if (s==null) 
			s="home";
		
		for (KeyValue<String> p:getStartPages()) {
			if (p.getDisplayName()!=null && p.getDisplayName().equals(s)) {
				setStartPage(p);
				break;
			}
		}
		
		String sid = getModel().getObject().getUser().getId().toString();
		StaticField<String> pid     = new StaticField<String> ("id", new Model<String>(sid));
		form.add(pid);

		form.add(new ChoiceField<String>("iconset", new PropertyModel<String>(this, "iconSet"),	
				new PropertyModel<List<String>>(this, "iconSets"), true) {
			@Override
			protected String getDisplayValue(String value) {
				return new StringResourceModel(value, UserEditor.this, null).getObject();
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				setUpdatedPart("Menu Icon Set: " + getValue());
			}
			@Override
			public boolean isEnabled() {
				return !is_external && !portalmode;
			}
			@Override
			public boolean isVisible() {
				return isRoot() || getDomain().getDomainType()!=DomainType.EXPRESS;
			}
		});
		
		form.add(new ChoiceField<KeyValue<String>>("startpage", 
				new PropertyModel<KeyValue<String>>(this, "startPage"),	
				new PropertyModel<List<KeyValue<String>>>(this, "startPages"), true) {
			@Override
			protected String getDisplayValue(KeyValue<String> value) {
				return value.getValue();
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				setUpdatedPart("Start page: " + getValue().getValue());
			}
			@Override
			public boolean isEnabled() {
				return !is_external && !portalmode;
			}
			@Override
			public boolean isVisible() {
				return true;
			}
		});
		
		form.add(new ChoiceField<UserProfileType>("type", 
				new PropertyModel<UserProfileType>(this, "type"),	
				new PropertyModel<List<UserProfileType>>(this, "types"), true) {
			@Override
			protected String getDisplayValue(UserProfileType value) {
				return value.getLabel( getSessionUser().getLocale() );
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				setUpdatedPart("ProfileType: " + getValue().getLabel());
			}
			@Override
			public boolean isEnabled() {
				return !is_external && !portalmode;
			}
			@Override
			public boolean isVisible() {
				return true;
			}
		});

		
		form.add(new LocaleField("locale", new PropertyModel<Locale>(this, "userEditedLocale"), true) {
			@Override
			public List<Locale> getLocales() {
				List<Locale> list = new ArrayList<Locale>();
				list.add(Locale.ENGLISH);
				list.add(Locale.forLanguageTag("es"));
				return list;
			}
		});
		
		zoneIdModel = new Model<ZoneId>(getUser().getZoneId());
		form.add(new ZoneIdField("timezone", zoneIdModel, true));
		
		form.add(new BooleanSwitchField("tipoftheday", new PropertyModel<Boolean>(this, "tipOfTheDay")) {
			public boolean isVisible() {
				return ("true").equals(
						getDomain().getService(DomainSettingsService.class).get(DomainSettingsService.TIP_OF_THE_DAY));
			}
		});

		
		form.add(new BooleanSwitchField("usereditaccount", 			new PropertyModel<Boolean>(this, "editPersonEnabled")) {
			@Override
			public boolean isBorder() {
				return true;
			}
			@Override
			public boolean isVisible() {
				return !ismyaccount;
			}
		});
		
		
		form.add(new BooleanSwitchField("userchangepassword", 		new PropertyModel<Boolean>(this, "changePasswordEnabled")) {
			@Override
			public boolean isBorder() {
				return true;
			}
			@Override
			public boolean isVisible() {
				return !ismyaccount;
			}
		});
		
		form.add(new BooleanSwitchField("usersendemail", 			new PropertyModel<Boolean>(this, "sendFilesEmail")) {
			@Override
			public boolean isBorder() {
				return true;
			}
			@Override
			public boolean isVisible() {
				return !ismyaccount;
			}
		});

		
		form.add(new BooleanSwitchField("enabled", new PropertyModel<Boolean>(this, "userEnabled")) {
			@Override
			public boolean isBorder() {
				return true;
			}
			public boolean isVisible() {
				return false; // !ismyaccount;
			}
		});
		
		
		DateTimeField d = new DateTimeField("validityaccessdate", 
				ZoneId.of(getDomain().getTimeZone()), 
				new PropertyModel<OffsetDateTime>( this, "validityAccessDate"), false);
		//d.setLabel(UserEditor.this.getLabel("validityaccessdate"));
		form.add(d);
		
		WebMarkupContainer passwords = new WebMarkupContainer("passwords") {
			@Override
			public boolean isVisible() {
				return false; //return isEditionEnabled() && editingpasswords;
			}
		};

		passwords.add(new PasswordField("password1", new PropertyModel<String>(this, "password1"), true));
		passwords.add(new PasswordField("password2", new PropertyModel<String>(this, "password2"), true));
		form.add(passwords);

		add(form);

		EditButtonsV5<UserProfile> buttons = new EditButtonsV5<UserProfile>(this) {
			@Override
			public boolean isVisible() {
				if (getModel().getObject().getUser().getUserName().startsWith("root@")
						&& !getSessionUser().getUserName().startsWith("root@"))
					return false;
				if (!isAdminSessionUser() && ismyaccount && !is_edit_person_enabled)
					return false;
				if (!role_security && !ismyaccount)
					return false;
				return true;
			}
			@Override
			public boolean isEnabled() {
				if (getModelObject().getEntity().getState() == ObjectState.DELETED) {
					return false;
				}	
				if (ismyaccount) {
					return is_edit_person_enabled;
				}	
//				if (isSupportSessionUser() && !isRoot()) {
//					return false;
//				}	
				// Only Root can Edit root user
				if (UserEditor.this.getModelObject().getUser().getUserName().startsWith("root@")
						&& !getSessionUser().getUserName().startsWith("root@"))
					return false;
				if (!role_security) {
					return false;
				}	
				return true;
			}
		};

		add(buttons);
	}
	
	
	
	/**
	 * 
	 * 
	 * 
	 * 
	 */
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				
				if (getStartPage()!=null)
					getModelObject().setStartPage(getStartPage().getDisplayName());
				
				getModelObject().getUser().setTimeZone(getZoneIdModel().getObject().getId());
				ServiceLocator.getService(SecurityContentMgmtService.class).update(getModelObject(), getUpdatedParts());
				
				// AjaxWicketEvet to refresh screen
				//
				fire(new EditEvent<UserProfile>(target, getModel()));
			}
		} catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}

	
	public Locale getUserEditedLocale() {
		return getUser().getLocale();
	}

	public void setUserEditedLocale(Locale locale) {
		getUser().setLocale(locale);
	}
	
	public List<String> getUithemes() {
		return ServiceLocator.getService(BrandingService.class).getUIThemes();
	}

	public boolean isUserEnabled() {
		return getUser().isEnabled();
	}

	public void setUserEnabled(boolean value) {
		if (value) {
			getModel().getObject().getPerson().setState(ObjectState.ENABLED);
			getUser().setStateEnabled();
			
		}
		else {
			getModel().getObject().getPerson().setState(ObjectState.ARCHIVED);
			getUser().setStateArchived();
		}
	}

	public boolean isEditPersonEnabled() {
		return getModelObject().isEditPersonEnabled();
	}

	public void setEditPersonEnabled(boolean value) {
		getModelObject().setEditPersonEnabled(value);
	}
	
	public boolean isChangePasswordEnabled() {
		return getModelObject().isChangePasswordEnabled();
	}
	
	public void setChangePasswordEnabled(boolean value) {
		getModelObject().setChangePasswordEnabled(value);
	}

	public void setEmailNotifications(boolean b) {
		getModelObject().setEmailNotifications(b);
	}

	public boolean isEmailNotifications() {
		return getModelObject().isEmailNotifications();
	}

	public void setClient(boolean b) {
		((KbeeUserProfile) getModelObject()).setClientProfile(true);
	}
	
	public boolean isClient() {
		return getModelObject().isClientProfile();
	}
	
	public String getIconSet() {
		return getModelObject().getIconSet();
	}
	
	public void setIconSet(String s) {
		getModelObject().setIconSet(s);
	}
	
	public void setUitheme(String b) {
		getModelObject().setUitheme(b);
	}

	public String getUitheme() {
		return getModelObject().getUitheme();
	}
					
	public void setEmailPendingNotifications(boolean b) {
		getModelObject().setEmailPendingNotifications(b);
	}

	public boolean isEmailPendingNotifications() {
		return getModelObject().isEmailPendingNotifications();
	}

	public void setEmailRuleNotifications(boolean b) {
		getModelObject().setEmailRuleNotifications(b);
	}

	public boolean isEmailRuleNotifications() {
		return getModelObject().isEmailRuleNotifications();
	}

	public boolean isTipOfTheDay() {
		return getModelObject().isTipOfTheDay();
	}

	public void setTipOfTheDay(boolean value) {
		getModelObject().setTipOfTheDay(value);
	}
	
	public boolean isSendFilesEmail() {
		return getModelObject().isSendFilesEmail();
	}
	
	public void setIsSendFilesEmail(boolean value) {
		getModelObject().setSendFilesEmail(value);
	}

	public void setSendFilesEmail(boolean value) {
		getModelObject().setSendFilesEmail(value);
	}
	
	public String getPassword1() {
		return password1;
	}
	
	public OffsetDateTime getValidityAccessDate() {
		return validityaccessdate;
	}

	public void setValidityAccessDate(OffsetDateTime validityaccessdate) {
		this.validityaccessdate = validityaccessdate;
		((KbeeUser) getModel().getObject().getUser()).setValidityAccessDate(validityaccessdate);
		
	}

	public void setPassword1(String password) {
		((KbeeUser) getUser()).setPassword(password);
		this.password1 = password;
	}

	public TextField<?> getPassword1Field() {
		return (TextField<?>) get("form:passwords:password1");
	}

	public String getPassword2() {
		return password2;
	}

	public TextField<?> getPassword2Field() {
		return (TextField<?>) get("form:passwords:password2");
	}

	public void setPassword2(String password) {
		this.password2 = password;
	}
	
	public UserProfileType getType() {
		return getModelObject().getType();
	}

	public void setType(UserProfileType type) {
		((KbeeUserProfile)getModelObject()).setType(type);
	}

	@Override
	public void onDetach() {
		// zlist = null;
		super.onDetach();
	}

	/**
	 * <p>
	 * This is the Domain of the user being edited, so thee method
	 * <code>ServiceLocator.getService(UserService.class).getDomain();</code> that
	 * returns the domain of the Session User can not be used.
	 * </p>
	 **/
	public Domain getDomain() {
		return UserEditor.this.getModelObject().getDomain();
	}

	/**
	 * User being edited (NOT SESSION USER)
	 * 
	 * @return
	 */
	public User getUser() {
		User user = getModelObject().getUser();
		if (user == null) {
			user = new KbeeUser();
			((KbeeUserProfile) getModelObject()).setUser(user);
		}
		return user;
	}

	@Override
	public void setEditionEnabled(boolean editionEnabled) {
		super.setEditionEnabled(editionEnabled);
		editingpasswords = false;
	}
	
	
	public KeyValue<String> getStartPage() {
		return this.startPage;
	}
	
	public void setStartPage(KeyValue<String> pair) {
		this.startPage=pair;
	}
	
	public IModel<ZoneId> getZoneIdModel() {
		return zoneIdModel;
	}
	
	public void setZoneIdModel(IModel<ZoneId> m) {
		zoneIdModel=m;
	}
	
	public List<String> getIconSets() {
		
		if (li!=null)
			return li;
		
		li = new ArrayList<String>();
	
		li.add("fad");
		li.add("far");
		li.add("fas");
		li.add("fal");
		
		return li;
	}
	
	
	
	public List<KeyValue<String>> getStartPages() {
		if (list!=null)
			return list;
		list = new ArrayList<KeyValue<String>>();
		if (isDomainKbee()) {
			list.add(new KeyValue<String>("domains", getLabelString("p_domains")));
			list.add(new KeyValue<String>("dashboard",	new StringResourceModel( "p_dashboard", this,null).getString())
			);
			list.add(new KeyValue<String>("api dashboard", new StringResourceModel( "p_api_dashboard", this,null).getString()));
			list.add(new KeyValue<String>("api reports", new StringResourceModel( "p_api_reports", this,null).getString()));
		}
		list.add(new KeyValue<String>("home", new StringResourceModel( "p_home", this,null).getString()));
		for (Site site: getPortalDao().getSites(getDomain())) {
			if (site.getState()==ObjectState.ENABLED && !site.isExternal())
				list.add(new KeyValue<String>(site.getKey(), "Portal - " + site.getTitle()));
		}
		if (role_admin) {
			list.add(new KeyValue<String>("users", "Users"));
		}
		Collections.sort(list, new Comparator<KeyValue<String>>() {
			@Override
			public int compare(KeyValue<String> a, KeyValue<String> b) {
				return a.getValue().compareToIgnoreCase(b.getValue());
			}
			
		});
		return list;
	}
	
	List<UserProfileType> types;
	
	public List<UserProfileType> getTypes() {
		
		if (types!=null)
			return types;
		
		List<Integer> enabled = UserProfileTypes!=null 
			? Arrays.stream(UserProfileTypes.split(","))
				.map(String::trim)
				.map(Integer::valueOf)
				.collect(Collectors.toList())
			: new ArrayList<>();
		
		types = new ArrayList<>();
		if (enabled.isEmpty() || enabled.contains(UserProfileType.READONLY.getId()))
			types.add(UserProfileType.READONLY);
		if (enabled.isEmpty() || enabled.contains(UserProfileType.WORKFLOW_PARTICIPANT.getId()))
			types.add(UserProfileType.WORKFLOW_PARTICIPANT);
		if (enabled.isEmpty() || enabled.contains(UserProfileType.EMPLOYEE.getId()))
			types.add(UserProfileType.EMPLOYEE);
		if (enabled.isEmpty() || enabled.contains(UserProfileType.CLIENT.getId()))
			types.add(UserProfileType.CLIENT);
		
		types.sort( new Comparator<UserProfileType>() {
			@Override
			public int compare(UserProfileType o1, UserProfileType o2) {
				try {
					return o1
							.getLabel(getSessionUser()
								.getLocale())
								.compareToIgnoreCase(o2.getLabel(getSessionUser().getLocale()));
				} 
				catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
		});
		
		return types;
	}
	
	protected PortalDao getPortalDao() {
		return (PortalDao)ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}
}
