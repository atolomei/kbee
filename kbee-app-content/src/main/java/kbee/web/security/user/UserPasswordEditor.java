package kbee.web.security.user;

import java.time.OffsetDateTime;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.ValidationError;

import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserProfile;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.user.KbeeUserProfile;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.event.EditEvent;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.PasswordField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.util.logging.Logger;
import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class UserPasswordEditor extends DomainObjectEditor<UserProfile> {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(UserEditor.class.getName());
	
	final boolean is_root = ServiceLocator
			.getService(SecurityService.class)
			.isRoot(); 
	final boolean is_domain_admin = ServiceLocator
			.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_security_admin = is_root || is_domain_admin || 
			ServiceLocator
			.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.SECURITY.getId());
	final boolean is_external = !is_root && !is_domain_admin && 
			ServiceLocator
			.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.EXTERNAL_USER.getId());
	
	private boolean editingpasswords = false;
	private String password1, password2 = "";

	private static final int MINIMUM_LENGTH = 8;
	
	class PasswordsValidator implements IFormValidator {

		private static final long serialVersionUID = 1L;
		
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

			if (!UserPasswordEditor.this.editingpasswords)
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

	
	 private boolean is_my_account = true;
	 private boolean is_edit_password_enabled = true;
	 
	 private boolean isHidden = true;
	 
	 protected boolean isHidden() {
		 return ( isHidden);
	 }
	 
	 protected void setHidden( boolean b) {
		 this.isHidden=b;
	 }
	 
	public UserPasswordEditor(String id, IModel<UserProfile> model, boolean is_my_account) {
		super(id, model);
		 this.is_my_account= is_my_account;
	}

	/**
	 * Password 
	 * Plain Password 
	 */
	@Override
	public void onInitialize() {
			super.onInitialize();

		setOutputMarkupId(true);
		setEditionEnabled(false);
		
		this.is_edit_password_enabled =  isRoot() || isAdminSessionUser() || (is_my_account && getModel().getObject().isChangePasswordEnabled());

		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);

		WebMarkupContainer pasword_disabled = new WebMarkupContainer("pasword-disabled") {
			public boolean isVisible() {
				return !is_edit_password_enabled;
			}
			
		};
		form.add(pasword_disabled);
		String lastp="";
		
		if (getModel().getObject().getUser().getPasswordLastModifiedDate()!=null) {
			lastp=ServiceLocator.getService(DateTimeService.class).format( 
				getModel().getObject().getUser().getPasswordLastModifiedDate(), 
				getSessionUser().getTimeZone(), 
				getSessionUser().getLocale(), DateTimeService.Dow_Month_Day_Year_hh_mm);
		}
		form.add((new Label("last-time", new StringResourceModel("password-last-time", this, null).getObject().replace("{0}", lastp))).setEscapeModelStrings(false));

							
		WebMarkupContainer passwords = new WebMarkupContainer("passwords") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return isEditionEnabled() && editingpasswords;
			}
		};

		passwords.add(new PasswordField("password1", new PropertyModel<String>(this, "password1"), true));
		passwords.add(new PasswordField("password2", new PropertyModel<String>(this, "password2"), true));

		form.add(passwords);
		
		
		form.add(new AjaxLink<Void>("changepassword-link") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				if (!isEditionEnabled()) {
					edit(target);
				}
				setPassword1("");
				setPassword2("");
				getPassword1Field().onBeforeRender();
				getPassword1Field().clearInput();
				getPassword2Field().onBeforeRender();
				getPassword2Field().clearInput();
				target.focusComponent(getPassword1Field().getInput());
				editingpasswords = true;
				UserPasswordEditor.this.get("buttons").setVisible(true);
				
				target.add(UserPasswordEditor.this);
			}

			@Override
			public boolean isVisible() {

				if (UserPasswordEditor.this.getModel().getObject().getEntity().getState() == ObjectState.DELETED)
					return false;

				// during edition these are not visible
				if (editingpasswords)
					return false;

				// if it is my account and the person has edit enabled
				if (is_my_account)
					return is_edit_password_enabled;

				if (isSupportSessionUser() && !isRoot())
					return false;

				/**
				 * if the user being edited is root, only root (session user) can change pwd)
				 */
				if ( getUserUnderEdition() .getUserName().startsWith("root@")) {
					if (getSessionUser().getUserName().startsWith("root@"))
						return true;
					return false;
				}
				
				if (!is_security_admin) {
					return false;
				}
				
				return true;
			}
		});

		form.add(new PasswordsValidator());

		add(form);
		
		
		EditButtonsV5<UserProfile> buttons = new EditButtonsV5<UserProfile>(this) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isEnabled() {
				
				if (getModel().getObject().getEntity().getState() == ObjectState.DELETED)
					return false;

				if (is_my_account)
					return  is_edit_password_enabled;

				if (isSupportSessionUser() && !isRoot())
					return false;

				// Only Root can Edit root user
				//
				if (UserPasswordEditor.this.getModel().getObject().getUser().getUserName().startsWith("root@")
						&& !getSessionUser().getUserName().startsWith("root@"))
					return false;

				return true;
			}
		};

		
		// If the user being edited is "root" but the session user is not "root" then
		// disable
		// only root can edit root user.
		//
		if (getModel().getObject().getUser().getUserName().startsWith("root@")
				&& !getSessionUser().getUserName().startsWith("root@"))
			buttons.setVisible(false);

		if (!isAdminSessionUser() && is_my_account && ! is_edit_password_enabled)
					buttons.setVisible(false);

		add(buttons);
		
		buttons.setVisible(false);
		
	}
	
	@Override
	public void cancel(AjaxRequestTarget target) {
		super.cancel(target);
		UserPasswordEditor.this.get("buttons").setVisible(false);
	}

	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				
				ServiceLocator.getService(SecurityContentMgmtService.class).update(getModelObject(), getUpdatedParts());
				fire(new EditEvent<UserProfile>(target, getModel()));
				
				UserPasswordEditor.this.get("buttons").setVisible(false);
				
				
			}
		} catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	@Override
	public void setEditionEnabled(boolean editionEnabled) {
		super.setEditionEnabled(editionEnabled);
		editingpasswords = false;
	}

	public String getPassword1() {
		return password1;
	}

	public void setPassword1(String password) {

		((KbeeUser) this.getUserUnderEdition()).setPassword(password);
		((KbeeUser) this.getUserUnderEdition()).setPasswordLastModifiedDate(OffsetDateTime.now());
		
		
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

	public User getUserUnderEdition() {
		User user = getModelObject().getUser();
		if (user == null) {
			user = new KbeeUser();
			((KbeeUserProfile) getModelObject()).setUser(user);
		}
		return user;
	}
}
