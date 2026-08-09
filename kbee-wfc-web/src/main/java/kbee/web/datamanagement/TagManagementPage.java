package kbee.web.datamanagement;

import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.error.ErrorPanel;
import kbee.web.nav.DataManagementBC;
import kbee.web.nav.DataManagementDropdownBC;
import kbee.web.nav.DataManagementPanelBC;
import kbee.web.nav.HomeBC;
import kbee.web.page.ApplicationPage;
import kbee.web.page.ConsoleObjectPage;
import kbee.web.page.PageContentHeaderPanel;

import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class TagManagementPage extends  ConsoleObjectPage<Person> {
    
	private static final long serialVersionUID = 1L;

    static private kbee.util.logging.Logger logger = new kbee.util.logging.Logger(TagManagementPage.class.getName());

    private List<IModel<DataSetMember>> dm_list;
    private final boolean role_domain_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
    private final boolean role_service_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
    private final boolean is_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
    
    
    public TagManagementPage() {
    }

    public TagManagementPage(PageParameters parameters) {
    }
    
    @Override
    public void onInitialize() {
    	super.onInitialize();
    
    	setOutputMarkupId(true);
    	
    	setPageTitle(new ResourceModel("mainmenu.tag-management-tool"));
    	
        Person person = getPerson();
        
        if (person==null) {
        	addOrReplace(new ErrorNotAuthorizedPanel<>("info-panel"));
        	return;
        }
        
        setModel(new ObjectModel<Person>(person));
        
        if (hasPermissions()) {	
        	
        	PageContentHeaderPanel<Person> header=new PageContentHeaderPanel<Person>(getModel());
        	header.setTitle(new ResourceModel("mainmenu.tag-management-tool"));
    		
        	MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<>();
        	bc.addElement( new HomeBC());
        	
			bc.addElement(new DataManagementDropdownBC());
			
			bc.addElement(new BCElement(new StringResourceModel("mainmenu.tag-management-tool", this, null)));
        	header.setBreadcrumbPanel(bc);
        	
        	setPageContentHeader(header);
        	setTopNavigation(getMainTopbar());
    		setMenu(getMainLaternalMenu());

        	TagManagementPanel panel=new TagManagementPanel("info-panel", new ObjectModel<Domain>(getDomain()));
        	
        	if (getSelection()!=null)
        		panel.setSelection(getSelection());
        	
        	if (getDataSetMemberSelection()!=null)
        		panel.setDataSetMemberSelection(getDataSetMemberSelection());
        	
        	add(panel);
        }
            
        else
			add(new ErrorNotAuthorizedPanel<>("info-panel"));
    }


    
    public void onDetach() {
    	super.onDetach();
    	if (list!=null) 
    		list.forEach(item -> item.detach());
    	if (dm_list!=null) 
   			dm_list.forEach(item -> item.detach());
    }
    
    
    
    public void setDataSetMemberSelection(List<IModel<DataSetMember>> list) {
    	this.dm_list=list;
    }
    
    public List<IModel<DataSetMember>> getDataSetMemberSelection() {
    	return this.dm_list;
    }

    
    
    private List<IModel<Content>> list;
    
    public void setSelection(List<IModel<Content>> list) {
    	this.list=list;
    }
    
    public List<IModel<Content>> getSelection() {
    	return this.list;
    }
	
    @Override
    protected boolean hasPermissions() {
        return (getSecurityService().isRoot() || role_domain_admin || role_service_admin || is_support);
    }

    private com.novamens.service.SecurityService getSecurityService() {
        return ServiceLocator.getService(com.novamens.service.SecurityService.class);
    }

    protected Person getPerson() {
        return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
    }
    
}
