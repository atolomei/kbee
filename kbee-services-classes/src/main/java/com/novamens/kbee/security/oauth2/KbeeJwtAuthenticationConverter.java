package com.novamens.kbee.security.oauth2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserProfile;
import com.novamens.dao.SecurityDao;
import com.novamens.hibernate.session.Session;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;

public class KbeeJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
	
	private static final String EMAIL_NOT_FOUND = "EMAIL_NOT_FOUND";
	private static final String USER_NOT_FOUND = "USER_NOT_FOUND";

	@Override
	public final AbstractAuthenticationToken convert(Jwt jwt) {
		return convert(jwt, null);
	}
	
	public final AbstractAuthenticationToken convert(Jwt jwt, Authentication authentication) {
		try {
			Session.open();

			String domain = null;
			if (authentication!=null && authentication.getDetails() instanceof KbeeAuthenticationDetails) {
				KbeeAuthenticationDetails details = (KbeeAuthenticationDetails)authentication.getDetails();
				domain = details.getDomain();
			}
			
			OAuth2AccessToken accessToken = new OAuth2AccessToken(
					OAuth2AccessToken.TokenType.BEARER, 
					jwt.getTokenValue(), 
					jwt.getIssuedAt(), 
					jwt.getExpiresAt());
			
			OAuth2User oauth2User = jwt.getClaim("email")==null
				? getUserByName(jwt, jwt.getClaim("principal"))
				: getUserByEmail(jwt, domain, jwt.getClaim("email"));		
			
			return new BearerTokenAuthentication(oauth2User, accessToken, oauth2User.getAuthorities());
		}
		finally {
			Session.close();
		}
	}
	
	protected OAuth2User getUserByEmail(Jwt jwt, String domain, String email) {
		List<UserProfile> profiles = getContentDao().findUserProfileByPersonEmail(email);
		if (profiles.isEmpty()) {
			throw new OAuth2AuthenticationException(new OAuth2Error(EMAIL_NOT_FOUND));
		}
		OAuth2User oauth2User;
		if (profiles.size()==1) {
			User user = profiles.get(0).getUser();
			String password = user.getPassword()!=null ? user.getPassword() : ""; 
			oauth2User = new KbeeOAuth2User(user.getId(), 
				user.getName(), 
				password, 
				user.isEnabled(), 
				getAuthorities(user), 
				jwt.getClaims());
		}
		else {
			UserProfile userprofile = null;
			if (domain!=null) {
				for (UserProfile profile :  profiles) {
					if (domain.equals(profile.getDomain().getName())) {
						userprofile = profile;
						break;
					}
				}
			}
			if (userprofile!=null) {
				User user = userprofile.getUser();
				String password = user.getPassword()!=null ? user.getPassword() : ""; 
				oauth2User = new KbeeOAuth2User(user.getId(), 
					user.getName(), 
					password, 
					user.isEnabled(), 
					getAuthorities(user), 
					jwt.getClaims());
			}
			else {
				ArrayList<GrantedAuthority> authorities = new ArrayList<>();
				authorities.add(new SimpleGrantedAuthority("email_user"));
				List<Long> userIds = profiles.stream().map(profile -> (Long)profile.getUser().getId()).collect(Collectors.toList());
				oauth2User =  new KbeeMultiUser(email, "", userIds);
			}			
		}
		return oauth2User;
	}
	
	protected OAuth2User getUserByName(Jwt jwt, String name) {
		User user = getSecurityDao().findUserByName(name);
		if (user==null) {
			throw new OAuth2AuthenticationException(new OAuth2Error(USER_NOT_FOUND));
		}
		String password = user.getPassword()!=null ? user.getPassword() : ""; 
		OAuth2User oauth2User = new KbeeOAuth2User(user.getId(), 
			user.getName(), 
			password, 
			user.isEnabled(), 
			getAuthorities(user), 
			jwt.getClaims());
		return oauth2User;
	}
	
	protected List<GrantedAuthority> getAuthorities(User user) {
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		for (Group group  : user.getGroups()) {
			authorities.add(new SimpleGrantedAuthority(group.getName()));
		}
		return authorities;
	}
	
	private SecurityDao getSecurityDao() {
		return	(SecurityDao)ServiceLocator.getService(BeansService.class).getBean("securityDao");
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
