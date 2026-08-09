package kbee.web.security;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class UsernameValidator implements IValidator<String> {
				
	private static final long serialVersionUID = 1L;
	
	
	private boolean check_availability = true;
	
	public UsernameValidator() {
		
	}
	
	public UsernameValidator(boolean check_availiavility) {
		this.check_availability=check_availiavility;
	}
	
	@Override
	public void validate(final IValidatable<String> validatable) {
		validate(validatable, getDomain());
	}
	
	public void validate(final IValidatable<String> validatable, Domain domain) {
		
		String username = validatable.getValue();

		if (username==null || username.length()==0) {
			validatable.error(new ValidationError(this, "characters"));
			return;
		}else {
			username=username.replaceAll("\\s", "");
		}
		
		if (!username.matches("[a-z|0-9]+")) {
			validatable.error(new ValidationError(this, "characters"));
			return;
		}
		
		if (!ServiceLocator.getService(SecurityService.class).validateName(username)) {
			validatable.error(new ValidationError(this, "reservedname"));
		}

		if (check_availability) {
			String fullname = username+ "@" + domain.getName();
			User user =	ServiceLocator.getService(SecurityService.class).findUserByUsername(fullname);
			if (user!=null) 
				validatable.error(new ValidationError(this, "uniqueness"));
		} 
	}
	
	/** -----------------------------------------------------------------------------------------------------
	 */
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

}
