package com.novamens.wicket.markup.html.form;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidator;

import com.novamens.wicket.markup.html.form.Form.Disposition;

@SuppressWarnings("serial")
public class TextAreaField<T> extends Field<T> {
										
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TextAreaField.class.getName());
	
	private static final long serialVersionUID = 1L;
	private int rows = 0, cols = 0;
	
	public class ControlFragment extends Fragment {
		
		WebMarkupContainer sub_container;
		Label subtitle;
		
		protected void addHelpLink() {
			IModel<String> help = getHelpText();
			AjaxLink<Void>  hl = new AjaxLink<Void>("help-link") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					ControlFragment.this.get("help").setVisible( !ControlFragment.this.get("help").isVisible());
					target.add(TextAreaField.this);
				}
				public boolean isVisible() {
					return (getHelpText()!=null && getHelpText().getObject()!=null && getHelpText().getObject().length()>0); 
				}
			};
			add(hl);
			if (help!=null && help.getObject()!=null) {
				Label label=new Label ("help", help);
				label.setEscapeModelStrings(false);
				add(label);
			}
			else
				add((new Label ("help", "")).setVisible(false));
		}

		public ControlFragment(String id) {
			super(id, "control-fragment", TextAreaField.this);
			
			 sub_container = new  WebMarkupContainer("subtitle-container");
			 add(sub_container);
			 sub_container.setVisible(  getSubtitle()!=null );
			 subtitle = new Label("subtitle", getSubtitle());
			 subtitle.setEscapeModelStrings(false);
			 sub_container.add(subtitle);
			 
			TextArea<?> input  = getTextField();
			
			input.setEscapeModelStrings(false);
			input.setOutputMarkupId(true);
			
			input.add(new AjaxFormComponentUpdatingBehavior("change") {
				protected void onUpdate(AjaxRequestTarget target) {
			
					TextAreaField.this.onUpdate(target);
					
	 				if (TextAreaField.this.hasFeedback()) {
						TextAreaField.this.validate();
						target.add(TextAreaField.this);
					}
				}
				protected void onError(AjaxRequestTarget target, RuntimeException e) {
					target.add(TextAreaField.this);
				}
			});
			
			add(input);
			addHelpLink();
			add(getFeedback());
		}
		public T getValue() {
			return TextAreaField.this.getValue();
		}
		public void setValue(T value) {
			TextAreaField.this.setValue(value);
		}
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param id
	 */
	public TextAreaField(String id) {
		this(id, null, false, Width.W12, null, 0, 0);
	}
	
	public TextAreaField(String id, int rows, int cols) {
		this(id, null, false, Width.W12, null, rows, cols);
	}

	public TextAreaField(String id, boolean required) {
		this(id);
		setRequired(required);
	}
	
	public TextAreaField(String id, int rows, int cols, boolean required) {
		this(id, null, required, Width.W12, null, rows, cols);
	}
	
	
	public TextAreaField(String id, IModel<T> model, int rows, int cols) {
		this(id, model, false, Width.W12, null, rows, cols);
	}
	
	public TextAreaField(String id, IModel<T> model, int rows, int cols, boolean required) {
		this(id, model,  required, Width.W12, null, rows, cols);
	}
	
	public TextAreaField(String id, IModel<T> model) {
		this(id, model, false, Width.W12, null, 0, 0);
	}
	
	public TextAreaField(String id, IValidator<T> validator) {
		this(id, null, false, Width.W12, validator, 0, 0);
	}
	
	public TextAreaField(String id, IValidator<T> validator, int rows, int cols) {
		this(id, null, false, Width.W12, validator, rows, cols);
	}

	public TextAreaField(String id, IModel<T> model, boolean required, Width width, IValidator<T> validator, int rows, int cols) {
		super(id, model);
		
		setOutputMarkupId(true);
	
		this.rows = rows;
		this.cols = cols;

		setRequired(required);
		
		setWidth(width);
		
		if (model!=null) 
			setValue(model.getObject());
		
		if (validator!=null)
			add(validator);
	}

	public void setRows( int rows ) {
		this.rows=rows;
	}
	
	public void setCols( int cols ) {
		this.cols=cols;
	}
	
	public void onUpdate(AjaxRequestTarget target) {
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void updateModel() {
		if (getInput()==null) return;
		IModel<?> model = getInput().getDefaultModel();
		Object input = model.getObject();
		if (input!=null || (input==null&&getModel().getObject()!=null)) {
			if (getModel().getObject()!=null && !getModel().getObject().equals(input) || getModel().getObject()==null && input!=null) {
				getModel().setObject((T)input);
				onUpdate(getModel().getObject(), (T)input);
			}
		}
	}
	
	public IModel<String> getLabel() {
		return new StringResourceModel("property."+getProperty(), TextAreaField.this, null);	
	}
	
	public Component getInput() {
		if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
			return get("horizontal-layout:control:input");
		}
		else {
			return get("control:input");
		}
	}
	
	public boolean isHelpVisible() {
		return ((getHelpText()!=null) &&  (getHelpText().getObject().length()>0));
	}
	
	public boolean hasFeedback() {
		return super.hasFeedback();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		WebMarkupContainer lc = new WebMarkupContainer("label-container") {
			public boolean isVisible() {
				return isHelpInfo() || (getLabel()!=null && getLabel().getObject()!=null && getLabel().getObject().length()>0); 
			}
		};
		
		add(lc);
		
		Label label = new Label("label", getLabel()) {
			@Override
			public boolean isVisible() {
				return (getLabel()!=null && getLabel().getObject()!=null && getLabel().getObject().trim().length()>0); 
			}
		};
		
		label.setEscapeModelStrings(false);
			
		label.add(new AttributeModifier("for", new Model<String>() {
			public String getObject() {
				return getInput().getMarkupId();
			}
		}));
			
		label.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				if (getDisposition()==null||getDisposition()==Disposition.HORIZONTAL)
					return "col-lg-1 control-label";
					else
				return "";
			}
		}));
			
		lc.add(label);
			
		lc.add(new WebMarkupContainer("readonly") {
			public boolean isVisible() {
				return TextAreaField.this.isReadOnly();
			}
		});		
			
		lc.add(new WebMarkupContainer("mandatory") {
			public boolean isVisible() {
				return TextAreaField.this.isRequired();
			}
		});
		
		AjaxLink<Void> helpLink = new AjaxLink<Void>("help-info-circle") {
			public boolean isVisible() {
				return isHelpInfo();
			}
			@Override
			public void onClick(AjaxRequestTarget target) {
				onHelp(target);
			}
		};
		lc.add(helpLink);
		

	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("horizontal-layout")==null) {
			WebMarkupContainer layout = new WebMarkupContainer("horizontal-layout");
			layout.add(newControlFragment());
			add(layout);
			add(newControlFragment());
			if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
				layout.add(new AttributeModifier("class", Width.W10.getCss()));
				get("control").setVisible(false);
			}
			else {
				layout.setVisible(false);
			}
		}
	}

	protected boolean isInputEnabled() {
		return getEditor()!=null ? getEditor().isEditionEnabled() : true;
	}
	
	protected void onHelp(AjaxRequestTarget target) {}
	
	protected Fragment newControlFragment() {
		return new ControlFragment("control");
	}
	
	protected void onKey(AjaxRequestTarget target, String jsKeycode) {
		
	}
	
	protected void onUpdate(T oldvalue, T newvalue) {
		if (getEditor()!=null) {
			getEditor().setUpdatedPart(getPart());
		}
	}

	protected TextArea<?> getTextField() {
		
		TextArea<?> input = new TextArea<T>("input", new PropertyModel<T>(this, "value")) {
			@Override
			public void validate() {
				TextAreaField.this.validate();
				super.validate();
			}
			@Override
			public boolean isEnabled() {
				return isInputEnabled();
			}
		};
		
		if (getTabIndex()>0)
			input.add(new AttributeModifier("tabindex", getTabIndex()));

		if (this.rows>0) {
			input.add(new AttributeModifier("rows", rows));
		}
		
		if (this.cols>0) {
			input.add(new AttributeModifier("cols", cols));
		}
		
		return input;
	}
	
	protected String getPart() {
		String s = ((Label)get("label-container:label")).getDefaultModelObjectAsString();
		return (s!=null ? s.toLowerCase() : "");
	}
} 