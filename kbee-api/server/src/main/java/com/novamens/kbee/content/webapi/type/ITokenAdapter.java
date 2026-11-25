package com.novamens.kbee.content.webapi.type;


import com.novamens.security.AuthToken;

import kbee.api.model.ApiToken;

public class ITokenAdapter implements Adapter<AuthToken, ApiToken> {
	
	
	public ITokenAdapter() {
	}
	
	public ApiToken adapt(AuthToken token) {
		
		ApiToken itoken = new ApiToken();
		
		itoken.setValue(token.getTokenValue());
		itoken.setLifeTime(token.getLifeTime());
		itoken.setDuration(token.getDuration());
		
		return itoken;	
	}
}
