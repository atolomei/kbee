package com.novamens.kbee.security.oauth2;



import java.util.Properties;



import org.springframework.beans.factory.FactoryBean;

import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;

import org.springframework.security.oauth2.client.registration.ClientRegistration;



import kbee.util.PropertiesFactory;



public class OAuth2GoogleClientRegistrationFactory implements FactoryBean<ClientRegistration> {

	static private final Properties props = PropertiesFactory.getInstance("kbee").getProperties();

	private static final String CLIENT_ID =  props.getProperty("google.client.id");

	private static final String CLIENT_SECRET= props.getProperty("google.client.secret");

	public ClientRegistration getObject() throws Exception {

		return CommonOAuth2Provider.GOOGLE.getBuilder("google")

				.clientId(CLIENT_ID)

				.clientSecret(CLIENT_SECRET)

				.scope("email",

				"profile")

				.build();

	}

	public Class<ClientRegistration>  getObjectType() {

		return ClientRegistration.class;

	}

	public boolean isSingleton() {

		return true;

	}

}
