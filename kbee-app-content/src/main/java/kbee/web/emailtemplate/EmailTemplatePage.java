package kbee.web.emailtemplate;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.email.EmailTemplate;
import com.novamens.content.model.ObjectId;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorPanel;

import kbee.web.nav.SettingsDropDownBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;

@SuppressWarnings("serial")
public class EmailTemplatePage extends ApplicationPage<EmailTemplate> {
	private static final long serialVersionUID = -1L;

	final boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();
	final boolean is_domain_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean is_settings = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SETTINGS.getId());
	final boolean is_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	
	final boolean has_permission = is_root || is_domain_admin || is_model || is_support || is_settings;				

	
	boolean isNew = false;
	
	public EmailTemplatePage(PageParameters parameters) {
	
		EmailTemplate et = getEmailTemplate(parameters);
		if (et != null && et.getDomain().getId().equals(getDomain().getId())) {
			setModel(new ObjectModel<EmailTemplate>(et));
		}
		
		
	}
	
	
	public EmailTemplatePage(IModel<EmailTemplate> model) {
		this(model,false);
	}
	
	
	public EmailTemplatePage(IModel<EmailTemplate> model, boolean isnew) {
		super(model);
		this.isNew=isnew;
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());

		if (getModel()!=null && hasPermissions()) {
			PageContentHeaderPanel<?> panel=new PageContentHeaderPanel<>();
								
			MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<>();
			bc.addElement(new SettingsDropDownBC());
			bc.addElement(new EmailTemplatesDropDownBC());
			bc.addElement(new BCElement(new Model<String>(getModel().getObject().getDisplayName())));
			panel.setBreadcrumbPanel(bc);
				
			setPageTitle( new Model<String>("Email Template: " + getModel().getObject().getDisplayName()+ " (" +  getModel().getObject().getLanguage()+")"));
			panel.setTitle(getModel().getObject().getDisplayName());
			setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.emailtemplates", this, null).getObject()));
			setSearchPanel(true);
			setAdvancedSearch(false);
			setSuggester(false);
			panel.setSearchPanel(getSearchPanel());
			setPageContentHeader(panel);
				
			EmailTemplateMainPanel editor = new EmailTemplateMainPanel(getModel(), isNew()) {
				@Override
				protected void onClose(AjaxRequestTarget target) {
					//setResponsePage(new com.novamens.content.web.security.markup.RulesPage());
				}
			};
			editor.setEditionEnabled(isNew());
			add(editor);
			getPageParameters().set("key", getModel().getObject().getKey());
			getPageParameters().set("lg", getModel().getObject().getLanguage());
		}
		else {
			add(new ErrorPanel("editor", "authorization error", ""));
		}
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SETTINGS;
	}
	
	private EmailTemplate getEmailTemplate(PageParameters parameters) {
		String etkey = null;
		String etlg = null;
		
		if (parameters.get("key")!=null && !"".equals(parameters.get("key").toString())) {
			etkey = parameters.get("key").toString();
		}					
		if (parameters.get("lg")!=null && !"".equals(parameters.get("lg").toString())) {
			etlg = parameters.get("lg").toString();
		}
		
		if (etkey==null || etlg==null)
			return null;
		
		return (EmailTemplate) getContentDao().findEmailTemplate(getDomain(), etlg, etkey);
		
	}
	
	public boolean isNew() {
		return this.isNew;
	}
	
	@Override
	protected boolean hasPermissions() {
		
		if (getModel()==null || getModel().getObject()==null)
			return false;
		
		if (!getDomain().getId().toString().equals(getModel().getObject().getDomain().getId().toString()))
			return false;

		return has_permission;
	}
	
	
	protected String getPageType()     {return "det";} 													// con | det  
	protected String getContentTitle() {return getModel().getObject().getDisplayName();} 				// content title or user title, ...
	protected String getStatsPageTitle() {return getModel().getObject().getDisplayName();} 				// for console page, it is the name of the console 
	protected Long getStatsPageId() {return Long.valueOf(0);} 								            // for console page, it is the name of the console
	protected String getObjectId()  {return new ObjectId(getModel().getObject()).toString();}    		// for user, domain, ...
	protected String getContentId() {return null;}	  													// for content

}
