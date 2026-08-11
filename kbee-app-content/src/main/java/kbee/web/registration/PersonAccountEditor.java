package kbee.web.registration;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.security.Role;
import com.novamens.content.service.PersonService;
import com.novamens.content.user.externalLogin.ExternalPlatformId;
import com.novamens.kbee.content.script.KbeeClassificableScriptWrapper;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.CheckField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.PasswordField;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.security.user.PasswordsValidator;

@SuppressWarnings("serial")
public class PersonAccountEditor extends DomainObjectEditor<PersonMember>  {
	private static final long serialVersionUID = 1L;
	
	private String  name, surname, email, organization, password1, password2;
	private Boolean googleAuth = true;
	private Boolean facebookAuth = true;
	
	public PersonAccountEditor(String id, IModel<PersonMember> model) {
		super(id, model);
		
		setName(getModelObject().getFirstName());
		setSurname(getModelObject().getLastName());
		setOrganization(getOrganization(getModelObject()));
		setEmail(getModelObject().getEmail());
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new TextField<String>("name", new PropertyModel<String>(this, "name")));
		((TextField<?>)form.get("name")).setAutoFocus(true);
		form.add(new TextField<String>("surname", new PropertyModel<String>(this, "surname")));
		form.add(new TextField<String>("email", new PropertyModel<String>(this, "email")) {
			public boolean isInputEnabled() {
				return false;
			}
		});
		form.add(new TextField<String>("organization", new PropertyModel<String>(this, "organization")) {
			public boolean isVisible() {
				return getOrganization()!=null;
			}
		});
		((TextField<?>)form.get("organization")).setEnabled(false);
		form.add(new CheckField("googleAuth", new PropertyModel<Boolean>(this, "googleAuth")));
		form.add(new CheckField("facebookAuth", new PropertyModel<Boolean>(this, "facebookAuth")));
		form.add(new PasswordField("password1", new PropertyModel<String>(this, "password1")));
		form.add(new PasswordField("password2", new PropertyModel<String>(this, "password2")));
		
		form.add(new PasswordsValidator() {
			@Override
			protected TextField<?> getPassword1Field() {
				return PersonAccountEditor.this.getPassword1Field();
			}
			@Override
			protected TextField<?> getPassword2Field() {
				return PersonAccountEditor.this.getPassword2Field();
			}
		});
		
		add(form);
		
		SubmitButton sb = new SubmitButton(getForm()) {
			@Override 
			protected void onSubmit(AjaxRequestTarget target) {
				super.onSubmit(target);
				createUser();
				onCreate(target);
			}
			@Override
			public boolean isVisible() {
				return true;
			}
			@Override
			public IModel<String> getLabel() {
				return new Model<String>("Crear");
			}
			@Override
			protected IModel<String> getWorkingLabel() {
				 return PersonAccountEditor.this.getLabel("button.submiting");
			}
		};

		add(sb);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public Boolean getGoogleAuth() {
		return googleAuth;
	}
	

	public String getPassword1() {
		return password1;
	}

	public void setPassword1(String password1) {
		this.password1 = password1;
	}

	public String getPassword2() {
		return password2;
	}

	public void setPassword2(String password2) {
		this.password2 = password2;
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

	public void onBeforeRender() {
		super.onBeforeRender();
		((org.apache.wicket.markup.html.form.TextField<?>)((PasswordField)get("form:password1")).getInput()).setRequired(false);
		((org.apache.wicket.markup.html.form.TextField<?>)((PasswordField)get("form:password2")).getInput()).setRequired(false);
	}
	
	private void createUser() {
		ServiceLocator.getService(SecurityService.class).authenticate("root@"+getModelObject().getDomain().getName());
		List<ExternalPlatformId> oauthPlatforms = new ArrayList<ExternalPlatformId>();
		if (getGoogleAuth()) oauthPlatforms.add(ExternalPlatformId.GOOGLE);
		if (getFacebookAuth()) oauthPlatforms.add(ExternalPlatformId.FACEBOOK);
		List<Role> roles = new ArrayList<Role>();
		if (getExternalRole()!=null) roles.add(getExternalRole());
		getModelObject().getService(PersonService.class).createUser(roles, oauthPlatforms);
	}
	
	protected void onCreate(AjaxRequestTarget target) {
		
	}
	
	private TextField<?> getPassword1Field() {
		return (TextField<?>) get("form:password1");
	}
	
	private TextField<?> getPassword2Field() {
		return (TextField<?>) get("form:password2");
	}
	
	private String getOrganization(DataSetMember member) {
		Object value = (new KbeeClassificableScriptWrapper(member)).getValue("organization");
		return  value!=null ?  (String)((KbeeClassificableScriptWrapper)value).getValue("nombre") : null;
	}
	
	private Role getExternalRole() {
		for (Role role : getContentSecurityDao().getRoles(getDomain())) {
			if ("external".equals(role.getAlias())) {
				return role;
			}
		}
		return null;
	}
}