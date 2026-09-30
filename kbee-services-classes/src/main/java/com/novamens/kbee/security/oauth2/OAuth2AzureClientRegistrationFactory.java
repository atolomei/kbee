package com.novamens.kbee.security.oauth2;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import com.novamens.util.PropertiesFactory;

public class OAuth2AzureClientRegistrationFactory
        implements FactoryBean<ClientRegistration> {

    private static final String CLIENT_ID =
            PropertiesFactory
                    .getInstance("kbee")
                    .getProperties()
                    .getProperty("oauth2.azure.client-id", "")
                    .trim();

    private static final String CLIENT_SECRET =
            PropertiesFactory
                    .getInstance("kbee")
                    .getProperties()
                    .getProperty("oauth2.azure.client-secret", "")
                    .trim();

    private static final String TENANT_ID =
            PropertiesFactory
                    .getInstance("kbee")
                    .getProperties()
                    .getProperty("oauth2.azure.tenant-id", "")
                    .trim();

    @Override
    public ClientRegistration getObject() {

        String base =
                "https://login.microsoftonline.com/" + TENANT_ID;

        return ClientRegistration
                .withRegistrationId("azure")
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .clientAuthenticationMethod(
                        ClientAuthenticationMethod.CLIENT_SECRET_BASIC
                )
                .authorizationGrantType(
                        AuthorizationGrantType.AUTHORIZATION_CODE
                )
                .redirectUri(
                        "{baseUrl}/login/oauth2/code/{registrationId}"
                )
                .scope(
                        "openid"
//                        "profile",
//                        "email"
                )
                .authorizationUri(
                        base + "/oauth2/v2.0/authorize"
                )
                .tokenUri(
                        base + "/oauth2/v2.0/token"
                )
                .jwkSetUri(
                        base + "/discovery/v2.0/keys"
                )
                .userNameAttributeName("sub")
                .clientName("Microsoft Azure")
                .build();
    }

    @Override
    public Class<ClientRegistration> getObjectType() {
        return ClientRegistration.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}