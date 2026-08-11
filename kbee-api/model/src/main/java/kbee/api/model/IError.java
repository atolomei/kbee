package kbee.api.model;

import kbee.api.service.ApiError;

public class IError {
	
	private String code;
	private String message;
	
	public IError() {
		
	}
	
	public IError(ApiError error, String message) {
		setCode(String.valueOf(error.getCode()));
		setMessage(message);
	}
	
	public IError(String code, String message) {
		setCode(code);
		setMessage(message);
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
}
