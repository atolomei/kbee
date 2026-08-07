package com.novamens.kbee.security.oauth2;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class CustomAuthorizationRequestResolver
        implements OAuth2AuthorizationRequestResolver {

    static Logger logger = LogManager.getLogger(CustomAuthorizationRequestResolver.class.getName());
    private OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository repo, String authorizationRequestBaseUri) {
        defaultResolver = new KbeeOAuth2AuthorizationRequestResolver(repo, authorizationRequestBaseUri);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest oauthRequest = defaultResolver.resolve(request);
        if (oauthRequest != null) {
            oauthRequest = customizeAuthorizationRequest(oauthRequest,request);
        }
        return oauthRequest;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request, clientRegistrationId);
        if (req != null) {
            req = customizeAuthorizationRequest(req, request);
        }
        return req;
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
            OAuth2AuthorizationRequest oauthRequest, HttpServletRequest httpServletRequest) {
        Map<String, String> stateAttributes= new HashMap<>();

        final String operationKey="op";
        if(httpServletRequest.getParameterMap().containsKey(operationKey)) {
            stateAttributes.put(operationKey, httpServletRequest.getParameterMap().get(operationKey)[0]);
        }else{
            stateAttributes.put(operationKey, KbeeOauth2Operation.LOGIN.getName());
        }

        final OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest.from(oauthRequest);

        try {
            final String state = KbeeOauthStateKeyGen.encodeState(stateAttributes);
            builder.state(state);
        } catch (JsonProcessingException e) {
            logger.error(e);
        }

        return builder.build();
    }
}
