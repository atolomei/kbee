package com.novamens.content.web.admin.markup.datamanagement;


import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.entity.Person;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.console.tools.GridExportMenuItem;

import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ApplicationPage;

public class DatabaseExportPage extends ApplicationPage<Person> {
			
	private static final long serialVersionUID = 1L;

	static long KB = 1024;
	static long MB = 1000 * KB;
	static long GB = 1000 * MB;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DatabaseExportPage.class.getName());
	static private Logger DBLogger = LogManager.getLogger("DBEventLogger");

	private IModel<User> user_model;
	boolean valid = false;

	/**
	 * 
	 */
	public DatabaseExportPage() {
		this(null);
	}
	
	public DatabaseExportPage(PageParameters parameters) {
	}

	
	/**
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		PageParameters parameters = getPageParameters();
		boolean err=true;
		StringBuilder str = new StringBuilder();
		if (parameters!=null) {
			 try {	
				 if (parameters.get("key")!=null) {
					 String token = parameters.get("key").toString();
					 if (ServiceLocator.getService(SecurityService.class).isValid(token)) {
						 String userid=ServiceLocator.getService(SecurityService.class).getUserId(token);
						 setAuthorizedUser(ServiceLocator.getService(SecurityService.class).findUserById(userid));
						 if (getSessionUser().getId().toString().equals(userid)) {
							 valid=true;	 
						 }
						 else {
							 str.append("The token belongs to a different User. ");
							 valid = false;
						 }
					 }
					 else {
						 str.append("The token is invalid. ");
						 valid = false;
					 }
				 }
				 else
					 str.append("Invalid URL. ");
				 
			 } catch (Exception e) {
				 valid = false;
				 str.append("err: "+e.getClass().getSimpleName()+". ");
			 }
		}
		else 
			str.append("Invalid URL. ");
		
		Person person = getUserProfile().getPerson();
		setModel(new ObjectModel<Person>(person));
		
		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());  


		
		if (this.valid) {
			
			String token = parameters.get("key").toString();
			String path = ServiceLocator.getService(SecurityService.class).getParameter(token);
			
			File zfile = new File(path);
			
			if ( zfile !=null && zfile.exists() &&  !zfile.isDirectory()) {
					DownloadLink lnk = new DownloadLink("download", zfile) {
						
						public void onClick() {
							logger.debug("onclick");
							super.onClick();
						}
					};
					
					add(lnk);
					Label filename = new Label("zip",  zfile.getName());
							
					String si=ServiceLocator.getService(DateTimeService.class).formatFileSize(zfile.length(), getSessionUser().getLocale(), "ago");
					Label filesize = new Label("size",si);
					filesize.setEscapeModelStrings(false);
					filename.setEscapeModelStrings(false);
					lnk.add(filename);
					add(filesize);
					add(lnk);
					err=false;
			}else {									
				str.append("err: The file was not created or removed from disk. ");
			}
		}
		
		if (err) 
			addErrorPanels(str.toString());
	}
	

	@Override
	public boolean hasPermissions() {
		return getSessionUser()!=null;
	}
	
		
	@Override
	public void onDetach() {
		super.onDetach();
		if (this.user_model!=null)
			this.user_model.detach();
	}
	
	@Override
	public String getPageHelpKey() {
		return "DatabaseExportPage";
	}
	
	
	private void setAuthorizedUser(User user) {
		this.user_model=new ObjectModel<User>(user);
	}
	
	private void addErrorPanels(String str) {
		WebMarkupContainer lc = new WebMarkupContainer("download");
		lc.add(new Label("zip", str));
		add( (new Label("size", "")).setVisible(false));
		add(lc);
	}

	

	


}
