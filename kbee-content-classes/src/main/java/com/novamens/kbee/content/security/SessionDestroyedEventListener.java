
package com.novamens.kbee.content.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.session.SessionDestroyedEvent;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.hibernate.session.Session;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.service.ServiceLocator;


/**
 * TODO. No esta llegando hasta acá
 */
public class SessionDestroyedEventListener implements ApplicationListener<SessionDestroyedEvent> {
	
	Logger logger = LogManager.getLogger(getClass().getName());

	@Override
	public void onApplicationEvent(SessionDestroyedEvent event) {
		try {
			
			Session.open();
			logger.info("Session destroyed: LOG OUT");
			Domain domain = ServiceLocator.getService(UserService.class).getDomain();
			if (domain!=null) 
				getMetricsServices().dec("users_logged", domain.getId().toString());
			
		}
		finally {
			Session.close();
		}
	}
	
	private SystemMetricsService getMetricsServices() {
		return ServiceLocator.getService(SystemMetricsService.class);
	}

}