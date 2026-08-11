package kbee.api.model;

import java.util.Map;

public class IFormTransaction extends ITransaction {
	private static final long serialVersionUID = 1L;
	
	private Map<String, String> errors;
	
	public IFormTransaction() {
	}

	public Map<String, String> getErrors() {
		return errors;
	}

	public void setErrors(Map<String, String> errors) {
		this.errors = errors;
	}
}
