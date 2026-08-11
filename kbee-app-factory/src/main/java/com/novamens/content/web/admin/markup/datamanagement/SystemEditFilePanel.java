package com.novamens.content.web.admin.markup.datamanagement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.model.IModel;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.nav.CommandsBC;
import kbee.web.nav.DataManagementBC;
import kbee.web.nav.DomainsBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.SchedulerBC;
import kbee.web.nav.SeparatorBC;
import kbee.web.nav.ServiceManagementBC;
import kbee.web.nav.SystemInfoBC;

public class SystemEditFilePanel extends ModelPanel<Object> {
			
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	static private Logger logger = LogManager.getLogger(SystemEditFilePanel.class.getName());

	private IModel<Domain> domain_model;

	public SystemEditFilePanel(String id) {
		super(id);
		setOutputMarkupId(true);
		
	}
	
	
	public void onInitialize() {
		super.onInitialize();

		 MenuBreadCrumbPanel  bc =new MenuBreadCrumbPanel();
		 DropDownMenuBC dd = new DropDownMenuBC();
		 dd.addElement(new ServiceManagementBC(), true);
		 dd.addElement(new DataManagementBC());
		 dd.addElement(new SchedulerBC());
		 dd.addElement(new CommandsBC());
		 dd.addElement(new SeparatorBC());
		 dd.addElement(new DomainsBC());
		 dd.addElement(new SystemInfoBC());
		 
		 bc.addElement(dd);
		 bc.addElement(new DataManagementBC());
		 add(bc);

		 add(new InvisiblePanel("panel"));
	}
	
	@Override
	public void onDetach() {
		if (domain_model!=null)
			domain_model.detach();
		super.onDetach();
	}

	
	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}

	

//	private ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
//
//	
//
//	private Domain getDomain() {
//		if (domain_model==null)
//			domain_model= new ObjectModel<Domain>(ServiceLocator.getService(UserService.class).getDomain());
//		return domain_model.getObject();
//	}

}
