//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.novamens.kbee.security.oauth2;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.novamens.content.user.UserSelfService;
import com.novamens.content.user.externalLogin.ExternalPlatformId;
import com.novamens.content.user.externalLogin.UserExternalPlatformIdType;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.*;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;

public class KbeeOauth2LinkAccountFilter extends OncePerRequestFilter {
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2AuthorizedClientRepository authorizedClientRepository;
    private final AuthenticationManager authenticationManager;
    private AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository = new HttpSessionOAuth2AuthorizationRequestRepository();
    private final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final RequestCache requestCache = new HttpSessionRequestCache();

    public KbeeOauth2LinkAccountFilter(ClientRegistrationRepository clientRegistrationRepository, OAuth2AuthorizedClientRepository authorizedClientRepository, AuthenticationManager authenticationManager) {
        Assert.notNull(clientRegistrationRepository, "clientRegistrationRepository cannot be null");
        Assert.notNull(authorizedClientRepository, "authorizedClientRepository cannot be null");
        Assert.notNull(authenticationManager, "authenticationManager cannot be null");
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.authorizedClientRepository = authorizedClientRepository;
        this.authenticationManager = authenticationManager;
    }



    public final void setAuthorizationRequestRepository(AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository) {
        Assert.notNull(authorizationRequestRepository, "authorizationRequestRepository cannot be null");
        this.authorizationRequestRepository = authorizationRequestRepository;
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (this.matchesAuthorizationResponse(request)) {
                this.processAuthorizationResponse(request, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean matchesAuthorizationResponse(HttpServletRequest request) {
        MultiValueMap<String, String> params = KbeeOAuth2AuthorizationResponseUtils.toMultiMap(request.getParameterMap());
        if (!KbeeOAuth2AuthorizationResponseUtils.isAuthorizationResponse(params)) {
            return false;
        } else {
            OAuth2AuthorizationRequest authorizationRequest = this.authorizationRequestRepository.loadAuthorizationRequest(request);
            if (authorizationRequest == null) {
                return false;
            } else {
                UriComponents requestUri = UriComponentsBuilder.fromUriString(UrlUtils.buildFullRequestUrl(request)).build();
                UriComponents redirectUri = UriComponentsBuilder.fromUriString(authorizationRequest.getRedirectUri()).build();
                Set<Map.Entry<String, List<String>>> requestUriParameters = new LinkedHashSet(requestUri.getQueryParams().entrySet());
                Set<Map.Entry<String, List<String>>> redirectUriParameters = new LinkedHashSet(redirectUri.getQueryParams().entrySet());
                requestUriParameters.retainAll(redirectUriParameters);
                return Objects.equals(requestUri.getScheme(), redirectUri.getScheme()) && Objects.equals(requestUri.getUserInfo(), redirectUri.getUserInfo()) && Objects.equals(requestUri.getHost(), redirectUri.getHost()) && Objects.equals(requestUri.getPort(), redirectUri.getPort()) && Objects.equals(requestUri.getPath(), redirectUri.getPath()) && Objects.equals(requestUriParameters.toString(), redirectUriParameters.toString());
            }
        }
    }

    private void processAuthorizationResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        OAuth2AuthorizationRequest authorizationRequest = this.authorizationRequestRepository.removeAuthorizationRequest(request, response);
        String registrationId = (String) authorizationRequest.getAttribute("registration_id");
        ClientRegistration clientRegistration = this.clientRegistrationRepository.findByRegistrationId(registrationId);
        MultiValueMap<String, String> params = KbeeOAuth2AuthorizationResponseUtils.toMultiMap(request.getParameterMap());
        String redirectUri = UrlUtils.buildFullRequestUrl(request);
        OAuth2AuthorizationResponse authorizationResponse = KbeeOAuth2AuthorizationResponseUtils.convert(params, redirectUri);
        OAuth2AuthorizationCodeAuthenticationToken authenticationRequest = new OAuth2AuthorizationCodeAuthenticationToken(clientRegistration, new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse));
        authenticationRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));

        OAuth2AuthorizationCodeAuthenticationToken authenticationResult;
        try {
            authenticationResult = (OAuth2AuthorizationCodeAuthenticationToken) this.authenticationManager.authenticate(authenticationRequest);
        } catch (OAuth2AuthorizationException var16) {
            OAuth2Error error = var16.getError();
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(authorizationRequest.getRedirectUri()).queryParam("error", new Object[]{error.getErrorCode()});
            if (!StringUtils.isEmpty(error.getDescription())) {
                uriBuilder.queryParam("error_description", new Object[]{error.getDescription()});
            }

            if (!StringUtils.isEmpty(error.getUri())) {
                uriBuilder.queryParam("error_uri", new Object[]{error.getUri()});
            }

            this.redirectStrategy.sendRedirect(request, response, uriBuilder.build().encode().toString());
            return;
        }



        final OAuth2UserRequest oAuth2UserRequest1 = new OAuth2UserRequest(authenticationResult.getClientRegistration(), authenticationResult.getAccessToken(), authenticationResult.getAdditionalParameters());
        tryAddLoginPlatformFor(oAuth2UserRequest1);
        String redirectUrl = authorizationRequest.getRedirectUri();
        this.redirectStrategy.sendRedirect(request, response, "/myaccount?tab=externalLogin");
//        Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
//        String principalName = currentAuthentication != null ? currentAuthentication.getName() : "anonymousUser";
//        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(authenticationResult.getClientRegistration(), principalName, authenticationResult.getAccessToken(), authenticationResult.getRefreshToken());
//        this.authorizedClientRepository.saveAuthorizedClient(authorizedClient, currentAuthentication, request, response);
//        String redirectUrl = authorizationRequest.getRedirectUri();
//        SavedRequest savedRequest = this.requestCache.getRequest(request, response);
//        if (savedRequest != null) {
//            redirectUrl = savedRequest.getRedirectUrl();
//            this.requestCache.removeRequest(request, response);
//        }
//
//        this.redirectStrategy.sendRedirect(request, response, redirectUrl);
    }


    OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DefaultOAuth2UserService();

    private void tryAddLoginPlatformFor(OAuth2UserRequest userRequest) {

        Transaction transaction = null;
        try {
            transaction = ServiceLocator.getService(TransactionService.class).beginTransaction(true);


            final SecurityService securityService = ServiceLocator.getService(SecurityService.class);
            final KbeeUser sessionUser = (KbeeUser) securityService.getSessionUser();
            final UserSelfService userSelfService = sessionUser.getService(UserSelfService.class);

            //later a service to get this values from request could be made
            ExternalPlatformId externalPlatformId = ExternalPlatformId.fromName(userRequest.getClientRegistration().getClientName());
            UserExternalPlatformIdType userExternalPlatformIdType = UserExternalPlatformIdType.EMAIL;

            OAuth2User oauth2user = oAuth2UserService.loadUser(userRequest);
            String userIdInPlatform = getUserPlatformId(externalPlatformId, userExternalPlatformIdType, oauth2user);


            userSelfService.addLinkLoginPlatform(externalPlatformId, userExternalPlatformIdType, userIdInPlatform);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
        }
    }

    private String getUserPlatformId(ExternalPlatformId userExternalPlatformId, UserExternalPlatformIdType userExternalPlatformIdType, OAuth2User oauth2user) {
        Object emailAttribute = oauth2user.getAttributes().get("email");
        return emailAttribute.toString();
    }

}
