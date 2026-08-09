package kbee.web.eform;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;

import com.novamens.beans.BeansService;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormContainer;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.model.Classificable;
import com.novamens.event.EventListener;
import com.novamens.kbee.content.form.KbeeEFormRow;
import com.novamens.kbee.wicket.editor.ClassificableEditor;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.markup.html.event.SignEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketAjaxEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;

import kbee.web.error.ErrorPanel;
import kbee.web.panel.AlertPanel;


/**
 * 
 * 
 * EFormData
 * Eform
 * 
 * - PDF
 * - View Editor | Signed Document | 
 * - Remove Signature
 *
 * ------------------------------------
 * Plan
 * ----------------------------------------
 * User -> Person
 * Login con email / FGT pwd con email
 * ----------------------------------------
 *
 */
@SuppressWarnings("serial")
public class EFormEditor extends ModelPanel<EFormData> implements EFormPanel {
	
	private static final long serialVersionUID = 1L;
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EFormEditor.class.getName());
	
	private EEditorFactory panelfactory;
	private EFormField<?> focusfield;
	private boolean first = true;
	private String error = null;
	
	private WebMarkupContainer alert;
	private WebMarkupContainer components;
	private WebMarkupContainer componentsContainer;
	

	public EFormEditor(String id, IModel<EFormData> model) {
		super(id, model);
	}
	
	public EForm getForm() {
		return getModelObject().getForm();
	}
	
	public EEditorFactory getPanelFactory() {
		if (panelfactory==null) {
			panelfactory = new EEditorFactory(getModel());
		}
		return panelfactory;
	}
	
	public void setFocus(AjaxRequestTarget target, EFormField<?> field) {
		Panel focuspanel = getPanelFactory().getPanel(field);
		if (focuspanel instanceof EFieldPanel<?>) {
			((EFieldPanel<?>)focuspanel).setFocus(target);
		}
	}
	
	public void setFocus(EFormField<?> field) {
		focusfield = field;
	}
	
	public EFormField<?> getFocusField() {
		if (focusfield==null && !getForm().getFields().isEmpty()) {
			focusfield = getForm().getFields().get(0);
		}
		return focusfield;
	}
	
	public void setError(EFormField<?> field, Serializable message) {
		Panel fieldpanel = getPanelFactory().getPanel(field);
		((EFieldPanel<?>)fieldpanel).addMessage(message);
	}
	
	public void clearMessages() {
		List<EFormField<?>> fields = getForm().getFields();
		for (EFormField<?> field : fields) {
			Panel panel = getPanelFactory().getPanel(field);
			if (panel instanceof EFieldPanel) {
				((EFieldPanel<?>)panel).clearMessages();
			}
		}
	
	}

	public void setAlert(WebMarkupContainer alertpanel) {
		alert=alertpanel;
	}
	
	
	@Override
	@SuppressWarnings("unchecked")
	public Classificable getObject() {
		Editor<?> editor = getEditor();
		if (editor instanceof ClassificableEditor<?>) {
			Classificable classificable = ((ClassificableEditor<Classificable>)editor).getModelObject();
			((ClassificableEditor<Classificable>)editor).update(classificable);
			return classificable;
		}
		logger.warn("Classificable is null");
		return null;
	}
	
	public Editor<?> getEditor() {
		MarkupContainer parent = getParent();
		Editor<?> editor = null;
		while (editor==null && parent!=null) {
			if (parent instanceof Editor) {
				editor = (Editor<?>)parent;
			}
			else
				parent = parent.getParent();
		}
		
		if (editor==null)
			logger.error("Editor is null");
		return editor;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		try {
			
			setOutputMarkupId(true);
			
			checkForm();
			addBehaviors();
			addComponents();
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			WebMarkupContainer err= new WebMarkupContainer("component");
			addOrReplace(err);
			err.add(new ErrorPanel("panel",e));
		}
		
	}

	@Override
	public void onAfterRender() {
		super.onAfterRender();
		Optional<AjaxRequestTarget> target = RequestCycle.get().find(AjaxRequestTarget.class);

		if (first && error==null) {
			
			EFormField<?> field =  getFocusField();

			Panel focuspanel = getPanelFactory().getPanel(field);
			
			if (focuspanel instanceof EFieldPanel) 
				((EFieldPanel<?>)focuspanel).setAutoFocus(true);
			else {
				logger.error("panel must be of class " + EFieldPanel.class.getName());
			}
			
			if (target!=null && target.isPresent())
				setFocus(target.get(), field);

			first = false;
		}
		else {
			if (target!=null && target.isPresent()) {

				List<EFormField<?>> fields = getForm().getFields();
				
				for (EFormField<?> field : fields) {

					Panel panel = getPanelFactory().getPanel(field);
					if (panel instanceof EFieldPanel && !((EFieldPanel<?>)panel).getMessages().isEmpty()) {
						setFocus(target.get(), field);
						break;
					}
				}
			}
		}
	}
	
	protected void checkForm() {
		error = (new EFormChecker(getModelObject())).check();
	}
	
	
	
	
	@Override
	public void addListeners() {
		super.addListeners();
	
		add(new WicketEventListener<SignEvent>() {
			@Override
			public void onEvent(SignEvent event) {
				if (EFormEditor.this.getModelObject().isSigned()) {
					EFormEditor.this.alert=new AlertPanel<EFormData>("alert", AlertPanel.INFO, getModel(), 
						getLabel("signed-title"),
						getLabel("signed-text"));
				}
				else {
					EFormEditor.this.alert = new InvisiblePanel("alert");
				}
				EFormEditor.this.addOrReplace(alert);
				event.getRequestTarget().add(EFormEditor.this);
			}
		});
		
		add(new WicketEventListener<EAjaxRefreshEvent>() {
			public void onEvent(EAjaxRefreshEvent event) {
				EFormEditor.this.handle(event);
			}
		});
		add(new WicketEventListener<EAjaxFormEvent>() {
			public void onEvent(EAjaxFormEvent event) {
				EFormEditor.this.handle(event);
			}
		});
		add(new WicketEventListener<EFocusEvent>() {
			public void onEvent(EFocusEvent event) {
				EFormEditor.this.handle(event);
			}
		});
	}
	/**
	 * 
	 * 
	 */
		
	@SuppressWarnings("unchecked")
	protected void addComponents() {

		if (getModelObject().isSigned()) {
			alert=new AlertPanel<EFormData>("alert", AlertPanel.INFO, getModel(), 
					new StringResourceModel("signed-title", this, null),
					new StringResourceModel("signed-text", this, null));
			((AlertPanel<EFormData>) alert).setIcon(AlertPanel.ICON_SIGNED);
		}
		if (alert==null)
			alert = new InvisiblePanel("alert");
		addOrReplace(alert);
		

			
		
		//Components --------------
		//
		componentsContainer = new WebMarkupContainer("components-container");
		componentsContainer.setOutputMarkupId(true);
		
		components = new ListView<EFormComponent>("component", getForm().getComponents()) {
			@Override
			public void populateItem(ListItem<EFormComponent> item) {
				EFormComponent c = item.getModelObject();
				Panel panel = getPanel("panel", c);
				if (( !(c instanceof EFormContainer)) || c instanceof KbeeEFormRow) {
					item.add(new AttributeModifier("class", "row"));
				}
				if (panel!=null) {
					item.add(panel);
				}
			}
			@Override
			public boolean isVisible() {
				return error==null;
			}
		};

		addOrReplace(componentsContainer);
		componentsContainer.addOrReplace( components );
		
		addOrReplace (new ErrorPanel("error", new Model<String>(error)) {
			@Override
			public boolean isVisible() {
				return error!=null;
			}
		});
	}
	
	
	protected void addBehaviors() {
		for (String behaviorbean : getForm().getBehaviors()) {
			try {
				Object bean = ServiceLocator.getService(BeansService.class).getBean(behaviorbean);
				if (bean instanceof Behavior) {
					add((Behavior)bean);
				}
			}
			catch(Exception e) {
				logger.error(e);
			}
		}
	}
	
	protected Panel getPanel(String id, EFormComponent component) {
		return getPanelFactory().getPanel(id, component);
	}
	
	

	
	
	protected void handle(WicketAjaxEvent event) {
		for (Behavior  behavior : getBehaviors()) {
			if (behavior instanceof EventListener) {
				if (((EventListener)behavior).listen(event)) {
					((EventListener)behavior).onEvent(event);
				}
			}
		}
	}

	protected void handle(EFocusEvent event) {
		List<EFormField<?>> fields = getForm().getFields();
		int fieldindex = 0;
		for (EFormField<?> field : fields) { 
			if (event.getField().getName().equals(field.getName())) {
				break;
			}
			else {
				fieldindex++;
			}
		}
		if (fieldindex<fields.size()-1) {
			EFormComponent focusfield = fields.get(fieldindex+1);
			Panel focuspanel = getPanelFactory().getPanel(focusfield);
			if (focuspanel instanceof EFieldPanel<?>) {
				((EFieldPanel<?>)focuspanel).setFocus(event.getRequestTarget());
			}
		}
	}
}