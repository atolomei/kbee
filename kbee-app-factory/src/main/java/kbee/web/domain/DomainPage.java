package kbee.web.domain;



import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.model.ObjectId;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;

import com.novamens.indexer.service.Index;

import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.nav.DomainsBC;
import kbee.web.nav.HomeBC;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;


public class DomainPage extends ApplicationPage<Domain> {

	private static final long serialVersionUID = 1L;

	final boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();
	final boolean role_admin = is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_settings = is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SETTINGS.getId());
	final boolean role_service = is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
	final boolean role_factory = is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId());
	final boolean is_linux = is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.OPERATIONS_ENGINEER.getId());
	
  /**
   * @param parameters
   */
	public DomainPage(PageParameters parameters) {

		if (parameters!=null) {
			
			String domain_to_view_id;

			StringValue domain_to_view_id_SV = parameters.get("id");

			if (domain_to_view_id_SV.isNull() || domain_to_view_id_SV.isEmpty()) {
				domain_to_view_id = getDomain().getId().toString();
				
			}
			else {
				domain_to_view_id = domain_to_view_id_SV.toString();
				getPageParameters().set("id", domain_to_view_id);
			}
			
			String session_domain_id = getDomain().getId().toString();
			
			boolean edit_session_domain					= session_domain_id.equals(domain_to_view_id);
			boolean edit_from_kbee_domain_domain		= isDomainKbee();
			
			if (edit_session_domain || edit_from_kbee_domain_domain)
				setModel(new ObjectModel<Domain>(getDomain()));
				
			}
		}
	

	public DomainPage(IModel<Domain> model) {
		setModel(model);
		getPageParameters().set("id", model.getObject().getId());
		
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setTopNavigation(getMainTopbar());
		setMenu(getMainLaternalMenu());
		
		if (getModel()!=null && hasPermissions()) {
		
			setLogVisit(true);
				
			PageContentHeaderPanel<Domain> panel=new PageContentHeaderPanel<Domain>(null);
			
			panel.setTitle(getModel().getObject().getDisplayName());
	
			setLogVisit(true);
	
			if (isDomainKbee()) {
				MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<Void>();
				
				bc.addElement(new HomeBC());
				bc.addElement(new SettingsDropDownBC());
				
				bc.addElement(new DomainsBC());
				bc.addElement(new BCElement(new Model<String>(getModel().getObject().getDisplayName())));
				panel.setBreadcrumbPanel(bc);
				setSearchPanel(true);
				setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.domains", this, null).getObject()));
				panel.setSearchPanel(getSearchPanel());
			}
			else {
				MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<Void>();
				bc.addElement(new HomeBC());
				bc.addElement(new SettingsDropDownBC());
				bc.addElement(new BCElement( new StringResourceModel("general", this, null)));
				panel.setBreadcrumbPanel(bc);
				setSearchPanel(false);
			}
			setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.domains", this, null).getObject()));
			setAdvancedSearch(false);
			setSuggester(false);
			
			setPageContentHeader(panel);
			
			addComponents();
		}
		
		
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SETTINGS;
	}
	
	protected boolean hasPermissions() {
		
		if (getDomain()==null)
			return false;

		if (!getDomain().getName().equals("kbee") && !getDomain().getId().equals(getModel().getObject().getId()))
				return false;
		
		// domain is KBEE or domain is session user domain
		
		return role_admin || role_service || role_factory || role_settings;
	}

	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	private void addComponents() {
		
		setPageTitle(new Model<String>(getModel().getObject().getName()));
		setPageDescription(new Model<String>(getModel().getObject().getName()));
		
		if (hasPermissions()) { 
			add(new DomainMainPanel(getModel()));
		}
		else { 
			add(new ErrorNotAuthorizedPanel<>("editor"));
		}
	}


	protected String getPageType()     {return "det";} 													 // con | det  
	protected String getContentTitle() {return getModel().getObject().getDisplayName();} 				// content title or user title, ...
	protected String getStatsPageTitle() {return getModel().getObject().getDisplayName();} 			// for console page, it is the name of the console 
	protected Long getStatsPageId() {return new Long(0);} 								                // for console page, it is the name of the console
	protected String getObjectId()  {return new ObjectId(getModel().getObject()).toString();}    		// for user, domain, ...
	protected String getContentId() {return null;}	  													// for content
}
