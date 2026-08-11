package kbee.web.security.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.IFormSubmitter;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.entity.Person;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EValidatable;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.PersonMember;
import com.novamens.content.model.UserSet;
import com.novamens.content.security.Role;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.externalLogin.ExternalPlatformId;
import com.novamens.kbee.content.form.KbeeEMemMemberData;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;
import kbee.web.eform.EFormDataModel;
import kbee.web.eform.EFormEditor;
import kbee.web.eform.FieldMessage;
import kbee.web.eform.KbeeUserForm;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class NewUserFormEditor extends ObjectEditor<NewUserData> {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(NewUserEditor.class.getName());
	
	private IModel<EFormData> datamodel;
	private IModel<Person> usermodel;
	private boolean errors = false;
	
	public class KbeeEValidatable implements EValidatable {
		EForm form;
		EFormField<?> field;
		public KbeeEValidatable(EForm form, EFormField<?> field) {
			this.form = form;
			this.field = field;
		}
		public Object getValue() {
			return getData().getData(getField());
		}
		public EFormField<?> getField() {
			return field;
		}
		public EFormData getData() {
			return datamodel.getObject();
		}
		public void error(String key) {
			error(key, getField().getLabel());
		}
		public void error(String key, String... parameter) {
			String message = getLabelString(key, parameter);
			setError(getField(), message);
			NewUserFormEditor.this.error(new FieldMessage(NewUserFormEditor.this, getEForm(), getField(), message, FeedbackMessage.ERROR));
			errors = true;
		}
	}
	
	public NewUserFormEditor(String id, IModel<NewUserData> model) {
		super(id, model);
		
		setOutputMarkupId(true);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL) {
			public void process(IFormSubmitter submittingComponent) {
				NewUserFormEditor.this.validate();
				if (!hasErrors()) {
					super.process(submittingComponent);
				}
				getRequestCycle().find(AjaxRequestTarget.class).ifPresent(target -> {
					target.add(NewUserFormEditor.this);
				});
			}
		};
		
		datamodel = getDataModel();
		form.add(new EFormEditor("eform", datamodel) {
			@Override
			public Classificable getObject() {
				return getUserSet().createMember();
			}
		});
		
		add(form);
		
		add(new EditButtonsV5<NewUserData>(this) {
			@Override
			public boolean isVisible() {
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
				setResponsePage(new NewUserPage(new Model<NewUserData>(new NewUserData()), null));
			}
		});
		
		feedbackcontainer.add(new Link<Void>("edit-link") {
			public void onClick() {
				onEdit(getUserModel());
			}
		});
		
		add(feedbackcontainer);
	}
	
	public void onEdit(IModel<Person> model) {
		
	}
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedFields().isEmpty()) {
				createUser();
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}	
	
	public void onCancel(AjaxRequestTarget target) {
	}
	
	public void validate() {
		errors = false;
		EFormEditor editor = getFormEditor();
		editor.clearMessages();
		for (EFormField<?> field : editor.getForm().getFields()) {
			field.validate(new KbeeEValidatable(editor.getForm(), field)); 
		}
	}
	
	public boolean hasErrors() {
		return errors;
	}
	
	private IModel<EFormData> getDataModel() {
		PersonMember member =  (PersonMember)getUserSet().createMember();
		EForm form = getForm(member);
		EFormData data = new KbeeEMemMemberData(form, member);
		IModel<EFormData> model = new EFormDataModel(data);
		return model;
	}
	
	private EFormData getData() {
		return datamodel.getObject();
	}
	
	public void setError(EFormField<?> field, Serializable message) {
		onInitialize();
		getFormEditor().setError(field, message);
		getFormEditor().setFocus(field);
	}
	
	private void createUser() {
		
		String firstName= (String)getData().getObject("firstName");
		String lastName= (String)getData().getObject("lastName");
		String email = (String)getData().getObject("email");
		
		Role role = (Role)getData().getObject("role");
		List<Role> roles = new ArrayList<Role>();
		if (role!=null) roles.add(role);
		
		Map<ModelElement, List<Object>> classification = new HashMap<ModelElement, List<Object>>();
		for (ModelElementTemplate template : getUserSet().getStructure()) {
			Object value = getData().getObject(template.getElement().getAlias());
			if (value!=null) {
				List<Object> values = new ArrayList<Object>();
				values.add(value);
				classification.put(template.getElement(), values);
			}
		}
		
		List<ExternalPlatformId> platforms = new ArrayList<ExternalPlatformId>();
		Boolean google = (Boolean)getData().getObject(ExternalPlatformId.GOOGLE.name());
		if (google) platforms.add(ExternalPlatformId.GOOGLE);
		
		Person user = (Person) ServiceLocator.getService(ObjectFactoryService.class).createUser(firstName,
					lastName,
					email,
					platforms,
					roles,
					classification);
		
		setUser(user);
		
 		//// System.out.println(firstName);
		//// System.out.println(lastName);
		//// System.out.println(email);
	}
	
	private IModel<String> getNewUserInfo() {
		StringBuilder str = new StringBuilder();
		if (getUser()!=null) {
			str.append("<br /><b>Name</b>: " + getUser().getFirstLastName());
			str.append("<br /><b>Username</b>: " + getUser().getProfile(UserProfile.class).getUser().getUserName());
//			if (getDomain().getDefaultPassword()!=null) {
//				str.append("<br /><b>Password</b>: " + getDomain().getDefaultPassword());
//			}
			str.append("<br /><b>Email</b>: " + getUser().getEmail());
			str.append("<br /><b>Start page</b>: " + getUser().getProfile(UserProfile.class).getStartPage());
			str.append("<br /><b>Language</b>: " + getUser().getProfile(UserProfile.class).getUser().getLocale().getLanguage());
			str.append("<br /><b>Timezone</b>: " + getUser().getProfile(UserProfile.class).getUser().getTimeZone());
			str.append("<br /><b>Roles</b>: ");
			int n= 0;
			for (UserRole up: getUser().getProfile(UserProfile.class).getRoles()) {
				if (n++>0)	str.append(" | ");	
				String value = up.getDisplayName();
				str.append(value);
			}
		}	
		
		return new Model<String>(str.toString());
	}
	
	private void setUser(Person user) {
		this.usermodel = new ObjectModel<Person>(user);
	}
	
	private IModel<Person> getUserModel() {
		return usermodel;
	}
	
	private Person getUser() {
		return usermodel!=null ? usermodel.getObject() : null;
	}

	private EForm getEForm() {
		return getFormEditor().getForm();
	}
	
	private EFormEditor getFormEditor() {
		return (EFormEditor)get("form:eform");
	}
	
	private EForm getForm(PersonMember member) {
		return new KbeeUserForm(member);
	}
	
	private UserSet getUserSet() {
		return getContentDao().getUserSet();
	}
}