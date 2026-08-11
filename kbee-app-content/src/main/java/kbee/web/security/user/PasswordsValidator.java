package kbee.web.security.user;

import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.wicket.markup.html.form.TextField;

public abstract class PasswordsValidator implements IFormValidator {
	private static final long serialVersionUID = 1L;
	
	private static final int MINIMUM_LENGTH = 8;
	public String SPECIALS = "[!@#\\$%\\^-_]";
	
	@Override
	public void validate(org.apache.wicket.markup.html.form.Form<?> form) {
		String pwd1 = (String) getPassword1Field().getInput().getDefaultModelObject();
		if (((String) getPassword1Field().getInput().getDefaultModelObject()) == null
			&& ((String) getPassword2Field().getInput().getDefaultModelObject()) == null) {
			return;
		}
		else if (((String) getPassword1Field().getInput().getDefaultModelObject()) == null
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
	public FormComponent<?>[] getDependentFormComponents() {
		return new FormComponent<?>[0];
	}
	
	protected abstract TextField<?> getPassword1Field();
	
	protected abstract TextField<?> getPassword2Field(); 
}