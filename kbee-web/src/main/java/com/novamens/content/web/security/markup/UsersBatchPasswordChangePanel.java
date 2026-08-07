package com.novamens.content.web.security.markup;


import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.ValidationError;

import com.novamens.dom.Domain;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.util.PropertiesFactory;
import kbee.web.form.EditButtonsV5;

public class UsersBatchPasswordChangePanel extends ObjectEditor<Domain> {

	static final String PASSWORD_COMPLEXITY_LEVEL = PropertiesFactory.getInstance("kbee").getProperties().getProperty("password.complexity.level", "production");  // test, production 
	static final boolean PASSWORD_LEVEL_TEST = PASSWORD_COMPLEXITY_LEVEL.toLowerCase().trim().equals("test"); 

	private static final long serialVersionUID = 1L;

	private static final int MINIMUM_LENGTH = 8;
	
	private String password= "";
	
	
	class PasswordsValidator implements IFormValidator {
		
		private static final long serialVersionUID = 1L;
		public String SPECIALS = "[!@#\\$%\\^-_]";
		
		public boolean hasNumber(String pwd) {
			if (PASSWORD_LEVEL_TEST)
				return true;
			return pwd.matches(".*[0-9].*");
		}
		
		public boolean hasSpecials(String pwd) {
			if (PASSWORD_LEVEL_TEST)
				return true;
			return pwd.matches(SPECIALS);
		}
		
		public boolean hasCapitalLetter(String pwd) {
			if (PASSWORD_LEVEL_TEST)
				return true;
			return pwd.matches(".*[A-Z].*");
		}
		
		@Override
		public void validate(org.apache.wicket.markup.html.form.Form<?> form) {
			
			String pwd1 = (String) getPasswordField().getInput().getDefaultModelObject();
			
			if (((String)getPasswordField().getInput().getDefaultModelObject()).length()<MINIMUM_LENGTH) {
				ValidationError error = new ValidationError();
				error.addKey("minimunlength");
				getPasswordField().setError(error);
				return;
			}
			
			else if (!hasNumber(pwd1)) {
				ValidationError error = new ValidationError();
				error.addKey("musthavedigit");
				getPasswordField().setError(error);
				return;
			}
			
			else if (!hasCapitalLetter(pwd1)) {
				ValidationError error = new ValidationError();
				error.addKey("musthavecapitalletter");
				getPasswordField().setError(error);
				return;
			}
		}
		@Override
		public FormComponent<?>[] getDependentFormComponents() {
			return new FormComponent<?>[0];
		}
	}

	
	public UsersBatchPasswordChangePanel(String id, IModel<Domain> model) {
		super(id, model);
		setOutputMarkupId(true);
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addForm();
	}
	
	
	@Override
	public Form<?> getForm() {
		return (Form<?>)get("form");
	}
	
	/** -----------------------------------------------------------------------------------------------------
	 */

	
	public String getPassword() {
		return password;
	}

	/** -----------------------------------------------------------------------------------------------------
	 */

	public void setPassword(String password) {
		this.password = password;
	}
	
	/** -----------------------------------------------------------------------------------------------------
	 */

	public TextField<?> getPasswordField() {
		return (TextField<?>)get("form:password");
	}
	
	
	/** -----------------------------------------------------------------------------------------------------
	 */

	
	/** -----------------------------------------------------------------------------------------------------
	 */
	
	protected void addForm() {
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new TextField<String>("password", new PropertyModel<String>(this, "password"), true));
		form.add(new PasswordsValidator());
		
		add(form);
		
		EditButtonsV5<Domain> buttons = new EditButtonsV5<Domain>(this, false) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled()  {
				return true;
			}
			protected String getCancelClass() {
				return "btn btn-default btn-sm";
			}
			protected String getSubmitClass() {
				return "btn btn-primary btn-sm";
			}
		};
	
		add(buttons);
		
		
		
	}
	

	
	/** --------------------------------------------------------------------------
	 * Session User is Root user
	 */
	//protected boolean isRoot() {
	//	return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(getSessionUser());
	//}

	
	
	
	
	
	
	
	
	
	
	

}
