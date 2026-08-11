package kbee.web.content.eform;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormEvent;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EResourceModel;
import com.novamens.content.form.EValidatable;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.kbee.content.form.EFormAbstractField;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.markup.html.event.GeneralWicketAjaxEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.form.FormLayout;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.eform.EFieldPanel;
import kbee.web.eform.EFormDataModel;
import kbee.web.content.editor.ClassificationPanel;
import kbee.web.content.editor.ContentEditor;
import kbee.web.eform.EAjaxFormEvent;
import kbee.web.eform.EAjaxFormResourceEvent;
import kbee.web.eform.EFormEditor;
import kbee.web.eform.EFormEditorToolbar;
import kbee.web.eform.EFormTemplateViewer;
import kbee.web.eform.EFormViewer;
import kbee.web.eform.ESignaturePanel;
import kbee.web.eform.FieldMessage;
import kbee.web.event.wicket.ContentEditorEvent;
import kbee.web.workflow.task.TaskErrorEvent;

/**
 * 
 * Task Page
 * 
 * Edit
 * View
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public class ContentFormEditor<T extends Content> extends ObjectEditorPanel<T> implements ClassificationPanel<T> {
	
	static final long serialVersionUID = 1L;
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentFormEditor.class.getName());

	private WebMarkupContainer alert;
	private WebMarkupContainer mainContainer;
	private WebMarkupContainer eformContainer;
	
	private IModel<EFormData> datamodel;
	private boolean updated = false;
	
	private Panel eform, preview;
	private Panel signature;

	private boolean initialeditionenabled;
	
	private boolean isSignatureVisible_on_off = false;
	
	/** --------------------------------
	 * 
	 * 
	 * 
	 *
	 */
	public class KbeeEValidatable implements EValidatable {
		EForm form;
		EFormField<?> field;
		public KbeeEValidatable(EForm form, EFormField<?> field) {
			this.form = form;
			this.field = field;
		}
		public Object getValue() {
			return getData().getData(getField());
		}
		public EFormField<?> getField() {
			return field;
		}
		public EFormData getData() {
			return datamodel.getObject();
		}
		public void error(String key) {
			error(key, getField().getLabel());
		}
		public void error(String key, String... parameter) {
			String message;
			try {
				message = getLabelString(key, parameter);
			}
			catch (Exception e) {
				message = key;
			}
			setError(getField(), message);
			((Panel)getEditor()).error(new FieldMessage(ContentFormEditor.this, getForm(), getField(), message, FeedbackMessage.ERROR));
		}
	}
	
	
	/** --------------------------------
	 * 
	 *
	 */
	public ContentFormEditor(IModel<EFormData> datamodel) {
		this("panel", datamodel);
	}
	
	public ContentFormEditor(EFormData data, Editor<T> editor) {
		this("panel", new EFormDataModel(data));
		setEditor(editor);
	}
	
	public ContentFormEditor(String id, IModel<EFormData> datamodel) {
		super(id);
		setOutputMarkupId(true);
		this.datamodel = datamodel;
	}
	
	public void setFocus(AjaxRequestTarget target, EFormField<?> field) {
		onInitialize();
		if (field!=null)
		getFormPanel().setFocus(target, field);
	}
	
	public void setError(EFormField<?> field, Serializable message) {
		onInitialize();
		getFormPanel().setError(field, message);
	}
	
	public void setFocus(EFormField<?> field) {
		onInitialize();
		getFormPanel().setFocus(field);
	}
	
	public EForm getForm() {
		return getData().getForm();
	}
	
	public EFormData getData() {
		return this.datamodel.getObject();
	}
	
	@Override
	public void validate() {
		// si es solo un viewer no valida
		if (isReadOnly()) {
		//if (!(eform instanceof EFormEditor)) {
			return;
		}
		if (eform!=null) {  
			getFormPanel().clearMessages();
		}	
		for (EFormField<?> field : getForm().getFields()) {
			field.validate(new KbeeEValidatable(getForm(), field)); 
		}
	}
	
	@Override
	public void updateModel() {
		if (updated) {
			getModelObject().setFormData(datamodel.getObject());
			updated = false;
		}
	}
	
	public void setUpdated(boolean value) {
		updated = true;
	}
	
	public List<Classification> getClassification() {
		T content = getModelObject();
		update(content);
		return content.getClassification();
	}
	
	public boolean includes(Classifier classifier) {
		return false;
	}
	
	public boolean includes(Attribute attribute) {
		return false;
	} 
	
	public boolean isUpdated() {
		return updated;
	}
	
	public void update(T content) {
		for (EFieldPanel<?> fieldpanel : getFieldPanels()) {
			fieldpanel.update(content);
		}
	}
	
	public List<String> getAttributeValue(Attribute attribute) {
		List<String> values = new ArrayList<String>();
		return values;
	}
	
	public List<Classifier> getClassifiers() {
		List<Classifier> classifiers = new ArrayList<Classifier>();
		return classifiers;
	}
	
	public boolean renderedForm() {
		return eform instanceof EFormEditor;
	}
	
	/**
	 * Atencion: USERA ADDORREPLACE porque este Panel se inicializa externamente a veces para validar el Form
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		initialeditionenabled = ((ContentEditor<?>)getEditor()).isEditionEnabled();
		
		//if (isReadOnly()) {
		//	AlertPanel<T> pa= new AlertPanel<T>("read-only", AlertPanel.INFO, getModel(), null, new Model<String>("This form is Read Only"));
		//	addOrReplace(pa);
		//	
		//}
		//else
		//	addOrReplace(new InvisiblePanel("read-only"));
		
		
		if (mainContainer==null) {
			mainContainer = new WebMarkupContainer("main-panel");
			add(mainContainer);
		}
		
		eformContainer = new WebMarkupContainer("eform-container");
		
		
		if (eform==null) {
			
			if (getForm() instanceof KbeeTaskForm) {
				if (((KbeeTaskForm) getForm()).getFormLayout()!=null)
					logger.debug(((KbeeTaskForm) getForm()).getFormLayout()!=null?((KbeeTaskForm) getForm()).getFormLayout().getDisplayName():"null" );
			}
			else
				logger.debug(" Not a TaskForm -> " + getForm().getClass().getName()); 
			
			
			if (isSignatureRequired() && isReadOnly()) {
				eform = getViewer(datamodel.getObject());
				mainContainer.add( new AttributeModifier("class", "eform-editor-signature"));
			}
			else {
				if (isReadOnly()) {
					eform = getViewer(datamodel.getObject());
				}
				else {
					eform = new EFormEditor("eform", datamodel);
				}
				mainContainer.add( new AttributeModifier("class", "eform-editor-form"));
				if (getForm().getViewer()!=null)
					eform.add( new AttributeModifier("style",   "border: 1px solid  #dededf;			    padding: 15px;			    margin-bottom: 1px; float:left; width:100%;"));
			}
			
			if (getForm().getCssClass()!=null) {
				eformContainer.add(new AttributeModifier("class", () -> getForm().getCssClass()));
			}
		}	
		
		mainContainer.addOrReplace(eformContainer);
		eformContainer.addOrReplace(eform);
		
		/** ------------------------
		 * Signature
		 * */
		 if (isSignatureRequired() || getData().isSigned() ) {
			 
			    isSignatureVisible_on_off = !getData().isSigned();
			 
				signature = new ESignaturePanel("signature", datamodel) {
					@Override
					public boolean isVisible() {
						return isSignatureVisible_on_off;
					}
					@Override
					public boolean validate() {
						if (!ContentFormEditor.this.isReadOnly()) {
							((Panel)getEditor()).getFeedbackMessages().clear(null);
							ContentFormEditor.this.validate();
							return !(((Panel)getEditor()).hasErrorMessage());
						}
						else {
							return true;
						}
					}
					@Override
					public boolean isEditionEnabled() {
						return getEditor().isEditionEnabled();	
					}
					@Override
					protected void onUnsign(AjaxRequestTarget target) {
						super.onUnsign(target);
						getData().clearSignatures();
						for (ResourceTag tag : getResourceTags()) {
							fireScanAll(new EAjaxFormResourceEvent(target, null, getData(), null, tag));
						}
					}
					@Override
					protected void setSignedState(AjaxRequestTarget target) {
						super.setSignedState(target);
						// si se firma un recurso(archivo) se esta modificando los datos del form 
						// por lo que se debe notificar a todos los componentes/campos del cambio
						for (ResourceTag tag : getResourceTags()) {
							fireScanAll(new EAjaxFormResourceEvent(target, null, getData(), null, tag));
						}
						if (target!=null) {
							target.add(ContentFormEditor.this);
						}
					}
					protected void setNotSignedState(AjaxRequestTarget target) {
						super.setNotSignedState(target);
						((ContentEditor<?>)getEditor()).setEditionEnabled(initialeditionenabled);
						if (target!=null) {
							target.add(ContentFormEditor.this);
						}
					}				
					@Override
					protected void setSignatureState(AjaxRequestTarget target) {
						getEditor().update(target);
						if (!validate()) {
							target.add(ContentFormEditor.this);
							setErrorState(target);
						}
						else {
							super.setSignatureState(target);
							((ContentEditor<?>)getEditor()).setEditionEnabled(false);
							target.add(ContentFormEditor.this);
						}
					}
				};
		}
		else
			signature = new InvisiblePanel("signature");	

		
		 
		 
		isSignatureVisible_on_off = signature.isVisible();
		eformContainer.add(signature);


		/** -------------------------
		 * Viewer
		 * */

		if (!(getForm() instanceof KbeeTaskForm)) {
			preview=new InvisiblePanel("preview");
		}
		else {
			FormLayout layout = ((KbeeTaskForm) getForm()).getFormLayout();
			if ((layout.equals(FormLayout.EDITOR_WITH_VIEWER) || layout.equals(FormLayout.VIEWER)) && !isReadOnly()) {
				boolean isPdfViewer = false;
				preview = new EFormTemplateViewer("preview", datamodel, isPdfViewer);    	
			}
			else
				preview=new InvisiblePanel("preview");
		}
			 
		
		mainContainer.addOrReplace(preview);
		
		if (alert==null)
			alert =new InvisiblePanel("alert");

		
		addOrReplace(alert);	
		addOrReplace(mainContainer);
		
		
		if (hasToolbar()) {
			EFormEditorToolbar toolbar = new EFormEditorToolbar("toolbar", datamodel, isSignatureVisible_on_off);
			addOrReplace(toolbar);
			
		} else {
			addOrReplace( new InvisiblePanel("toolbar"));
		}
		
	}
	
	
	
	/**
	 * @param event
	 */
	@SuppressWarnings("unchecked")
	public void handle(EFormEvent event) {
		for (EFormField<?> field : getForm().getFields()) {
			if (field.getModel()!=null && (field.getModel().handle(event) || field.getCalculation()!=null)) {
				if (field.getCalculation()!=null) {
					Object calculation =((EFormAbstractField<?>)field).calculate(event.getFormData(), event);
					List<Object> values;
					if (calculation!=null && !(calculation instanceof List<?>)) {
						values = new ArrayList<Object>();
						if (calculation!=null) values.add(calculation);
					}
					else {
						values = (List<Object>)calculation;
					}
					if (values!=null) {
						setUpdated(true);
						getData().setData(field, getModel(values));
					}
				}
				else {
					List<?> values = field.getModel().onEvent(event);
					if (values!=null) {
						setUpdated(true);
						if (field.isSingleValue()) {
							Object value = values.isEmpty() ? null : getModel(values.get(0));
							getData().setData(field, value);
						}
						else {
							getData().setData(field, getModel(values));
						}
					}
				}
			}
		}
	}
	
	public List<ResourceTag> getResourceTags() {
		List<ResourceTag> tags = new ArrayList<ResourceTag>();
		for (EFormField<?> field : getForm().getFields()) {
			if (field.getModel() instanceof EResourceModel) {
				tags.add(((EResourceModel<?>)field.getModel()).getTag());
			}
		}
		return tags;
	}	
	
	
	public void setAlert(WebMarkupContainer alertpanel) {
		alert = alertpanel;
	}
	
	public boolean isSignatureRequired() {
		return false;
	}
	
	protected Panel getSignaturePanel() {
		return signature;
	}
	
	public boolean isReadOnly() {
		return getForm() instanceof KbeeTaskForm && ((KbeeTaskForm)getForm()).isReadOnly();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		datamodel.detach();
	}
	
//	protected IModel<String> getLabel(String key, String... parameter) {
//		StringResourceModel model = new StringResourceModel(key, this);
//		model.setParameters((Object[])parameter);
//		return model;
//	}
	
	protected EFormEditor getFormPanel() {
		return (EFormEditor)eform;
	}
	
	protected List<EFieldPanel<?>> getFieldPanels() {
		List<EFieldPanel<?>> panels = new ArrayList<EFieldPanel<?>>();
		for (Panel panel : getFormPanel().getPanelFactory().getPanels()) {
			if (panel instanceof EFieldPanel) {
				panels.add((EFieldPanel<?>)panel);
			}
		}
		return panels;
	}

	/**
	 * 
	 * if it has Viewer -> it has Toolbar
	 * 
	 * @return
	 */
	protected boolean hasToolbar() {
		return  datamodel.getObject().getForm().hasToolbar();
	}
	
	@Override
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<GeneralWicketAjaxEvent>() {
			public boolean handles(Class<GeneralWicketAjaxEvent> claz) {
				boolean b=super.handles(claz);
				if (!b)
					return false;
				return  claz.getName().equals("preview") ||
						claz.getName().equals("removeSignature") ||
						claz.getName().equals("editor");
			}
			@Override
			public void onEvent(GeneralWicketAjaxEvent event) {
				ContentFormEditor.this.handleToolbarEvent(event);
			}
		});
		
		add(new WicketEventListener<EAjaxFormEvent>() {
			public void onEvent(EAjaxFormEvent event) {
				T content = getEditor().getModelObject();
				((ContentEditor<T>)getEditor()).update(content);
				fireScanAll(new ContentEditorEvent(event.getRequestTarget(), content));
				if (isSignatureRequired()) { 
					((ESignaturePanel) getSignaturePanel()).refresh(event.getRequestTarget());
				}
				updated = true;
			}
		});	
		
		add(new WicketEventListener<TaskErrorEvent>() {
			public void onEvent(TaskErrorEvent event) {
				event.getRequestTarget().add((ContentFormEditor.this));
			}
		});
	}
	
	protected EFormViewer getViewer(EFormData data) {
		if (data.getForm().getViewer()!=null) {
			boolean isPdfViewer = data.getForm().isFileContainer();
			return new EFormTemplateViewer("eform", datamodel, isPdfViewer);
		}
		else
			return new EFormViewer("eform", datamodel);
	}

	
	protected void handleToolbarEvent(GeneralWicketAjaxEvent event) {
		if (event.getName().equals("editor")) {
			eformContainer.setVisible(!eformContainer.isVisible());
			event.getRequestTarget().add(this);
			return;
		}
		if (event.getName().equals("preview")) {
			preview.setVisible(!preview.isVisible());
			event.getRequestTarget().add(this);
			return;
		}
		if (event.getName().equals("signaturePanel")) {
			isSignatureVisible_on_off=!isSignatureVisible_on_off;
			event.getRequestTarget().add(this);
			return;
		}
	}
	
	private List<Object> getModel(List<?> values) {
		List<Object> models = new ArrayList<Object>();
		for (Object value : values) {
			models.add(getModel(value));
		}
		return models;
	}
	
	private Object getModel(Object value) {
		Object model;
		if (value instanceof PersonMember) {
			model = new ObjectModel<PersonMember>((PersonMember)value);
		}
		else
		if (value instanceof DataSetMember) {
			model = new ObjectModel<DataSetMember>((DataSetMember)value);
		}
		else
		if (value instanceof Content) {
			model = new ObjectModel<Content>((Content)value, true);
		}
		else
		if (value instanceof Resource) {
			model = new ObjectModel<Resource>((Resource)value, true);
		}
		else {
			model = value;
		}
		return model;
	}

}