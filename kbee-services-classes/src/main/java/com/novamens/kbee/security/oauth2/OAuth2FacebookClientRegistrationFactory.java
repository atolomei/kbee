package com.novamens.kbee.security.oauth2;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

public class OAuth2FacebookClientRegistrationFactory implements FactoryBean<ClientRegistration> {
	
	public ClientRegistration getObject() throws Exception {
		return CommonOAuth2Provider.FACEBOOK.getBuilder("facebook")
				.clientId("429946975301881")
				.clientSecret("5f93a4f9f92ccc688b3ca0ac23b234a9")
				.scope("public_profile")
				.scope("email")
				.build();
//		return CommonOAuth2Provider.FACEBOOK.getBuilder("facebook")
//				.clientId("222713956655636")
//				.clientSecret("bef915f3958ccda17b1808142806caea")
//				.scope("email")
//				.build();
//		return CommonOAuth2Provider.FACEBOOK.getBuilder("facebook")
//				.clientId("1177498869326010")
//				.clientSecret("b617e50c86dc27bbfd08508e512c3584")
//				.scope("public_profile")
//				.scope("email")
//				.build();

	}

	public Class<ClientRegistration>  getObjectType() {
		return ClientRegistration.class;
	}
	
	public boolean isSingleton() {
		return true;
	}
}