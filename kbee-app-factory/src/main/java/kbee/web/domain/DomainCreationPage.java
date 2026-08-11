package kbee.web.domain;

import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.dom.Domain;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.nav.DropDownDomainsBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;


/***
 *  Top Level Sections are:
 *  
 *   TASK
 *   CONTENT
 *   INFORMATION MODEL
 *   SECURITY
 *   MY ACCOUNT
 *   AUDIT	
 *   SETTINGS
 *   ------------------
 *   DOMAIN
 *   SYSTEM INFO
 *   DATA MANAGEMENT
 *   API
 *   
 *   
 * 
 *
 */
public class DomainCreationPage extends ApplicationPage<Domain> {
	private static final long serialVersionUID = 1L;
	
	final boolean is_root 			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin	= is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_factory_admin	= is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId());
	final boolean is_service_admin	= is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
	
	public DomainCreationPage() {
		setPageTitle( new Model<String>( "Create Domain"));
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (hasPermissions()) {
			setTopNavigation(getMainTopbar());       // setNavigation(new GlobalNavigationBar<Person>("navigation"));
			setMenu(getMainLaternalMenu());       // setMenu(new NavBarLateralMenu("menu", getApplicationMenuSection().getKey()));
			
			PageContentHeaderPanel<Void> panel=new PageContentHeaderPanel<Void>(null);
			panel.setTitle("Domain");
			setPageContentHeader(panel);
			
			MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<Void>();
			bc.addElement(new DropDownDomainsBC());
			bc.addElement(new BCElement(new StringResourceModel("new", this)));
			panel.setBreadcrumbPanel(bc);
			add(new DomainCreationEditor("editor"));
		}
		else {
			setTopNavigation(new InvisiblePanel("navigation"));
			setMenu(new GlobalNavigationBar<Domain>("menu"));
			add(new ErrorNotAuthorizedPanel<>("editor"));
		}
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.DOMAINS;
	}
	 
	@Override
	public boolean hasPermissions() {
		if (!isDomainKbee())
			return false;
		return is_domain_admin || is_service_admin || is_factory_admin;
	}
}