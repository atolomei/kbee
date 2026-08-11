package kbee.api.service;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	private String errorcode;
	private HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
	private boolean auditable = true;
	
	public ApiException(HttpStatus httpStatus, ApiError error, String... parameter) {
		super(buildMessage(error.getMessage(), parameter));
		this.httpStatus = httpStatus;
		this.errorcode = String.valueOf(error.getCode());
	}
	
	public ApiException(HttpStatus httpStatus, ApiError error) {
		super(error.getMessage());
		this.errorcode = String.valueOf(error.getCode());
		this.httpStatus = httpStatus;
	}
	
	public ApiException(HttpStatus httpStatus, String errorcode, String message) {
		super(message);
		this.errorcode = errorcode;
		this.httpStatus = httpStatus;
	}
	
	public ApiException(HttpStatus httpStatus, String message) {
		super(message);
		this.httpStatus = httpStatus;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
	
	public String getErrorCode() {
		return errorcode;
	}
	
	public boolean isAuditable() {
		return auditable;
	}
	
	@Override
	public String getMessage() {
		return super.getMessage() + (errorcode!=null? (". errorCode: "+ errorcode):"");
	}
	
	public void setAuditable(boolean value) {
		this.auditable = value;
	}
	
	public static String buildMessage(String template, String... parameter) {
		String message = template;
		for (int p=0; p<parameter.length; p++) {
			message = template.replace("%"+String.valueOf(p+1), parameter[p]!=null ? parameter[p] : "null");
			template = message;
		}
		return message;
	}
}