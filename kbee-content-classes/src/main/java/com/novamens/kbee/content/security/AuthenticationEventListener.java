package com.novamens.kbee.content.security;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserService;
import com.novamens.hibernate.session.Session;
import com.novamens.kbee.metrics.KbeeSystemMetricsService;
import com.novamens.kbee.security.KbeeWebAuthenticationDetails;
import com.novamens.logging.LoginEvent;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

/**
 * 
 * IMPORTANT. DBEventLogger must be "INFO" in log4j.xml
 *
 */
public class AuthenticationEventListener implements ApplicationListener<AbstractAuthenticationEvent> {
	
	static private Logger DBLogger = LogManager.getLogger("DBEventLogger");
	static private Logger logger = LogManager.getLogger(AuthenticationEventListener.class.getName());

	/**
	 * en {@link KbeeUserDetail} se agrega un boolean que indica si es por su.
	 */
	@Override
	public void onApplicationEvent(AbstractAuthenticationEvent event) {
		
		boolean opensession = Session.get()==null;
		
		if (event instanceof AuthenticationSuccessEvent) {
			try {
				if (isWebSession(event)) {
					
					if (opensession) 
						Session.open();
					
					HttpServletRequest request = ((KbeeWebAuthenticationDetails)((AbstractAuthenticationToken)event.getSource()).getDetails()).getRequest(); 
				
					User user = ServiceLocator.getService(SecurityService.class).findUserByUsername(event.getAuthentication().getName());

					ServiceLocator.getService(UserService.class).onLogin(user);
					
					logger.debug("Login " + user.getDisplayName());
					DBLogger.info(new LoginEvent(user, request));
					
					getMetricsServices().mark("login", getContentDao().findUserProfileByUser(user).getDomain().getId());
					
					if (!ServiceLocator.getService(SecurityService.class).isActive(user)) {
						ServiceLocator.getService(SecurityService.class).setActive(user);
					}
					
					getMetricsServices().inc("users_logged", getContentDao().findUserProfileByUser(user).getDomain().getId());
				}
				else {
					opensession = false;
				}
			}
			finally {
				if (opensession) Session.close();
			}
		} 
		else {
			
			logger.debug(event.getClass().getSimpleName() + "  -> "  + ( (event instanceof AuthenticationFailureBadCredentialsEvent) ? 
					((AuthenticationFailureBadCredentialsEvent) event).getException().getMessage() 
					: "" ));
			
		}
	}
	
	private boolean isWebSession(AbstractAuthenticationEvent event) {
		if (event.getSource() instanceof AbstractAuthenticationToken) {
			if (((AbstractAuthenticationToken)event.getSource()).getDetails() instanceof KbeeWebAuthenticationDetails) {
				return true;
			}
		}
		return false;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	private KbeeSystemMetricsService getMetricsServices() {
		return ServiceLocator.getService(KbeeSystemMetricsService.class);
	}
}