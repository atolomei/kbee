package com.novamens.kbee.content.webapi.type;


import com.novamens.security.AuthToken;

import kbee.api.model.IToken;

public class ITokenAdapter implements Adapter<AuthToken, IToken> {
	
	
	public ITokenAdapter() {
	}
	
	public IToken adapt(AuthToken token) {
		
		IToken itoken = new IToken();
		
		itoken.setValue(token.getTokenValue());
		itoken.setLifeTime(token.getLifeTime());
		itoken.setDuration(token.getDuration());
		
		return itoken;	
	}
}
