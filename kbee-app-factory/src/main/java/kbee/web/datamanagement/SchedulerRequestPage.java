package kbee.web.datamanagement;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.error.ErrorPanel;
import kbee.web.nav.HomeBC;
import kbee.web.nav.SecurityDropDownMenuBC;
import kbee.web.nav.UsersBC;
import kbee.web.notification.AccountDropDownBC;
import kbee.web.page.ApplicationPage;
import kbee.web.page.FactoryPage;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.scheduler.SchedulerDropdownBC;


public class SchedulerRequestPage extends ApplicationPage<Domain> implements FactoryPage {
			
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = new kbee.util.logging.Logger(TagManagementPage.class.getName());
    
	final boolean is_root 						= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
    private final boolean role_domain_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
    private final boolean role_service_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
    final boolean is_support 					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
    
    
    public SchedulerRequestPage() {
    }

    
    
    @Override
    public void onInitialize() {
    	super.onInitialize();
    
    	setOutputMarkupId(true);
    	
    	
    	
        Domain domain = getDomain();
        
        if (domain==null) {
        	logger.error("domain is null");
        	addOrReplace(new ErrorNotAuthorizedPanel<>("info-panel", new Model<String>("Not authorized")));
        	return;
        }
        
        
        if (getDomain()!=null && hasPermissions()) {
        	setModel(new ObjectModel<Domain>(domain));
        	setTopNavigation(getMainTopbar());
    		setMenu(getMainLaternalMenu());
    		add(new SchedulerRequestMainPanel("editor", new ObjectModel<Domain>(getDomain())));
    		
        }
        else
        	addOrReplace(new ErrorNotAuthorizedPanel<>("info-panel", new Model<String>("Not authorized")));
        
		PageContentHeaderPanel<Domain> panel=new PageContentHeaderPanel<Domain>(null);
		setPageTitle(new ResourceModel("execute-request"));
		panel.setTitle(new ResourceModel("execute-request"));
		panel.setBreadcrumbPanel(getSchedulerPanelBreadcrumbPanel());
		
		// panel.setBreadcrumbPanel(getContentHeaderPanelBreadcrumbPanel());
		
		setSearchPanel(false);
		setClearAllSearch(false);
		setAdvancedSearch(false);
		setSuggester(false);
		setPageContentHeader(panel);
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
    }


    public void onDetach() {
    	super.onDetach();
    }

    
    protected Panel getSchedulerPanelBreadcrumbPanel() {
			MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<>();
			
			bc.addElement( new HomeBC());
			bc.addElement( new FactoryDataManagementDropdownBC());
			bc.addElement( new SchedulerDropdownBC());
			bc.addElement(new BCElement(new StringResourceModel("execute-request", SchedulerRequestPage.this, null)));
			return bc;
	}

    

    
    @Override
    protected boolean hasPermissions() {
    	
    	if (isDomainKbee())
    		return (is_root || role_domain_admin || role_service_admin);
    	
    	return false;
    }


    protected Person getPerson() {
        return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
    }
}
