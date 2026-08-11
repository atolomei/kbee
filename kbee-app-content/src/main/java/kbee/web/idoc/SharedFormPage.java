package kbee.web.idoc;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.base.Content;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.service.TokenService;
import com.novamens.content.web.console.markup.ErrorPanel;
import com.novamens.dom.Json;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;
import kbee.web.eform.EFormDataModel;
import kbee.web.eform.EFormTemplateViewer;
import kbee.web.eform.EFormViewer;
import kbee.web.page.KbeeWebPage;
import kbee.web.searcher.page.SearcherResultsPage;

public class SharedFormPage extends KbeeWebPage<Content> {
	private static final long serialVersionUID = 1L;
				
	private static Logger logger = Logger.getLogger(SharedFormPage.class.getName());
	
	private static final ResourceReference KBEE_SEARCHER_CSS = new CssResourceReference(SearcherResultsPage.class, "searcher.css");

	private static final ResourceReference ICONS_CSS = new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/css/icons/icomoon/styles.css");
	
	private static final ResourceReference COMPONENTS_CSS = new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/css/components.css");
	
	private static final ResourceReference CORE_CSS = new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/css/core.css");
	
	private static final ResourceReference KBEE_JS = new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/js/core/kbee.js");
	
	private static final ResourceReference BOOTSTRAP_CSS = new CssResourceReference(Form.class, Form.BOOTSTRAP);
	
	private static final ResourceReference KBEE_BOOTSTRAP_CSS = new CssResourceReference(AbstractKbeeWebPage.class, "kbeebootstrap.css");
	
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_0_360      = new CssResourceReference(AbstractKbeeWebPage.class, "kb-0-360.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_361_800    = new CssResourceReference(AbstractKbeeWebPage.class, "kb-361-800.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_801_1200   = new CssResourceReference(AbstractKbeeWebPage.class, "kb-801-1200.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1201_1600  = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1201-1600.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1601  	 = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1601.css");
	
	private static final ResourceReference CSS_KBEE_LIMITLESS = new CssResourceReference(AbstractKbeeWebPage.class, "kbee-limitless.css");

	private static final ResourceReference AW = new CssResourceReference(Form.class, Form.FONTAWESOME);
	

	private IModel<EForm> formmodel = null;
	private Json tokendata = null;
	private String user = null;
	private PageParameters parameters;
	
	public SharedFormPage() {
	}

	public SharedFormPage(PageParameters parameters) {
		
		this.parameters = parameters;
		
		Content idoc = getContent(parameters);
		
		EForm form = getForm(parameters);
		
		String user = getUser(parameters);
	
		if (idoc!=null) {
			setModel(new ObjectModel<Content>(idoc));
		}
		
		if (form!=null) {
			setForm(new ObjectModel<EForm>(form));
		}
		
		if (user!=null) {
			setUser(user);
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(KBEE_SEARCHER_CSS));
		response.render(CssHeaderItem.forReference(ICONS_CSS));
		
		response.render(CssHeaderItem.forReference(COMPONENTS_CSS));
		
		response.render(CssHeaderItem.forReference(CORE_CSS));
		response.render(CssHeaderItem.forReference(KBEE_BOOTSTRAP_CSS));

		response.render(CssHeaderItem.forReference(BOOTSTRAP_CSS));

		response.render(JavaScriptHeaderItem.forReference(KBEE_JS));
		
		response.render(CssHeaderItem.forReference(AW));
		
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_0_360));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_361_800));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_801_1200));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1201_1600));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1601));
		response.render(CssHeaderItem.forReference(CSS_KBEE_LIMITLESS));

	}
	
	public EForm getForm() {
		return formmodel!=null ? formmodel.getObject() : null;
	}

	public void setForm(IModel<EForm> model) {
		this.formmodel = model;
	}
	
	public Content getContent() {
		return getModel()!=null ? getModelObject() : null;
	}
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		try {
			ServiceLocator.getService(SecurityService.class).authenticate(getUser());
		}
		catch (Exception e) {
			addOrReplace(new ErrorPanel("form", new Model<String>("eform"), new Model<String>("content or form not found or access denied.")));
		}
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (getContent()==null || getForm()==null) {
			addOrReplace(new ErrorPanel("form", new Model<String>("eform"), new Model<String>("content or form not found or access denied.")));
			return;
		}
		
		add(getViewer());
	}
	
	protected Content getContent(PageParameters parameters) {
		Content content = null;		
		try {
			if (getToken(parameters)==null) return null;
			String id =  (String)getToken(parameters).get("content");
			if (id!=null) {
				content = getContentDao().findContentById(Long.valueOf(id));
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
		return content;
	}
	
	protected EForm getForm(PageParameters parameters) {
		EForm form = null;
		try {
			if (getToken(parameters)==null) return null;
			String id =  (String)getToken(parameters).get("form");
			if (id!=null) {
				form = getRepository(EForm.class).findById(Long.valueOf(id));
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
		return form;
	}
	
	protected Panel getViewer() {
		EFormViewer eform;
		EFormData data = getContent().getFormData(new KbeeTaskForm(getForm()));
		IModel<EFormData> datamodel = new EFormDataModel(data);	

		if (getForm().getViewer()!=null) {
			boolean isPdfViewer = false;
			eform = new EFormTemplateViewer("form", datamodel, isPdfViewer); 
		}
		else 	
			eform =  new EFormViewer("form", datamodel, null);
		
		StringValue size = parameters.get("size");
		if (!size.isNull() && !size.isEmpty()) {
			eform.add(new AttributeModifier("style", "font-size:"+size.toString()+"px;"));
			//eform.add(new AttributeModifier("style", "font-size: unset;"));
		}	
		
		return eform;
	}
	
	protected String getUser(PageParameters parameters) {
		String user = null;
		try {
			if (getToken(parameters)==null) return null;
			user =  (String)getToken(parameters).get("user");
		}
		catch (Exception e) {
			logger.error(e);
		}
		return user;
	}
	
	private Json getToken(PageParameters parameters) {
		if (tokendata==null) {
			StringValue token = parameters.get("token");
			if (!token.isNull() && !token.isEmpty()) {
				tokendata = ServiceLocator.getService(TokenService.class).decode(token.toString());
			}
		}
		return tokendata;
	}
	
	private <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}
}