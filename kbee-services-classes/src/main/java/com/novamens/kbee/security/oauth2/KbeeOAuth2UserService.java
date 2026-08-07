package com.novamens.kbee.security.oauth2;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.externalLogin.ExternalPlatformId;
import com.novamens.content.user.externalLogin.UserExternalLoginPlatform;
import com.novamens.content.user.externalLogin.UserExternalPlatformIdType;
import com.novamens.hibernate.session.Session;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KbeeOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
	
	static Logger logger = LogManager.getLogger(KbeeOAuth2UserService.class.getName());
	
	OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DefaultOAuth2UserService();


	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		
		try {
			OAuth2User oauth2user = internalLoad(userRequest);
			Session.open();


			//later a service to get this values from request could be made
			final ExternalPlatformId externalPlatformId = ExternalPlatformId.fromName(userRequest.getClientRegistration().getClientName());

			final UserExternalPlatformIdType userExternalPlatformIdType = UserExternalPlatformIdType.EMAIL;
			final String userPlatformId = getUserPlatformId(externalPlatformId, userExternalPlatformIdType, oauth2user);

			final List<UserExternalLoginPlatform> userExternalLoginPlatform = getContentDao().findUserExternalLoginPlatform(externalPlatformId.getId(), userExternalPlatformIdType.getId(), userPlatformId);

			if (userExternalLoginPlatform == null || userExternalLoginPlatform.size() == 0) {
				throw new OAuth2AuthenticationException(new OAuth2Error("no_user"));
			}else if(userExternalLoginPlatform.size() == 1){
				if ( userExternalLoginPlatform.size()==1) {
					UserProfile userprofile = userExternalLoginPlatform.get(0).getUserProfile();
					User user = userprofile.getUser();
					String password = user.getPassword();
					if(password==null){
						SecureRandom rnd = new SecureRandom();
						byte[] token = new byte[512];
						rnd.nextBytes(token);
						password=token.toString();
					}
					return new KbeeOAuth2User(user.getId(), user.getName(), password, user.isEnabled(), getAuthorities(user), oauth2user.getAttributes());
				}
			}

			final ArrayList<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority("EMAIL_USER"));
			final List<Long> userIds = userExternalLoginPlatform.stream().map(el -> (Long)el.getUserProfile().getUser().getId()).collect(Collectors.toList());
			KbeeMultiUser kbeeMultilUser =  new KbeeMultiUser(oauth2user.getName(), "",userIds );
			return kbeeMultilUser;

		}catch (OAuth2AuthenticationException e){
			logger.error(e);
			throw e;
		}
		catch (Exception e) {
			logger.error(e);
			String message = e.getMessage();
			if(message == null)
				message= OAuth2ErrorCodes.SERVER_ERROR;
			throw new OAuth2AuthenticationException(new OAuth2Error(message));
		}
		finally {
			Session.close();
		}
	}

	private String getUserPlatformId(ExternalPlatformId userExternalPlatformId, UserExternalPlatformIdType userExternalPlatformIdType, OAuth2User oauth2user){

		Object emailAttribute = oauth2user.getAttributes().get("email");
		return emailAttribute.toString();
	}

	public OAuth2User internalLoad(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		return oAuth2UserService.loadUser(userRequest);
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
