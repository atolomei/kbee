package kbee.web.content.nav;

import org.apache.wicket.MarkupContainer;
import org.springframework.beans.factory.FactoryBean;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;

public class LateralMenuFactory implements FactoryBean<MarkupContainer>{
	
	public MarkupContainer getObject() {
		if (isKbeeDomain()) {
			return new MainLateralMenuFactoryV5("menu", "menu");
		}	
		else {
			return new MainLateralMenuContentV5("menu", "menu");
		}
	}
	
	public Class<MarkupContainer>  getObjectType() {
		return MarkupContainer.class;
	}
	
	public boolean isSingleton() {
		return false;
	}
	
	private boolean isKbeeDomain() {
		return Boolean.valueOf(getDomain().getName().toLowerCase().trim().equals("kbee"));
	}
	
	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}