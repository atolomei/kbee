package kbee.web.domain;

 
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.web.console.markup.ErrorPanel;
import com.novamens.dom.Domain;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.kbee.wicket.services.PanelFactory;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.nav.DomainSectionBC;
import kbee.web.nav.DomainsBC;
import kbee.web.nav.DropDownDomainsBC;
import kbee.web.nav.SettingsBC;
import kbee.web.page.AbstractApplicationPage;
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
@Deprecated
public class DomainCreationPage2 extends ApplicationPage<Domain> {
													
	static private kbee.util.logging.Logger logger = new kbee.util.logging.Logger(DomainCreationPage2.class.getName());

	private static final long serialVersionUID = 1L;
	
	final boolean is_root 			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin	= is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_factory_admin	= is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId());
	final boolean is_service_admin	= is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
	
	public DomainCreationPage2() {
		setPageTitle( new Model<String>( "Create Domain"));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		

		
		
		if (hasPermissions()) {
			
			//add(new MenuBreadCrumbPanel("bc2",  
			//		DomainCreationPage.this.getModel(),
			//		new DomainSectionBC(), new DomainsBC()));
			
			setTopNavigation(getMainTopbar());       // setNavigation(new GlobalNavigationBar<Person>("navigation"));
			setMenu(getMainLaternalMenu());       // setMenu(new NavBarLateralMenu("menu", getApplicationMenuSection().getKey()));
			
			
			PageContentHeaderPanel<Void> panel=new PageContentHeaderPanel<Void>(null);
			panel.setTitle("Domain");
			setPageContentHeader(panel);
			
			
			MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<Void>();
			bc.addElement(new DropDownDomainsBC());
			bc.addElement(new BCElement(new StringResourceModel("new", this)));
			panel.setBreadcrumbPanel(bc);

			
			add(new DomainCreationEditor2("editor"));
			
			/**
			try {
				PanelFactory factory = (PanelFactory) ServiceLocator.getService(BeansService.class).getBean("domain-creation-editor-factory");
				add((factory!=null) ? factory.create() : new DomainCreationEditor("editor"));
			} catch (Exception e) {
				logger.error(e, " Bean domain-creation-editor-factory does not exist");
				add(new DomainCreationEditor("editor"));
			}*/
		}
		else {
			//add(new MenuBreadCrumbPanel("bc2",  
			//		DomainCreationPage.this.getModel(),
			//		new DomainSectionBC(), new DomainsBC()));
			
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
