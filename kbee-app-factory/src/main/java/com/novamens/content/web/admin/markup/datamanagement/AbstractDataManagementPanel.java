package com.novamens.content.web.admin.markup.datamanagement;

import java.util.List;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.metrics.domain.DomainMetricsService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.nav.DataManagementBC;
 
public class AbstractDataManagementPanel extends Panel {
	private static final long serialVersionUID = 1L;
	
	private IModel<Domain> domain_model;
	
	public AbstractDataManagementPanel(String id) {
		super(id);
	}
	
	public void onDetach() {
		super.onDetach();
		if (this.domain_model!=null)
			this.domain_model.detach();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		 MenuBreadCrumbPanel  bc =new MenuBreadCrumbPanel();
		 bc.addElement(new DataManagementBC());
		 
		 List <BCElement> li= getPageBreadCrumb();
		 
		 if (li==null)
			 bc.addElement(getPageBCElement());
		 else {
			 for (BCElement b: li) {
				 bc.addElement(b);
			 }
		 }
		 add(bc);
	}
	
	protected List<BCElement> getPageBreadCrumb() {
		return null;
	}

	protected BCElement getPageBCElement() {
		return new BCElement(new Model<String>(this.getClass().getSimpleName()));
	}

	protected DomainMetricsService getDomainMetricsServices() {
		return  ServiceLocator.getService(DomainMetricsService.class);
	}
	
	protected User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} catch (Exception e) {
			return null;
		}
	}
	
	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}
	
	protected Domain getDomain() {
		if (domain_model==null) 
			domain_model= new ObjectModel<Domain>(ServiceLocator.getService(UserService.class).getDomain());
		return domain_model.getObject();
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
