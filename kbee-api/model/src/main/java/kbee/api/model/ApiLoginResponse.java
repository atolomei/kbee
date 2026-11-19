package kbee.api.model;

import java.io.Serializable;

public class ApiLoginResponse implements Serializable  {
	private static final long serialVersionUID = 1L;
	
	private ApiUser user;
	private ApiToken token;
	
	public ApiUser getUser() {
		return user;
	}
	
	public void setUser(ApiUser user) {
		this.user = user;
	}
	
	public ApiToken getToken() {
		return token;
	}
	
	public void setToken(ApiToken token) {
		this.token = token;
	}
}