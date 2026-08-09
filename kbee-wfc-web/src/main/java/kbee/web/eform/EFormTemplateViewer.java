package kbee.web.eform;



import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.base.Content;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;
import com.novamens.content.model.Classificable;
import com.novamens.kbee.content.form.KbeeEForm;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.template.KbeeContentTemplateModel;
import com.novamens.kbee.template.KbeeEMailTemplateModel;
import com.novamens.kbee.template.KbeeHtmlModel;
import com.novamens.kbee.template.KbeePdfModel;
import com.novamens.kbee.template.KbeeSignatureModel;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.portal6.model.Site;
import com.novamens.text.TextTemplate;

import kbee.web.error.ErrorPanel;


/***
 *  Cuando se crea la instancia de Firma
 *  se crea junto con la firma (que tiene la imagen)
 *  se crea un certificado de RSA y una clave privada
 *  
 * 
 * Cada usuario registra una firma que se instancia el Certificado X509 - 
 * X509 es un paquete exportable y publico
 * La clave privada se almacena junto con el Certificado 
 * 
 * UserSignature -> queda grabado ese Certificado [la clave publica, nombre distinguido, vigencia, ...]
 * El certificado se publica y se usa para validad la firma (que se hizo con una clave privada)
 * 
 * Esos certificados persisten el la base encriptados (tabla -> KbeeSignature)
 * 
 * ------
 *  cuando se firma el documento kbee, se calcula el Digest y con la clave privada se firma y se genera el string firmado.
 *  
 *  Se aplica con la clave privada del usuario se aplica sobre ese digest la encriptacion y genera como resultado el string firmado
 *  
 *  Con ese string-firmado y Certificado que tiene la clave publica puedo validad lo firmado.
 *  
 *  SignedData tiene
 *  . lo que firmaste (el string fuente usado para firmar, la FirmaDeUsuario, y el resultado de aplicar al digest la firma)
 * ------
 * 
 * 
 */
@SuppressWarnings("serial")
public class EFormTemplateViewer extends EFormViewer {

	public static final ResourceReference KBEE_EFORM_VIEWER_CSS = new CssResourceReference(EFormViewer.class, "eform-viewer-v1.css");
	
	private static final long serialVersionUID = 1L;
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EFormTemplateViewer.class.getName());
	
	private IModel<Site> sitemodel;
	private String error = null;
	private boolean isPdfViewer = false;
	
	public EFormTemplateViewer(String id, IModel<EFormData> model, boolean  isPdfViewer) {
		super(id, model);
		this.isPdfViewer=isPdfViewer;
	}
	
	public EFormTemplateViewer(String id, IModel<EFormData> model, IModel<Site> sitemodel, boolean  isPdfViewer) {
		super(id, model);
		this.sitemodel = sitemodel;
		this.isPdfViewer=isPdfViewer;
	}
	
	public boolean isPdfViewer() {
		return this.isPdfViewer;
	}
	
	public void setPdfViewer(boolean b) {
		this.isPdfViewer=b;
	}
	
	public EForm getForm() {
		return getModelObject().getForm();
	}
	
	public IModel<Site> getSiteModel() {
		return sitemodel;
	}
	
	@Override
	public Classificable getObject() {
		return null;
	}
	
	public Editor<?> getEditor() {
		return null;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		checkForm();
		addComponents();
		
		add(new WicketEventListener<EAjaxFormEvent>() {
			@Override
			public void onEvent(EAjaxFormEvent event) {
				if (handle(event)) {
					event.getRequestTarget().add(EFormTemplateViewer.this);
				}
			}
			public boolean handle(EAjaxFormEvent event) {
				return event.getRequestTarget()!=null;
			}
		});
		
		add(new WicketEventListener<EAjaxRefreshEvent>() {
			@Override
			public void onEvent(EAjaxRefreshEvent event) {
				if (handle(event)) {
					event.getRequestTarget().add(EFormTemplateViewer.this);
				}
			}
			public boolean handle(EAjaxRefreshEvent event) {
				return event.getRequestTarget()!=null;
			}
		});
	}
	
	protected void checkForm() {
		error = (new EFormChecker(getModelObject())).check();
	}

	
	/**
	 * 
	 */
	protected void addComponents() {
		
		IModel<String> textmodel = new Model<String>() {
			public String getObject() {
				return getText();
			}
		};
		
		WebMarkupContainer c= new WebMarkupContainer("payload-container");
		addOrReplace(c);
		
		Label text = new Label("text", textmodel);
		
		c.add(new AttributeModifier("class", isPdfViewer() ? " pdfViewer " :  " fr-view"));
		text.setEscapeModelStrings(false);
		c.addOrReplace(text);
		
		addOrReplace (new ErrorPanel("error", new Model<String>(error)) {
			@Override
			public boolean isVisible() {
				return error!=null;
			}
		});
	}
	
	
	protected String getText() {
		try {
			
			EForm eform = getForm();
			
			
			if (!(eform instanceof KbeeTaskForm))
				return "";
			
			/**
			 * Wrapper qe necesita Freemaker para resolver el modelo del Template
			 */
			
			KbeeEMailTemplateModel model = new KbeeEMailTemplateModel();
			
			eform = ((KbeeTaskForm)eform).getForm(); 
			EFormData data = getModelObject();
		
			Content content = ((EFormContentData)data).getContent();

			model.setContent(content);
			if (!data.getSignatures().isEmpty()) {
				model.setModel("signeddata",data.getSignatures().get(0));
				model.setModel("signature", new KbeeSignatureModel());
			}
			model.setModel("pdf", new KbeePdfModel());
			model.setModel("html", new KbeeHtmlModel());

			/**
			 * 
			 */
			String template = ((KbeeEForm)eform).getViewer();
			
			if (template!=null) {
				template = template.replace("%24", "$");
				TextTemplate texttemplate = new KbeeTextTemplate(template);
				String text = texttemplate.process(content);
				return text;
			}
			return null;
		} catch (Exception e) {
			logger.error(e);
			return e.getClass().getName()+ (e.getMessage()!=null? ("  " + e.getMessage()):"");
		}
	}
	
	
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(KBEE_EFORM_VIEWER_CSS));
		
	}
	
}