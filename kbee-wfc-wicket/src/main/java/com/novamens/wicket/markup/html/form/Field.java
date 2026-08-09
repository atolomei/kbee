package com.novamens.wicket.markup.html.form;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IFormModelUpdateListener;
import org.apache.wicket.markup.html.form.ValidationErrorFeedback;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.string.interpolator.VariableInterpolator;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.hibernate.proxy.HibernateProxy;

import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.services.PanelFactory;
import com.novamens.security.Identifiable;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;

/**
 * 
 * 
 * 
 * 
 * 
 *  <h3>HELP</h3>
 *  
 *  The link for inline "help" 	->  getHelpInfo() != null 
 *  the (i) icon  				-> 	isHelpInfo()=TRUE. It is the caller panel who opens the help dialog or other  
 * 
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public abstract class Field<T> extends KBPanel implements IFormModelUpdateListener {
	
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(Field.class.getName());

	
	private int tab_index = -1;
	private boolean readonly = false;
	private boolean required;
	private boolean feedback = false;
	private Editor<?> editor;

	private IModel<T> model, valuemodel;
	private T value;
	
	private IValidator<T> validator;
	private String property;
	private Disposition disposition;
	private Width width = Width.W10;
	private List<Behavior> behaviors;
	private boolean autofocus = false;
	
	private IModel<String> subtitle;
	
	
	
	
	private PanelFactory infofactory = null;
	protected WebMarkupContainer infopanel = null;
	protected boolean infovisible = false;
	
	public class MessageSource implements IErrorMessageSource {
		public String getMessage(String key, Map<String, Object> vars) {
			// return (new StringResourceModel(key, getInput(), null)).getObject();

			final Map<String, Object> parameterMap = (vars != null) ? vars : new HashMap<String, Object>();

			if (getLabel() != null)
				parameterMap.put("label", getLabel().getObject());
			final String s = new StringResourceModel(key, getInput(), null).getString();
			return new VariableInterpolator(s,
					org.apache.wicket.Application.get().getResourceSettings().getThrowExceptionOnMissingResource()) {
				private static final long serialVersionUID = 1L;

				@Override
				protected String getValue(String variableName) {
					Object value = parameterMap.get(variableName);

					if (value != null && (value instanceof String)) {
						return String.valueOf(value);
					}
					return null;
				}
			}.toString();

		}
	}
	
	public enum Width {
		
		W01 ("col-lg-1"),
		W02 ("col-lg-2"), 
		W03 ("col-lg-3"), 
		W04 ("col-lg-4"),
		W05 ("col-lg-5"),
		W06 ("col-lg-6"),
		W07 ("col-lg-7"),
		W08 ("col-lg-8"),
		W09 ("col-lg-9"),
		W10 ("col-lg-10"),
		W11 ("col-lg-11"),
		W12 ("col-lg-12");
		
		private String css;
		
		private Width(String css) {
			this.css = css;
		}
		public String getCss() {
			return css;
		}
	};

	
	/**
	 * 
	 * 
	 * @param id
	 * @param model
	 */
	public Field(String id, IModel<T> model) {
		super(id);
		setProperty(id);
		setModel(model);
	}
	

	public void updateModel() {
	}
	

	public void setSubtitle(IModel<String> s) {
		subtitle=s;
	}
	
	public IModel<String> getSubtitle() {
		return subtitle;
	}

	
	public void cancel() {
		clearInput();
		if (getModel()!=null)
		setValue(getModel().getObject());
	}
	
	public void clearInput() {
		feedback = false;
		getFeedbackMessages().clear();
		if (getInput()!=null && getInput() instanceof FormComponent)
			((FormComponent<?>)getInput()).clearInput();
	}
	
	public void add(IValidator<T> validator) {
 		this.validator = validator;
	}
	
	public IValidator<T> getValidator() {
		return validator;
	}
	
	public Component getInput() {
		return get("input");
	}
	
	public void setModel(IModel<T> model) {
		this.model = model;
	}
	
	public IModel<T> getModel() {
		return this.model;
	}
	
	
	
	
	public int getTabIndex() {
		return this.tab_index;
	}
	
	public void setTabIndex(int t_index) {
		this.tab_index=t_index;
	}
	
	
	public void setReadOnly(boolean b) {
		this.readonly=b;
	}
	
	public boolean isReadOnly() {
		return readonly;
	}

	
	public void setDisposition(Disposition disposition) {
		this.disposition = disposition;
	}
	
	public Disposition getDisposition() {
		if (this.disposition==null) {
			if (getEditor()!=null) {
				if (getEditor().getForm()!=null) {
					if (getEditor().getForm() instanceof Form)
						this.disposition = ((Form<?>)getEditor().getForm()).getDisposition();
				}
			}
			else {
				if (getForm()!=null) { 
					this.disposition = getForm().getDisposition();
				}
			}
		}
		
		
		if (this.disposition==null)
			return Disposition.VERTICAL;
		
		return this.disposition;
	}
	
	public void setProperty(String name) {
		this.property = name;
	}
	
	public String getProperty() {
		return this.property;
	}
	
	public void setRequired(boolean value) {
		this.required = value;
	}
	
	public boolean isRequired() {
		return required;
	}
	
	
	/**
	 * (i) icon
	 * @return
	 */
	public boolean isHelpInfo() {
		return false;
	}
	
	public String helpIcon() {
		return "far fa-info-circle";
	}
	
	public boolean isNullValid() {
		return false;
	}
	
	public String getMessage() {
		if (hasErrorMessage()) {
			ValidationErrorFeedback error =	(ValidationErrorFeedback)((ValidationError)getFeedbackMessages().first().getMessage()).getErrorMessage(new MessageSource());
			return (String)error.getMessage();
		}			
		return null;
	}
	
	public boolean hasFeedback() {
		return feedback;
	}
	
	public T getValue() {
		if (value==null && valuemodel!=null) {
			value = valuemodel.getObject(); 
		}
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
		if (value==null) valuemodel = null;
	}
	
	@SuppressWarnings("unchecked")
	public void setFieldValue(String value) {
		String[] values = { value };
		((org.apache.wicket.markup.html.form.TextField<T>)getInput()).setModelValue(values);
	}
	
	public Width getWidth() {
		return width;
	}
	
	public void setWidth(Width width) {
		this.width = width;
	}
	
	public void setError(ValidationError error) {
		error(error);
		feedback = true;
	}
	
	public void addInputBehavior(Behavior behavior) {
		if (behaviors==null) 
			behaviors = new ArrayList<Behavior>();
		behaviors.add(behavior);
	}
	
	public void addInfoPanel(PanelFactory  panel) {
		infofactory = panel;
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (getModel() == null) {
			Editor<?> editor = getEditor();
			if (editor!=null) {
				IModel<T> model = new PropertyModel<T>(editor.getModel(), getProperty());
				setModel(model);
				setValue(model.getObject());
			}
		}
	}
	
	@Override
	public void onAfterRender() {
		super.onAfterRender();
		setAutoFocus(false);
	}
	
	@Override
	public void onDetach() {
		if (value!=null && (!(value instanceof Serializable) || value instanceof com.novamens.dom.Object || value instanceof HibernateProxy)) {
			valuemodel = getModel(value);
			valuemodel.detach();
			value = null;
		}
		super.onDetach();
	}
	
	public void addBehaviors() {
		if (behaviors!=null) {
			for (Behavior behavior : behaviors) {
				getInput().add(behavior);
			}
		}
	}
	
	public void setAutoFocus(boolean value) {
		autofocus = value;
	}
	
	protected IModel<T> getModel(T value) {
		if (value instanceof Identifiable) {
			return new ObjectModel<T>(value);
		}
		logger.error((value!=null?value.toString():"null") + " -> is not Identifiable");
		return null;
	}	
	
	protected Component getFeedback() {
		WebMarkupContainer feedback = new WebMarkupContainer("feedback") {
			@Override
			public boolean isVisible() {
				return Field.this.hasFeedback();
			}
		};
		
		Label err=new Label("error", new PropertyModel<String>(this, "message")) {
			@Override
			public boolean isVisible() {
				return Field.this.hasFeedback();
			}
		};
		err.setEscapeModelStrings(false);
		feedback.add(err);
		
		WebMarkupContainer icon = new WebMarkupContainer("icon") {
			@Override
			public boolean isVisible() {
				return false;
				// return Field.this.hasFeedback();
			}
		};
		
		
		icon.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				if (Field.this.hasFeedback()) {
					if (Field.this.hasFeedback()) {
						return "fal fa-times form-control-feedback";
					}
				}
				return "glyphicon glyphicon-ok form-control-feedback";
			}
		}));
		
		feedback.add(icon);
		
		return feedback;
	}
	
	protected boolean autofocus() {
		return autofocus;
	}

	protected IModel<String> getHelpText() {
 		IModel<String> model = new StringResourceModel(getProperty()+".help", Field.this, null);
		try {
			model.getObject();
			return model;
		}
		catch (MissingResourceException e) {
			return null;
		}
	}
	
	public void validate() {
		feedback = false;
		getFeedbackMessages().clear();
		final Object input = getInputValue();
		if (isRequired() && (input==null || "".equals(input))) {
			ValidationError message = (new ValidationError()).addKey("requiredvalidator.message");
			error(message);
			onError(message.toString());
			feedback = true;
			return;
		}
		if (validator!=null) {
			IValidatable<T> validatable = new IValidatable<T>() {
				@SuppressWarnings("unchecked")
				public T getValue() {
  					return (T)input;
				};
				public void error(IValidationError error) {
					Field.this.error(error);
					feedback = true;
				};
				public boolean isValid() {
					return true;
				}
				public IModel<T> getModel() {
					return null;
				}
			};
			validator.validate(validatable);
		}
 	}
	
	public void validateModel() {
		getFeedbackMessages().clear();
		final Object input = getModel().getObject();
		if (isRequired() && (input==null || "".equals(input))) { 
			error((new ValidationError()).addKey("requiredvalidator.message"));
			feedback = true;
			return;
		}
		if (validator!=null) {
			IValidatable<T> validatable = new IValidatable<T>() {
				@SuppressWarnings("unchecked")
				public T getValue() {
					return (T)input;
				};
				public void error(IValidationError error) {
					Field.this.error(error);
					feedback = true;
				};
				public boolean isValid() {
					return true;
				}
				public IModel<T> getModel() {
					return null;
				}
			};
			validator.validate(validatable);
		}
 	}
	
	protected Object getInputValue() {
		return ((FormComponent<?>)getInput()).getInput();
	}
	
	protected void setFeedback() {
		this.feedback = true;
	}
	
	protected void setFeedback(boolean value) {
		this.feedback = value;
	}
	
	protected Editor<?> getEditor() {
		if (editor==null) {
			MarkupContainer parent = getParent();
			while (editor==null && parent!=null) {
				if (parent instanceof Editor) {
					editor = (Editor<?>)parent;
				}
				else
					parent = parent.getParent();
			}
		}
		return editor;
	}
	
	protected Form<?> getForm() {
		MarkupContainer parent = getParent();
		while (parent!=null) {
			if (parent instanceof Form) {
				return (Form<?>)parent;
			}
			else
				parent = parent.getParent();
		}
		return null;
	}
	
	protected void onError(Serializable message) {
	}
	
	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		if (Field.this.hasFeedback()) {
			if (Field.this.hasErrorMessage()) {
				tag.put("class", (tag.getAttribute("class")!=null ? tag.getAttribute("class"): "" ) +" has-error has-feedback");
			}
			else {
				tag.put("class", (tag.getAttribute("class")!=null ? tag.getAttribute("class"): "" ) + " has-success has-feedback");
			}
		}
		else {
			tag.put("class", tag.getAttribute("class"));
		}
	}
	
	protected void setValueModel(IModel<T> model) {
		this.valuemodel = model;
	}   
	
	protected IModel<T> getValueModel() {
		return this.valuemodel;
	}
	
	protected boolean isRequiredMark() {
		return isRequired(); 
	}
	
	private IModel<String> getLabel(){
		try {
			return new StringResourceModel("property."+getProperty(), this, null);
		} 
		catch (java.util.MissingResourceException e) {
			return new Model<String>(getProperty());
		} 
		catch (Exception e2) {
			return new Model<String>(getProperty());
		}
	}
	
	protected Component getInfo() {
		WebMarkupContainer infocontainer = new WebMarkupContainer("info-panel");
		if (infofactory!=null) {
			infopanel = infofactory.create("panel");
			infocontainer.add(infopanel);
			infopanel.setVisible(infovisible);
		}

		infocontainer.setVisible(infofactory!=null);
		return infocontainer;
	}
}
