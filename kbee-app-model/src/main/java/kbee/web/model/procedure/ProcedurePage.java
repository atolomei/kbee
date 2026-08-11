package kbee.web.model.procedure;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.beans.BeansService;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.workflow.ContentProcedure;
import com.novamens.content.workflow.WorkflowDao;

import com.novamens.kbee.content.workflow.KbeeProcedure;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;
import com.novamens.workflow.Procedure;

import kbee.util.logging.Logger;
import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.error.ErrorPanel;
import kbee.web.model.InformationModelDropDownBC;
import kbee.web.model.contentclass.ContentTemplatesDropDownBC;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;

@SuppressWarnings("serial")
public class ProcedurePage extends ApplicationPage<Procedure>  {
	private static final long serialVersionUID = 1L;
														
	private static Logger logger = Logger.getLogger(ProcedurePage.class.getName());
	
	private  final boolean is_root					= ServiceLocator.getService(SecurityService.class).isRoot(); 
	private  final boolean is_domain_admin			= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	private  final boolean is_model					= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	private  final boolean is_model_read			= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.MODEL_READ.getId());
	
	private	IModel<Procedure> model;
	
	public ProcedurePage(PageParameters parameters) {
		Procedure procedure = getProcedure(parameters);
		if (procedure!=null) {
			setModel(new ObjectModel<Procedure>(procedure));		
		}
	}
	
	public ProcedurePage(IModel<Procedure> model) {
		this.model = model;
	}
	
	@Override
	public void setModel(IModel<Procedure> model) {
		this.model = model;
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		if  (getProcedure()!=null && hasPermissions()) {
			
			setLogVisit(true);
			
			PageContentHeaderPanel<?> panel=new PageContentHeaderPanel<>();
			
			MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<>();
			bc.addElement(new SettingsDropDownBC());
			bc.addElement(new InformationModelDropDownBC());
			bc.addElement(new ContentTemplatesDropDownBC());
			
			bc.addElement(new ContentTemplateBC(new ObjectModel<ContentTemplate>(getContentTemplate())));
			
			bc.addElement(new BCElement(new Model<String>() {
				public String getObject() {
					String name = getProcedure().getName();
					if (name==null) 
						name = getLabelString("procedure")+". N/A";
					
					return  name + " <span class=\"ago\">("+getLabelString("procedure")+")</span>";
				}
			}));
			
			panel.setBreadcrumbPanel(bc);
			
			setPageTitle( new Model<String>(getModel().getObject().getDisplayName()));
			panel.setTitle(getModel().getObject().getDisplayName());
			
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
	
	public IModel<Procedure> getModel() {
		return this.model;
	}
	
	public Procedure getProcedure() {
		return this.model!=null ? this.model.getObject()  : null; 
	}

	@Override
	public boolean hasPermissions() {
		return is_domain_admin || is_root || is_model || is_model_read; 
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (this.model!=null) this.model.detach();
	}
	
	private void addComponents() {
		setPageTitle(getLabel("procedure", getProcedure().getName()));
		setPageDescription(getLabel("procedure", getProcedure().getName()));
		setTopNavigation(getMainTopbar()); 
		setMenu(getMainLaternalMenu());    
		if (hasPermissions()) {
			add(new ProcedureMainPanel( getModel()));
		}
		else {
			add(new ErrorPanel("editor"));
		}
		getPageParameters().set("id", getModel().getObject().getId().toString());
	}
	
	private Procedure getProcedure(PageParameters parameters) {
		Procedure procedure = null;
		StringValue id = parameters.get("id");
		if (!id.isNull() && !id.isEmpty()) {
			try {	
				procedure = (Procedure) getWorkflowDao().getProcedure(id.toLong());
				if (procedure!=null && procedure instanceof KbeeProcedure) {
					 procedure = (Procedure) getWorkflowDao().reload(procedure);
					if (! ((KbeeProcedure) procedure).getDomain().equals(getDomain())) {
						procedure = null;
					}	
				}
			} 
			catch (Exception e) {
				logger.error(e);
				procedure = null;
			}
		}	
		return procedure;
	}
	
	private ContentTemplate getContentTemplate() {
		Procedure procedure = getProcedure();
		ContentTemplate template = ((ContentProcedure)procedure).getContentTemplate();
		return template;
	}
	
	private WorkflowDao getWorkflowDao() {
		return (WorkflowDao)ServiceLocator.getService(BeansService.class).getBean("WorkflowDao");
	}
}