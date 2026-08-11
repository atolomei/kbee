package kbee.api.model;

import java.io.Serializable;

public class IValidator implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String condition;
	private String message;
	
	public IValidator(String condition, String message) {
		this.condition = condition;
		this.message = message;
	}
	
	public String getCondition() {
		return condition;
	}
	
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
}