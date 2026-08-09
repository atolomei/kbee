package kbee.web.portal6;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

public class SiteUrlValidator implements IValidator<String> {

	private static final long serialVersionUID = 1L;

	static Map<String, String> reserved = new HashMap<String, String>();
	static {
		reserved.put("portal", "portal");
		reserved.put("siteindex", "siteindex");
		reserved.put("indice", "indice");
		reserved.put("sysadmin", "sysadmin");
	};

	@Override
	public void validate(IValidatable<String> validatable) {

		String url = validatable.getValue();

		if (url == null || url.length() == 0) {
			validatable.error(new ValidationError(this, "characters"));
			return;
		}

		if (!url.matches("[a-z|0-9]+")) {
			validatable.error(new ValidationError(this, "characters"));
			return;
		}

		if (reserved.containsKey(url)) {
			validatable.error(new ValidationError(this, "reserved-url"));
			return;
		}
	}
}
