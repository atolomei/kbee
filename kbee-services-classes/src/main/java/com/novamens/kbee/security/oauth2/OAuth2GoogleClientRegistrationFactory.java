package com.novamens.kbee.security.oauth2;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import com.novamens.util.PropertiesFactory;

public class OAuth2GoogleClientRegistrationFactory implements FactoryBean<ClientRegistration> {
	
	static String Google_Client_id = 
		PropertiesFactory
		.getInstance("kbee")
		.getProperties()
		.getProperty("oauth2.google.client-id","")
		.trim();

	static String Google_Client_Secret = 
		PropertiesFactory
		.getInstance("kbee")
		.getProperties()
		.getProperty("oauth2.google.client-secret","")
		.trim();
	
	public ClientRegistration getObject() throws Exception {
		return CommonOAuth2Provider.GOOGLE.getBuilder("google")
			.clientId(Google_Client_id)
			.clientSecret(Google_Client_Secret)
			.scope("email", "profile")
			.build();
	}

	public Class<ClientRegistration>  getObjectType() {
		return ClientRegistration.class;
	}
	
	public boolean isSingleton() {
		return true;
	}
}