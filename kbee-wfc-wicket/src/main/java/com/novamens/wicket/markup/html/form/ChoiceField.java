 package com.novamens.wicket.markup.html.form;

import java.util.List;


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.value.IValueMap;
import org.apache.wicket.validation.IValidator;

import com.novamens.kbee.wicket.markup.html.behaviour.KeyboardBehavior;
import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.security.Identifiable;
import com.novamens.wicket.markup.html.form.Form.Disposition;

@SuppressWarnings("serial")
public class ChoiceField<T> extends Field<T> {
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ChoiceField.class.getName());
	
	IModel<List<T>> choices;
	boolean islabelvisible = false;
	
	public class ControlFragment extends Fragment {
		
		WebMarkupContainer sub_container;
		Label subtitle;
		
		public ControlFragment(String id, IModel<List<T>> choices) {
			super(id, "control-fragment", ChoiceField.this);
			
			setOutputMarkupId(true);
			
			sub_container = new  WebMarkupContainer("subtitle-container");
			add(sub_container);
			
			sub_container.setVisible(  getSubtitle()!=null  &&getSubtitle().getObject()!=null && getSubtitle().getObject().length()>0);
			subtitle = new Label("subtitle", getSubtitle());
			subtitle.setEscapeModelStrings(false);
			sub_container.add(subtitle);
						
			DropDownChoice<T> selector = new DropDownChoice<T>("input", choices) {
				@Override
				public boolean isEnabled() {
					return isInputEnabled();
				}
				@Override
				public void validate() {
					super.validate();
					ChoiceField.this.validate();
				}
				@Override
				protected String getNullValidDisplayValue() {
					return ChoiceField.this.getNullValidDisplayValue();
				}
				@Override
				public boolean isNullValid() {
					return ChoiceField.this.isNullValid();
				}
				@Override
				public boolean isRequired()	{
					return false;
				}
				@Override
				protected void onComponentTag(final ComponentTag tag) {
					IValueMap attributes = tag.getAttributes();
					if (autofocus())
						attributes.putIfAbsent("autofocus", "");
					super.onComponentTag(tag);
				}
			};
			
			selector.setModel(new PropertyModel<T>(this, "value"));
			selector.setChoiceRenderer(new ChoiceRenderer<T>() {
				public String getIdValue(T value, int index) {
					return ChoiceField.this.getIdValue(value); 
				};
				public String getDisplayValue(T value) {
					return ChoiceField.this.getDisplayValue(value);
				};
			});
			add(selector);
			
			
			if (getTabIndex()>0)
				selector.add(new AttributeModifier("tabindex", getTabIndex()));

			
			selector.setRequired(ChoiceField.this.isRequired());
			
			selector.add(new AjaxFormComponentUpdatingBehavior("change") {
				protected void onUpdate(AjaxRequestTarget target) {
					ChoiceField.this.onUpdate(target);
				}
			});
			
			selector.add(new KeyboardBehavior() {
				protected void onKey(AjaxRequestTarget target, String jsKeycode) {
					ChoiceField.this.onKey(target, jsKeycode);
				}
			});
			

			
			addHelpLink();
			
			add(getFeedback());
			
		}
		public T getValue() {
			return ChoiceField.this.getValue();
		}
		
		public void setValue(T value) {
			ChoiceField.this.setValue(value);
		}
		
		protected void addHelpLink() {
			IModel<String> help = getHelpText();
			AjaxLink<T>  hl = new AjaxLink<T>("help-link-choice", ChoiceField.this.getModel()) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					islabelvisible = !islabelvisible;
					target.add(ChoiceField.this);
				}
				public boolean isVisible() {
					return isHelpVisible(); 
				}
			};
			Label la=new Label("helpstr", new StringResourceModel("helplabel", ChoiceField.this, null));
			la.setEscapeModelStrings(false);
			hl.add(la);
			add(hl);
			hl.setVisible(false);
			if (help!=null && help.getObject()!=null) {
				Label label=new Label ("help", help) {
					public boolean isVisible() {
						return islabelvisible;
					}
				};
				label.setEscapeModelStrings(false);
				add(label);
			}
			else
				add((new Label ("help", "")).setVisible(true));
		}
	}


	/** -------------
	 * 
	 * 
	 * @param id
	 */
	public ChoiceField(String id) {
		this(id, null);
	}
	
	public ChoiceField(String id, IModel<List<T>> choices) {
		this(id, null, choices, false);
	}
	
	public ChoiceField(String id, IModel<List<T>> choices, boolean required) {
		this(id, null, choices, required);
	}
	
	public ChoiceField(String id, IModel<T> model, IModel<List<T>> choices) {
		this(id, model, choices, false);
	}
	
	public ChoiceField(String id, IModel<T> model, IModel<List<T>> choices, boolean required) {
		super(id, model);
		setOutputMarkupId(true);
		setRequired(required);
		setChoices(choices);
		if (model!=null)
			setValue(model.getObject());
	}

	public ChoiceField(String id, IModel<T> model, IModel<List<T>> choices, IValidator<T> validator) {
		super(id, model);
		setOutputMarkupId(true);
		add(validator);
		setChoices(choices);
		if (model!=null)
			setValue(model.getObject());
	}
			
	public IModel<List<T>> getChoices() {
		return choices;
	}
	
	public void setChoices(IModel<List<T>> choices) {
		this.choices=choices;
	}

	
	public String getNullValidDisplayValue() {
		return "None";
	}
	
	public void onUpdate(AjaxRequestTarget target) {
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void updateModel() {
		if (getInput()==null) {
			return;
		}
		IModel<?> model = getInput().getDefaultModel();
		Object input = model.getObject();
		if (input!=null) {
			if (getModel().getObject()!=null && !equals(getModel().getObject(),input) || getModel().getObject()==null && input!=null) {
				onUpdate(getModel().getObject(), (T)input);
				try {
					getModel().setObject((T)input);
				}
				catch(Exception e) {
					logger.error(e);
				}
			}
		}
		else {
			if (getModel().getObject()!=null) {
				onUpdate(getModel().getObject(), null);
				getModel().setObject(null);
			}
		}
	}
	
	public Component getInput() {
		if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
			return get("horizontal-layout:control:input");
		}
		else {
			return get("control:input");
		}
	}
	
	@SuppressWarnings("unchecked")
	public void setFieldValue(String value) {
		String[] values = { value };
		((DropDownChoice<T>)getInput()).setModelValue(values);
	}
	
	public IModel<String> getLabel() {
		try {
			return new StringResourceModel("property."+getProperty(), ChoiceField.this, null);
		} 
		catch (java.util.MissingResourceException e) {
			return new Model<String>(getProperty());
		} 
		catch (Exception e2) {
			return new Model<String>(getProperty());
		}
	}
	
	public boolean isHelpVisible() {
		return (getHelpText()!=null && getHelpText().getObject()!=null && getHelpText().getObject().length()>0); 
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (getChoices()==null)
			throw new NullPointerException("choices is null");
		
		Label label = new Label("label", getLabel());
		label.setEscapeModelStrings(false);
		
		label.add(new AttributeModifier("for", new Model<String>() {
			public String getObject() {
				return getInput().getMarkupId();
			}
		}));
		
		WebMarkupContainer lc = new WebMarkupContainer("label-container") {
			public boolean isVisible() {
				return  getLabel()!=null && 
						getLabel().getObject()!=null 
						&& getLabel().getObject().length()>0; 
			}
		};
		
		add(lc);
		lc.add(label);
		

		lc.add(new WebMarkupContainer("readonly") {
			public boolean isVisible() {
				return ChoiceField.this.isReadOnly();
			}
		});		
		
		/**
		 * Help info is the (i) icon that opens a InfoDialog
		 * 
		 */
		AjaxLink<Void> helpLink = new AjaxLink<Void>("help-info") {
			public boolean isVisible() {
				return isHelpInfo();
			}
			public boolean isEnabled() {
				return true;
			}
			@Override
			public void onClick(AjaxRequestTarget target) {
				onHelp(target);
			}
		};
		
		add(helpLink);
		
		lc.add(new WebMarkupContainer("mandatory") {
			public boolean isVisible() {
				return ChoiceField.this.isRequiredMark();
			}
		});
	}
	
	@Override
	public void onAfterRender() {
		super.onAfterRender();
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("horizontal-layout")==null) {
			WebMarkupContainer layout = new WebMarkupContainer("horizontal-layout");
			layout.add(new ControlFragment("control", choices));
			add(layout);
			add(new ControlFragment("control", choices));
			if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
				get("control").setVisible(false);
			}
			else {
				layout.setVisible(false);
			}
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (this.choices!=null)
			this.choices.detach();
	}
	
	protected void onUpdate(T oldvalue, T newvalue) {
		if (getEditor()!=null) {
			getEditor().setUpdatedPart(getPart());
		}
	}
	
	protected void onHelp(AjaxRequestTarget target) {
	}

	protected void onKey(AjaxRequestTarget target, String jsKeycode) {
		
	}
	
	protected boolean isInputEnabled() {
		return getEditor()!=null ? getEditor().isEditionEnabled() : true;
	}
	
	protected String getIdValue(T value) {
		if (value==null)
			return null;

		String id = null;

		if (value instanceof Identifiable && ((Identifiable)value).getId()!=null)
			id = ((Identifiable)value).getId().toString();
		else {
			id = DisplayNameExtractor.get(value).toLowerCase();
		}	
		return id;
	}
	
	protected String getPart() {
		return ((Label)get("label-container:label")).getDefaultModelObjectAsString().toLowerCase();
	}
	
	protected String getDisplayValue(T value) {
		return DisplayNameExtractor.get(value);
	}
	
	protected boolean equals(T value, Object object) {
		return value!=null && object!=null && value.equals(object);
	}
}