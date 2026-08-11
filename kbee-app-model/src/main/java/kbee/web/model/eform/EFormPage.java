package kbee.web.model.eform;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.form.EForm;
import com.novamens.content.model.ContentTemplate;
import com.novamens.kbee.content.form.KbeeEForm;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.error.ErrorPanel;
import kbee.web.model.InformationModelDropDownBC;
import kbee.web.model.contentclass.ContentTemplatesDropDownBC;
import kbee.web.model.procedure.ContentTemplateBC;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;

@SuppressWarnings("serial")
public class EFormPage extends ApplicationPage<EForm>  {
	private static final long serialVersionUID = 1L;
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EFormPage.class.getName());
	
	private  final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	private  final boolean is_domain_admin			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	private  final boolean is_model_read 			= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.MODEL_READ.getId());
	private  final boolean is_model					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	
	private IModel<EForm> eformmodel;
	private IModel<ContentTemplate> templatemodel;
	
	public EFormPage(PageParameters parameters) {
		
		EForm form = getForm(parameters);
		if (form!=null) {
			setForm(new ObjectModel<EForm>(form));		
		}
		
		ContentTemplate template = getTemplate(parameters);
		
		if (template!=null) {
			setTemplate(new ObjectModel<ContentTemplate>(template));		
		}
	}
	
	
	public EFormPage(IModel<EForm> model, IModel<ContentTemplate> template_model) {
		setForm(model);
		setTemplate(template_model);
		
		if (model.getObject() instanceof com.novamens.content.form.EIdentifiableForm)
			getPageParameters().add("eform",  ( ( com.novamens.content.form.EIdentifiableForm) model.getObject() ).getId().toString());
		
		getPageParameters().add("template", template_model.getObject().getId().toString());

		
	}
	
	public void setForm(IModel<EForm> model) {
		this.eformmodel = model;
		setModel(model);
	}
	
	public void setTemplate(IModel<ContentTemplate> model) {
		this.templatemodel = model;
	}
	
	public IModel<ContentTemplate> getTemplateModel() {
		return this.templatemodel;
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		if (	getModel()!=null 				&&	
				getModel().getObject()!=null 	&&
				getTemplateModel()!=null 		&&
				hasPermissions() ) {
			
					setLogVisit(true);
					
					PageContentHeaderPanel<?> panel = new PageContentHeaderPanel<>();
		
					MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<>();
					bc.addElement( new SettingsDropDownBC());
					bc.addElement(new InformationModelDropDownBC());
					bc.addElement(new ContentTemplatesDropDownBC());
					bc.addElement(new ContentTemplateBC(getTemplateModel()));
					
					bc.addElement(new BCElement(new Model<String>() {
						public String getObject() {
							String name = EFormPage.this.getModel().getObject().getDisplayName();
							if (name==null) name =  new StringResourceModel("eform", EFormPage.this, null).getObject() +". N/A";
							return  name + " <span class=\"ago\">("+new StringResourceModel("eform", EFormPage.this, null).getObject()+")</span>";
						}
					}));
					
					panel.setBreadcrumbPanel(bc);
					
					setPageTitle( new Model<String>(getModel().getObject().getDisplayName()));
					
					panel.setTitle(getModel().getObject().getDisplayName());
					setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.datasets", this, null).getObject()));
					setSearchPanel(false);
					setAdvancedSearch(false);
					setSuggester(false);
					setPageContentHeader(panel);			
					addComponents();
		}
		else {
			setTopNavigation(getMainTopbar());
			addOrReplace(new ErrorNotAuthorizedPanel<>("editor", new Model<String>("Not authorized")));
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (this.eformmodel!=null)
			this.eformmodel.detach();
		
		if (this.templatemodel!=null)
			this.templatemodel.detach();
	}
	
	public IModel<EForm> getModel() {
		return this.eformmodel;
	}

	@Override
	public boolean hasPermissions() {
		return is_domain_admin || is_root || is_model || is_model_read; 
	}
	
	private void addComponents() {
		
		
		// setPageTitle( new Model<String>( new StringResourceModel("eform", EFormPage.this, null).getObject() +". "+  getModel().getObject().getName()));
		
		setPageDescription( new Model<String>( new StringResourceModel("eform", EFormPage.this, null).getObject() +". "+  getModel().getObject().getName()));

		setTopNavigation(getMainTopbar()); 
		setMenu(getMainLaternalMenu());    

		if (hasPermissions()) {
			add(new EFormMainPanel(getTemplateModel(), getModel()));
		}
		else {
			add(new ErrorPanel("editor"));
		}
		
		getPageParameters().set("eform", ((KbeeEForm)getModel().getObject()).getId().toString());
	}


	private EForm getForm(PageParameters parameters) {
		try {
			EForm form = null;
			StringValue id = parameters.get("eform");
			if (!id.isNull() && !id.isEmpty()) {
				form  = getRepository(EForm.class).findById(Long.valueOf(id.toString()));
			}	
			return form;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	private ContentTemplate getTemplate(PageParameters parameters) {
		try {
		ContentTemplate template = null;
		StringValue id = parameters.get("template");
		if (!id.isNull() && !id.isEmpty()) {
			template = (ContentTemplate)getContentDao().findModelObjectById(ContentTemplate.class, id.toLong());
			if (template!=null && !template.getDomain().equals(getDomain())) {
				template = null;
			}
		}	
		return template;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
}