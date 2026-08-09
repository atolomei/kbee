package kbee.web.util;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.security.authentication.ProviderManager;

import com.novamens.beans.BeansService;
import com.novamens.service.ServiceLocator;

public class AuthenticationManagerFactory implements FactoryBean<ProviderManager> {
	
	public ProviderManager getObject() throws Exception {
		return (ProviderManager)ServiceLocator.getService(BeansService.class).getBean("com.novamens.security.service.AuthenticationManager");
	}

	public Class<ProviderManager> getObjectType() {
		return ProviderManager.class;
	}
	
	public boolean isSingleton() {
		return true;
	}
}
