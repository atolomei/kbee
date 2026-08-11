package kbee.api.model;

import java.io.Serializable;

public class ILoginResponse implements Serializable  {
	private static final long serialVersionUID = 1L;
	
	private ApiUser user;
	private IToken token;
	
	public ApiUser getUser() {
		return user;
	}
	
	public void setUser(ApiUser user) {
		this.user = user;
	}
	
	public IToken getToken() {
		return token;
	}
	
	public void setToken(IToken token) {
		this.token = token;
	}
}