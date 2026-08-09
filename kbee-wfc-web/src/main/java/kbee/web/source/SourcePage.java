
package kbee.web.source;

import com.novamens.content.base.Source;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorPanel;

import kbee.web.nav.SettingsDropDownBC;
import kbee.web.nav.SourcesBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class SourcePage extends ApplicationPage<Source> {
	private static final long serialVersionUID = -1L;
		
	final boolean is_root			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_security 		= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	final boolean is_support 		= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	
	public SourcePage() {
		
	}
	
	public SourcePage(PageParameters parameters) {
		Source source = getSource(parameters);
		if (source != null)
			setModel(new ObjectModel<Source>(source));
		
		
	}
	

	boolean isNew=false;
	
	public SourcePage(IModel<Source> model, boolean isNew) {
		setModel(model);
		this.isNew=isNew;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void onInitialize() {
		super.onInitialize();
	
		if (getModel()!=null) {
			
			getPageParameters().set("id", getModel().getObject().getId());
			setPageTitle(new Model<String>(getModel().getObject().getDisplayName()));

			setTopNavigation(getMainTopbar());  
			setMenu(getMainLaternalMenu());
			
			PageContentHeaderPanel<Source> panel=new PageContentHeaderPanel<Source>(null);
			panel.setTitle(getModel().getObject().getDisplayName());
	
			
			MenuBreadCrumbPanel  bc = new MenuBreadCrumbPanel();
			
			bc.addElement(new SettingsDropDownBC());
			bc.addElement(new SourcesBC());
			bc.addElement(new BCElement(new Model<String>(getModel().getObject().getDisplayName())));
			panel.setBreadcrumbPanel(bc);
			
			setSearchPanel(false);
			setAdvancedSearch(false);
			setSuggester(false);
			setPageContentHeader(panel);
			
			SourceMainPanel editor = new SourceMainPanel(getModel(), false) {
				@Override
				protected void onClose(AjaxRequestTarget target) {
					setResponsePage(new SourcesPage());
				}
			};
			editor.setEditionEnabled(isNew);
			add(editor);
			
		}
		else {
			add(new ErrorPanel("editor", "not found", ""));
		}
		
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SETTINGS;
	}
	
	private Source getSource(PageParameters parameters) {
		if (parameters.get("id")!=null && !"".equals(parameters.get("id").toString())) {
			String sourceId = parameters.get("id").toString();
			Source source = getRepository(Source.class).findById(Long.valueOf(sourceId));
			if (source !=null && source.getDomain().getId().equals(getDomain().getId()))
				return source;
		}	
		return null;
	}
}
