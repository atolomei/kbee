package kbee.web.security.user;

import com.novamens.kbee.bulkImport.RowEntityLoader;
import com.novamens.kbee.bulkImport.UserBulkRowImporter;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;

import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.error.ErrorPanel;

import kbee.web.nav.SecurityDropDownMenuBC;
import kbee.web.nav.UsersBC;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;

public class UserBulkCreationPage extends ApplicationPage<User> {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UserBulkCreationPage.class.getName());
	
    private final boolean admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
    final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	
    
    public UserBulkCreationPage() {
    }

    
    @Override
     public void onInitialize() {
    	super.onInitialize();

		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());

        
		PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);
		panel.setTitle(new StringResourceModel("batchcreation", this, null));
		
		MenuBreadCrumbPanel<?> bc = new MenuBreadCrumbPanel<>();
		bc.addElement(new SecurityDropDownMenuBC());
		bc.addElement(new UsersBC());
        bc.addElement(new BCElement("batchcreation"));
		panel.setBreadcrumbPanel(bc);

		setSearchPanel(false);
		setAdvancedSearch(false);
		setSuggester(false);
		
		setPageContentHeader(panel);
		
		// panel.setSearchPanel(getSearchPanel());
        if (hasPermissions())
        	add(new BulkCreationPanel("editor") {
				 
				private static final long serialVersionUID = 1L;

				@Override
				public RowEntityLoader getRowLoader() {
					return new UserBulkRowImporter();
				}
			});
        else
			add(new ErrorNotAuthorizedPanel<>("editor"));
     }
    
    @Override
    protected boolean hasPermissions() {
        final boolean has_permission = is_root || admin || is_support;
        return has_permission;

    }
}
