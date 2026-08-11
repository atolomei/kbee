package com.novamens.content.web.integration;

import java.io.File;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.web.console.markup.ErrorPanel;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.Query;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.system.parameters.SystemParameterService;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.nav.DropDownMenuBC;

import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;

public class FileUploadPage extends ApplicationPage<Domain> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(FileUploadPage.class.getName());
	
	private static final long serialVersionUID = 1L;
	private String folder;
	private String bpath;

	Query query;
	
	private String drive_dir_name;
	private String home_dir_name;
	
	File home_dir = new File(getDriveDir()+File.separator+getHomeDir());
	
	boolean admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	public FileUploadPage( PageParameters param) {
		
		if (getDomain()!=null) { 
			setModel(new ObjectModel<Domain>(getDomain()));
			this.query=new LocalFSQuery(home_dir);
			this.folder = ((LocalFSQuery) this.query).getDirectory().getAbsolutePath();
			String r_path=				
					((LocalFSQuery) this.query).getRootDir().getParentFile() != null ?
					((LocalFSQuery) this.query).getRootDir().getParentFile().getAbsolutePath() :
					((LocalFSQuery) this.query).getRootDir().getAbsolutePath();
			this.bpath=!isDomainKbee()?folder.replace(r_path,  ""):folder;
		}
	}
	
	
	public FileUploadPage(IModel<Domain> model, Query query, String folder, String bpath) {
		super(model);
		
		this.folder=folder;
		this.bpath=bpath;
		this.query=query;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		setPageTitle(new StringResourceModel("upload-file", FileUploadPage.this, null));
		setPageDescription(getPageTitle());
		
		if (hasPermissions()) {
			
			setTopNavigation(getMainTopbar());       // setNavigation(new GlobalNavigationBar<Person>("navigation"));
			setMenu(getMainLaternalMenu());       // setMenu(new NavBarLateralMenu("menu", getApplicationMenuSection().getKey()));
	
			MenuBreadCrumbPanel bc =new MenuBreadCrumbPanel();
			 DropDownMenuBC dd = new DropDownMenuBC();
			 dd.addElement(new BCElement(new Model<String>("Integration")), true);
			 dd.addElement(new FileServerBC());
			 bc.addElement(dd);
			 bc.addElement(new BCElement(new Model<String>("Upload")));
			add(bc);
			
			add (new FSUploadPanel("upload-panel", query, folder, bpath));
		}
		else {
			
			add (new ErrorPanel("upload-panel", new Model<String>("Not Authorized"), new Model<String>("User must be Admin")));
		}
	}
	
	@Override
	public String getPageHelpKey() {
		return super.getPageHelpKey()+"-"+this.getClass().getSimpleName();
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.DATA_MANAGEMENT;
	}
	
	
	@Override
	public boolean hasPermissions() {
		
		try {
		if (getDomain()==null || getModel()==null)
			return false;
		
		if (!getDomain().getId().equals(getModelObject().getDomain().getId()))
			return false;
			
		return admin || isRoot();
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}
	
	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(getSessionUser());
	}
	
	public String getDriveDir() {
		if (drive_dir_name==null)
			drive_dir_name = ServiceLocator.getService(SystemParameterService.class).getParameter("integration.drive.home", "."+File.separator+"drive");
		return drive_dir_name;
	}
	
	
	public String getHomeDir() {
		if (home_dir_name==null) 
			home_dir_name=getDomain().getName().replace(" ", "").toLowerCase().trim();
		return home_dir_name;
	}
	
}
