package kbee.web.datamanagement;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.error.ErrorPanel;
import kbee.web.nav.HomeBC;
import kbee.web.page.ApplicationPage;
import kbee.web.page.FactoryPage;
import kbee.web.page.PageContentHeaderPanel;

public class ThumbnailServicePage extends ApplicationPage<Domain> implements FactoryPage {

	
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = new kbee.util.logging.Logger(ThumbnailServicePage.class.getName());
    
	final boolean is_root 						= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
    private final boolean role_domain_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
    private final boolean role_service_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
    final boolean is_support 					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
    
    
    public ThumbnailServicePage() {
    }

    
    @Override
    public void onInitialize() {
    	super.onInitialize();
    
    	setOutputMarkupId(true);
    	
    	setPageTitle(new ResourceModel("cache"));
    	
        Domain domain = getDomain();
        
        if (domain==null) {
        	logger.error("domain is null");
        	add(new ErrorPanel("editor", "Not authorized", ""));
        	return;
        }	
        
        
		PageContentHeaderPanel<Domain> panel=new PageContentHeaderPanel<Domain>(null);
		setPageTitle(new ResourceModel("cache"));
		panel.setTitle(new ResourceModel("cache"));
		panel.setBreadcrumbPanel(getBreadcrumbPanel());
		setSearchPanel(false);
		setClearAllSearch(false);
		setAdvancedSearch(false);
		setSuggester(false);
		setPageContentHeader(panel);

        
        if (getDomain()!=null && hasPermissions()) {
        	setModel(new ObjectModel<Domain>(domain));
        	setTopNavigation(getMainTopbar());
    		setMenu(getMainLaternalMenu());
        	add(new ThumbnailServicePanel("editor", getModel()));
        }
        else
        	addOrReplace(new ErrorNotAuthorizedPanel<>("info-panel", new Model<String>("Not authorized")));
    }


    protected Panel getBreadcrumbPanel() {
        MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<>();
        bc.addElement( new HomeBC());
        bc.addElement( new FactoryDataManagementDropdownBC());
        
		bc.addElement(new BCElement(new StringResourceModel("cache", ThumbnailServicePage.this, null)));
		return bc;
}
    public void onDetach() {
    	super.onDetach();
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
