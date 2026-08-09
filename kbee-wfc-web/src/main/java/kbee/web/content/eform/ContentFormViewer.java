package kbee.web.content.eform;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.kbee.wicket.viewer.Viewer;
import com.novamens.portal6.model.Site;

import kbee.web.eform.EFormDataModel;
import kbee.web.eform.EFormSharedViewer;
import kbee.web.eform.EFormTemplateViewer;
import kbee.web.eform.EFormViewer;
import kbee.web.eform.ESignatureViewer;


/**
 * 
 * used by inline 
 * 
 * {@link IDocHitExpandedPanelV6}
 * 
 *  CHECKOUT JS
 *  -----------
 *  IQL/JS para cada launcher -> que se evalua en cada uno de los contextos
 *  
 * @param <T>
 */
@SuppressWarnings("serial")
public class ContentFormViewer<T extends Content> extends ModelPanel<T> implements Viewer<T> {
	private static final long serialVersionUID = 1L;
	
	private IModel<EFormData> datamodel;
	
	private EFormViewer eform;
	private boolean shared = false;
	private IModel<Site> sitemodel;

	private Panel toolbar;
	
	private  WebMarkupContainer e_container;
	
	public ContentFormViewer(String id, IModel<T> model, EForm form) {
		this(id, model, form, null, false);
	}
	
	public ContentFormViewer(String id, IModel<T> model, EForm form, IModel<Site> sitemodel) {
		this(id, model, form, sitemodel, false);
	}
	
	public ContentFormViewer(String id, IModel<T> model, EForm form, boolean shared) {
		this(id, model, form, null, shared);
	}
	
	public ContentFormViewer(String id, IModel<T> model, EForm form, IModel<Site> sitemodel, boolean shared) {
		super(id, model);
		this.shared = shared;
		this.sitemodel = sitemodel;
		EFormData data = getModelObject().getFormData(form);
		setFormData(data);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		this.e_container = new WebMarkupContainer("eform-container");  

		addOrReplace(this.e_container);
		
		if (this.eform==null) {
			if (getForm().getViewer()!=null) {
				boolean isPdfViewer = false;
				eform = new EFormTemplateViewer("eform", datamodel, isPdfViewer);
				if (getForm().isFileContainer())
					this.toolbar = new ContentFormViewerToolbar("toolbar", datamodel);
			}
			else 	
				this.eform = shared ? 
					new EFormSharedViewer("eform", datamodel) : 
					new EFormViewer("eform", datamodel, sitemodel);
		}	
		
		if (this.toolbar==null)
			this.toolbar=new InvisiblePanel("toolbar");
		
		this.e_container.addOrReplace(toolbar);
		
		this.e_container.addOrReplace(eform);
		
		this.e_container.addOrReplace(new ESignatureViewer("signature", datamodel) {
			public boolean isVisible() {
				return getForm().getViewer()==null && datamodel.getObject().isSigned();
			}
		});
		
		this.e_container.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				return "eform-audit";
			}
		}));
	}

	public void setFormData(EFormData data) {
		datamodel = new EFormDataModel(data);	
	}
	
	public EForm getForm() {
		return datamodel.getObject().getForm();
	}
	
}