package kbee.web.datamanagement;


import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.error.ErrorPanel;
import kbee.web.page.AbstractApplicationPage;

public class ContentTemplateChangePage extends AbstractApplicationPage<Person> {
			
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	static private kbee.util.logging.Logger logger = new kbee.util.logging.Logger(TagManagementPage.class.getName());
    
    private final boolean role_domain_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
    private final boolean role_service_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());

	
    public ContentTemplateChangePage() {
    }
    
    
    public ContentTemplateChangePage(PageParameters parameters) {
    }
    
    @Override
    public void onInitialize() {
    	super.onInitialize();
    
    	setOutputMarkupId(true);
    	
    	setPageTitle(new ResourceModel("mainmenu.tag-management-tool"));
        Person person = getPerson();

        if (person==null) {									
        	addOrReplace(new ErrorNotAuthorizedPanel<>("info-panel", new Model<String>("Please ask admin user to grant permissions to this page")));
        	return;
        }
        
		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());  

		setModel(new ObjectModel<Person>(person));
        
        if (hasPermissions())
            add(new TagManagementPanel("info-panel", new ObjectModel<Domain>(getDomain())));
        else
			add(new ErrorNotAuthorizedPanel<>("info-panel"));
    }
    

    @Override
    protected boolean hasPermissions() {
        return (getSecurityService().isRoot() || role_domain_admin || role_service_admin);
    }

    private com.novamens.service.SecurityService getSecurityService() {
        return ServiceLocator.getService(com.novamens.service.SecurityService.class);
    }


    protected Person getPerson() {
        return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
    }
}
