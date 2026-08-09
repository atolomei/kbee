package kbee.web.eform;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormEvent;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.UpdatedField;
import com.novamens.content.model.Classificable;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.event.Event;
import com.novamens.kbee.content.form.EFormAbstractField;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.markup.html.console.browser.RefreshClickEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Field;


/*
 * mandatory -> the editor is mandatory 
 * read only -> 
 * enabeld   ->
 */
@SuppressWarnings("serial")
public class EFieldPanel<T extends EFormField<?>> extends EComponentPanel<T> {
	private static final long serialVersionUID = 1L;
	
	private List<Serializable> messages = new ArrayList<Serializable>();
	private boolean autofocus, initialized=false;

	public EFieldPanel(String id, T field, IModel<EFormData> data) {
		super(id, field, data);
	}
	
	public void setFocus(AjaxRequestTarget target) {
		if (getContainer()==null) {
			onInitialize();
		}
		Field<?> field = getInput();
		if (field!=null) {
			target.focusComponent(field.getInput());
		}
	}
	
	public Field<?> getInput() {
		return (Field<?>)getContainer().get("field");
	}
	
	public void update(Classificable classificable) {
		getField().set(classificable, getData());
	}
	
	public T getField() {
		return getComponentModel().getObject();
	}
	
	public IModel<T> getFieldModel() {
		return getComponentModel();
	}
	
	public String getHelpText() {
		return getField().getHelpText();
	}
	
	public boolean isEnabled() {
		return true;
	}
	
	public boolean isRequired() {
		return false;
	}
	
	public boolean isEditionEnabled() {
		return getEditor()!=null && getEditor().isEditionEnabled();
	}
	
	public boolean isReadOnly() {
		return getField().isReadOnly() || getData().isSigned();
	}
	
	public void setAutoFocus(boolean value) {
		this.autofocus = value;
	}
	
	public boolean autofocus() {
		return autofocus;
	}
	
	public void addMessage(Serializable message) {
		messages.add(message);
	}
	
	public List<Serializable> getMessages() {
		return messages;
	}
	
	public void clearMessages() {
		messages.clear();
	}
	
	public Classificable getFormObject() {
		EFormPanel eformpanel = getFormPanel();
		if (eformpanel !=null) {
			return eformpanel.getObject();
		}
		return null;
	}
	
	public EFormPanel getFormPanel() {
		MarkupContainer parent = getParent();
		EFormPanel panel = null;
		while (panel==null && parent!=null) {
			if (parent instanceof EFormPanel) {
				panel = (EFormPanel)parent;
			}
			else {
				parent = parent.getParent();
			}	
		}
		return panel;
	}
	
	public Editor<?> getEditor() {
		return getFormPanel().getEditor();
	}
	
	@Override
	public void fireScanAll(Event event) {
		if (findPage()!=null) {
			super.fireScanAll(event);
		}
		else {
			MarkupContainer parent = getParent();
			MarkupContainer panel = null;
			while (parent!=null) {
				panel = parent;
				parent = parent.getParent();
			}
			fire(event, panel.iterator(), false);
		}
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (getInput()!=null)
		getInput().add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				String css = "efield";
				if (!getMessages().isEmpty()) {
					css = "eform-error";
				}
				return css;
			}
		}));
	}
	
	@Override
	public void onAfterRender() {
		super.onAfterRender();
		setAutoFocus(false);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (isKbInitialized()) return;
		
		add(new WicketEventListener<EAjaxFormEvent>() {
			@Override
			public void onEvent(EAjaxFormEvent event) {
				if (handle(event)) {
					List<?> values =  getValues(event);
					if (values!=null) {
						setValues(values);
					}
					if (event.getRequestTarget()!=null) {
						refresh(event.getRequestTarget());
					}
				}
			}
			public boolean handle(EAjaxFormEvent event) {
				return getField().getModel()!=null && (getField().getModel().handle(event) || getField().getCalculation()!=null);
			}
			public List<?> getValues(EAjaxFormEvent event) {
				if (getField().getCalculation()!=null) {
					return calculate(event);
				}
				else {
					return getField().getModel().onEvent(event);
				}
			}
		});
		
		add(new WicketEventListener<RefreshClickEvent>() {
			@Override
			public void onEvent(RefreshClickEvent event) {
				refresh(event.getRequestTarget());
			}
		});
		
		initialized = true;
	}
	
	protected boolean isKbInitialized() {
		return initialized;
	}
	
	protected void validate(AjaxRequestTarget target) {
		if (!getMessages().isEmpty()) {
			clearMessages();
			refresh(target);
		}
		getField().validate(new KbeeEValidatable(getData(), getField()) {
			public void onError(String key) {
				String message = getLabel(key, getField().getLabel()).getObject();
				addMessage(message);
				((Panel)getEditor()).error(new FieldMessage(EFieldPanel.this, getData().getForm(), getField(), message, FeedbackMessage.ERROR));
			}
		});
	}
	
	protected void onUpdate(AjaxRequestTarget target) {
		if (getField().getOnUpdate()!=null) {
			((EFormAbstractField<?>)getField()).onUpdate(getData());
		}
		fireScanAll(new EAjaxFormEvent(target, getField(), getData()));
		if (!getMessages().isEmpty()) {
			clearMessages();
			refresh(target);
		}
	}
	
	protected void addFeedbackPanel() {
		getContainer().addOrReplace(getFeedbackPanel());
	}
	
	protected WebMarkupContainer getFeedbackPanel() {
		WebMarkupContainer panel = new WebMarkupContainer("feedback-panel") {
			public boolean isVisible() {
				return !EFieldPanel.this.getMessages().isEmpty();
			}
		};
		Label message = new Label("message", new Model<String>() {
			public String getObject() {
				return !getMessages().isEmpty() ?  (String)getMessages().get(0) : null;
			}
		});
		message.setEscapeModelStrings(false);
		panel.add(message);
		return panel;
	}
	
	protected void refresh(AjaxRequestTarget target) {
		target.add(getContainer());
	}
	
	protected List<?> calculate(EFormEvent event) {
		Object calculation =((EFormAbstractField<?>)getField()).calculate(event.getFormData(), event);
		if (calculation!=null && !(calculation instanceof List<?>)) {
			List<Object> values = new ArrayList<Object>();
			if (calculation!=null) values.add(calculation);
			return values;
		}
		else {
			return (List<?>)calculation;
		}
	}
	
	protected void setValues(List<?> values) {
	}
	
	protected void setUpdatedPart(String part) {
		if (getEditor()!=null) {
			getEditor().setUpdatedPart(part);
		}	
	}
	
	protected void setUpdatedField(UpdatedField update) {
		if (getEditor()!=null) {
			getEditor().setUpdatedField(update);
		}	
	}
	
	protected IModel<String> getLabel(String key, String... parameter) {
		StringResourceModel model = new StringResourceModel(key, this);
		model.setParameters((Object[])parameter);
		return model;
	}
	
	protected Domain getDomain() {
		return (Domain)ServiceLocator.getService(UserService.class).getDomain();
	}
}