package kbee.web.eform;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.WorkflowContext;

import kbee.web.error.ErrorPanel;
import kbee.web.page.ApplicationMenuSection;

public class EFormPrintPage extends AbstractKbeeWebPage {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EFormPrintPage.class.getName());
					
											
	
	private static final long serialVersionUID = 1L;
	
	public static final ResourceReference KBEE_EFORM_VIEWER_CSS = new CssResourceReference(EFormViewer.class, "eform-viewer.css");
	
	
	
	private IModel<EFormData> model;
	//private IModel<WorkflowContext> contextModel;
	
	
	
	/**
	 * 
	 */
	public EFormPrintPage(PageParameters parameters) {
			super();
	}
	
	/**
	 * 
	 */
	public EFormPrintPage(IModel<EFormData> model) {
		this.model=model;
	}
	
	
	
	/**
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		logger.debug(this.getClass().getName());
		
		if (this.model==null) {
			logger.error("model is null");
			add(new ErrorPanel("eform-render", new Model<String>("model is null")));
			return;
		}
		
		if ( getForm().getViewer()!=null) {
			
			boolean isPdfViewer = false;
			add(new EFormTemplateViewer("eform-render", getModel(),  isPdfViewer));
		}
		else	
			add(new ErrorPanel("eform-render", new Model<String>("form has no Viewer"))); 
		
		
	}
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (this.model!=null)
			this.model.detach();
	}
	
	
	public IModel<EFormData> getModel() {
		return this.model;
	}
	
	public EForm getForm() {
		return getModel().getObject().getForm();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(KBEE_EFORM_VIEWER_CSS));
	}

	
	/** 
	 * Este debe sobrecargar a kbee2.css para usar bootstrap
	 */
	@Override
	protected ResourceReference getCssResource() {
		return  null;
	}

	
	@Override
	public String getPageHelpKey() {
		return  getApplicationMenuSection().getKey() + "-" + this.getClass().getSimpleName().toLowerCase();
	}

	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.TASK;
	}


	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	
}
