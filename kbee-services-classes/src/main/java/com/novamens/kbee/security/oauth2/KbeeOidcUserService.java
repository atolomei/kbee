package com.novamens.kbee.security.oauth2;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserProfile;
import com.novamens.hibernate.session.Session;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.ArrayList;
import java.util.List;

public class KbeeOidcUserService
implements OAuth2UserService<OidcUserRequest, OidcUser> {

private static final Logger logger =
    LogManager.getLogger(KbeeOidcUserService.class);

private final OidcUserService oidcUserService =
    new OidcUserService();

	@Override
	public OidcUser loadUser(OidcUserRequest userRequest)
	    throws OAuth2AuthenticationException {
	
		OidcUser oidcUser =
		        oidcUserService.loadUser(userRequest);
		
		try {
		    Session.open();
		
//		    ExternalPlatformId externalPlatformId =
//		            ExternalPlatformId.fromName(
//		                    userRequest
//		                        .getClientRegistration()
//		                        .getClientName()
//		            );
		
		    String email = getEmail(oidcUser);
		    
		    if (email!=null) {
		    	email = email.toLowerCase();
		    }
		    
		    logger.debug(
		            "Azure login email=[{}]",
		            email
		    );
		    
		    List<UserProfile> profiles = getContentDao()
		    	.findUserProfileByPersonEmail(email);
		
		    if (profiles.size() == 1) {
		
		        User user = profiles.get(0)
		        	.getUser();
		
		        return new KbeeOidcUser(
		                user,
		                oidcUser,
		                getAuthorities(user)
		        );
		    }
		    else {
			    if (profiles.isEmpty()) {
				    throw new OAuth2AuthenticationException(
				            new OAuth2Error("no user")
				    );
			    }
			    else {
				    throw new OAuth2AuthenticationException(
				            new OAuth2Error("multiple_users")
				    );
			    }
		    	
		    }
		
		} finally {
		    Session.close();
		}
	}
	
	private String getEmail(OidcUser oidcUser) {

	    String email = oidcUser.getEmail();

	    if (email != null && !email.isBlank()) {
	        return email;
	    }

	    Object preferredUsername =
	            oidcUser.getClaims().get("preferred_username");

	    if (preferredUsername != null) {
	        return preferredUsername.toString();
	    }

	    throw new OAuth2AuthenticationException(
	            new OAuth2Error("email_not_found")
	    );
	}
	
	
	
	protected List<GrantedAuthority> getAuthorities(User user) {
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		for (Group group  : user.getGroups()) {
			authorities.add(new SimpleGrantedAuthority(group.getName()));
		}
		return authorities;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
