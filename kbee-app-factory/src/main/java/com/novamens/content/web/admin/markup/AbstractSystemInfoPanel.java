package com.novamens.content.web.admin.markup;


import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.metrics.domain.DomainMetricsService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.nav.SystemInfoBC;

public class AbstractSystemInfoPanel extends KBPanel {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AbstractSystemInfoPanel.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	static public final double GB = 1000000000.0;

	
	public AbstractSystemInfoPanel(String id) {
		super(id);
	}
	
	public void onDetach() {
		super.onDetach();
		if (this.domain_model!=null)
			this.domain_model.detach();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		 // add(getMenuBreadCrumbPanel());
	}

	/**
	
	protected MenuBreadCrumbPanel getMenuBreadCrumbPanel() {
		MenuBreadCrumbPanel  bc =new MenuBreadCrumbPanel();
		 bc.addElement(new SystemInfoBC());
		 bc.addElement(getPageBCElement());
		 return bc;
	}**/

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
	
	private IModel<Domain> domain_model;
	
	protected Domain getDomain() {
		if (domain_model==null) 
			domain_model= new ObjectModel<Domain>(ServiceLocator.getService(UserService.class).getDomain());
		return domain_model.getObject();
	}
	
	protected boolean isKbeeDomain() {
		return getDomain().getName().equals("kbee");
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	protected boolean isLinux() {
		if  (System.getenv("OS")!=null && System.getenv("OS").toLowerCase().contains("windows")) 
			return false;
		return true;
	}
	
	
	protected String getServerHost() {
		
		StringBuilder output = new StringBuilder();
		
		try {

			ProcessBuilder processBuilder = new ProcessBuilder();

			if (isLinux())
				processBuilder.command("bash", "-c", "hostname");
			else
				processBuilder.command("cmd.exe", "/c", "hostname");
			
			Process process = processBuilder.start();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line;
			
			while ((line = reader.readLine()) != null) {
				output.append(line);
			}

			int exitVal = process.waitFor();
			if (exitVal == 0) {
				logger.debug(output.toString());
			} else {
				logger.debug(String.valueOf(exitVal));
			}

		} catch (Exception e) {
			return e.getClass().getName();
		}
		
		return output.toString();
		
}


}
