package com.novamens.wicket.markup.html.form;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.value.IValueMap;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.MarkupContainer;

import com.novamens.kbee.wicket.markup.html.behaviour.KeyboardBehavior;
import com.novamens.wicket.markup.html.form.Form.Disposition;

@SuppressWarnings("serial")
public class TextField<T> extends Field<T> {
			
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TextField.class.getName());
	
	private IModel<String> mlabel;
	private boolean placeholderLabel = false;
	private boolean listenEnter;
	boolean centered = false;
	
	public class ControlFragment extends Fragment {
		
		WebMarkupContainer sub_container;
		Label subtitle;
		
		
		private AjaxLink<Void> help_link;
		
		public ControlFragment(String id, MarkupContainer markupProvider) {
			super(id, "control-fragment", markupProvider);
			
			 sub_container = new  WebMarkupContainer("subtitle-container");
			 add(sub_container);
			 sub_container.setVisible(  getSubtitle()!=null );
			 subtitle = new Label("subtitle", getSubtitle());
			 subtitle.setEscapeModelStrings(false);
			 sub_container.add(subtitle);
			 
			org.apache.wicket.markup.html.form.TextField<?> input = newTextField(); 
			
			if (getPlaceHolder()!=null && getPlaceHolder().getObject()!=null)
				input.add( new AttributeModifier("placeholder", getPlaceHolder()));
			
			input.add(new AjaxFormComponentUpdatingBehavior("change") {
				protected void onUpdate(AjaxRequestTarget target) {
					TextField.this.onUpdate(target);
	 				if (TextField.this.hasFeedback()) {
						TextField.this.validate();
						target.add(TextField.this);
					}
				}
				protected void onError(AjaxRequestTarget target, RuntimeException e) {
					target.add(TextField.this);
				}
			});
			
			input.add(new KeyboardBehavior(listenEnter()) {
				protected void onKey(AjaxRequestTarget target, String jsKeycode) {
					TextField.this.onKey(target, jsKeycode);
				}
			});
			
			
			if (getTabIndex()>0)
				input.add(new AttributeModifier("tabindex", getTabIndex()));

			add(input);
			
			IModel<String> help = getHelpText();
			
			help_link = new AjaxLink<Void>("help-link") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					ControlFragment.this.get("help").setVisible( !ControlFragment.this.get("help").isVisible());
					target.add(TextField.this);
				}
				public boolean isVisible() {
					return TextField.this.isHelpVisible(); 
				}
			};
			
			

			
			Label la=new Label("helpstr", getHelpLinkTitle());
			la.setEscapeModelStrings(false);
			help_link.add(la);
			add(help_link);
			
			
			if (help!=null && help.getObject()!=null) {
				
				Label label=new Label ("help", help);
				label.setEscapeModelStrings(false);
				label.setVisible(false);
				add(label);
			}
			else
				add((new Label ("help", "")).setVisible(false));
			

			add(getFeedback());
			add(getInfo());
		}
		public T getValue() {
			return TextField.this.getValue();
		}
		
		public void setValue(T value) {
			TextField.this.setValue(value);
		}
	}
	
	
	/**
	 * 
	 * 
	 * 
	 * @param id
	 */
	public TextField(String id) {
		this(id, null, false, Width.W12, null);
	}
	
	public IModel<String> getHelpLinkTitle() {
		return new StringResourceModel("help", TextField.this, null);
	}

	public TextField(String id, Width width) {
		this(id, null, false, width, null);
	}
	
	public TextField(String id, boolean required) {
		this(id, null, required, Width.W12, null);
	}
	
	public TextField(String id, boolean required, IValidator<T> validator) {
		this(id, null, required, Width.W12, validator);
	}
	
	public TextField(String id, IModel<T> model) {
		this(id, model, false, Width.W12, null);
	}
	
	public TextField(String id, IModel<T> model, boolean required) {
		this(id, model, required, Width.W12, null);
	}
	
	public TextField(String id, IModel<T> model, boolean required, IValidator<T> validator) {
		this(id, model, required, Width.W12, validator);
	}
	
	public TextField(String id, IModel<T> model, boolean required, Width width, IValidator<T> validator) {
		super(id, model);
		
		setOutputMarkupId(true);
		setRequired(required);
		setWidth(width);
		if (model!=null) 
			setValue(model.getObject());
		if (validator!=null)
			add(validator);
		
		Label label;
		
		WebMarkupContainer lc = new WebMarkupContainer("label-container") {
			public boolean isVisible() {
				if (isPlaceholderLabel())
					return false;
				return getLabel()!=null && getLabel().getObject()!=null && getLabel().getObject().length()>0; 
			}
		};
		
		add(lc);
	
		
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
	

		
		if (getLabel()!=null) {
			label = new Label("label", getLabel()) {
				public boolean isVisible() {
					return getLabel()!=null && getLabel().getObject()!=null && getLabel().getObject().length()>0;
				}
			};
			
			label.setEscapeModelStrings(false);
			
			label.add(new AttributeModifier("for", new Model<String>() {
				public String getObject() {
					return getInput()!=null ? getInput().getMarkupId() : null;
				}
			}));
			
			lc.add(label);
			
			lc.add(new WebMarkupContainer("mandatory") {
				public boolean isVisible() {
					return TextField.this.isRequiredMark();
				}
			});	

		}
		else { 
			
			lc.add((new Label("label", ""    )).setVisible(false));
			lc.add((new Label("mandatory", "")).setVisible(false));
		}
		
		
		lc.add(new WebMarkupContainer("readonly") {
			public boolean isVisible() {
				return TextField.this.isReadOnly();
			}
		});		
	}

	

	public void setCentered(boolean c) {
		this.centered=c;	
	}
	
	public boolean isCentered() {
		return centered;
	}
	
	public boolean isHelpVisible() {
		return ((getHelpText()!=null) &&  (getHelpText().getObject().length()>0));
	}
	
	public void setHelpVisible() {
		if (isHelpVisible() && getControl().get("help")!=null) {
			getControl().get("help").setVisible(true);
		}
	}
	
	public boolean isPlaceholderLabel() {
		return placeholderLabel;
	}

	public void setPlaceholderLabel(boolean placeholderLabel) {
		this.placeholderLabel = placeholderLabel;
	}
	
	public IModel<String> getPlaceHolder() { 
		if (isPlaceholderLabel())
			return this.getLabel();
		return null;
	}
	
	public void onUpdate(AjaxRequestTarget target) {
	}
	
	public void onBlur(AjaxRequestTarget target) {
	}

	@Override
	@SuppressWarnings("unchecked")
	public void updateModel() {

		Object input = null;
		
		try {
			
			if (getInput()==null)
				return;
			
			input = getInputValue();
			
			if (!isInputEnabled())
				return;
			
			if (getModel()==null)
				return;

			
			if (input!=null) {
				if (getModel().getObject()!=null && 
						!getModel().getObject().equals(input) || 
						getModel().getObject()==null && 
						input!=null && !"".equals(input)) {
					
					onUpdate(getModel().getObject(), (T)input);
					
					getModel().setObject((T)input);
				}
			}
			else {
				if (getModel().getObject()!=null) {
					getModel().setObject(null);
					onUpdate(getModel().getObject(), null);
				}
			}
		} 
		catch (Exception e) {
			logger.error(e,  input!=null?input.toString(): "");
			getModel().detach();
		}
	}
 
	public IModel<String> getPlaceHolderLabel() {
		try {										
			return new StringResourceModel("property." + getProperty() + ".placeholder", TextField.this, null);
		} 
		catch (java.util.MissingResourceException e) {
			return null;
		} 
		catch (Exception e2) {
			logger.error(e2);
			return null;
		}
	}
		
	public IModel<String> getLabel() {
		try {
			if (this.mlabel!=null)
				return this.mlabel;
			IModel<String> model = new StringResourceModel("property."+getProperty(), TextField.this, null);
			return model;
		} 
		catch (java.util.MissingResourceException e) {
			return null;
		} 
		catch (Exception e2) {
			logger.error(e2);
			return null;
		}
	}

	public void setLabel(IModel<String> ml) {
		
		this.mlabel=ml;
		
		if (get("label-container:label")!=null) {
			
			Label label = new Label("label", getLabel()) {
					public boolean isVisible() {
						return getLabel()!=null && getLabel().getObject()!=null && getLabel().getObject().length()>0;
					}
				};
				
				label.add(new AttributeModifier("for", new Model<String>() {
					public String getObject() {
						return getInput().getMarkupId();
					}
				}));
				
				label.add(new AttributeModifier("class", new Model<String>() {
					public String getObject() {
						return getDisposition()==null||getDisposition()==Disposition.HORIZONTAL ? "col-lg-1 control-label" : "control-label";
					}
				}));
				
				((WebMarkupContainer) get("label-container")).addOrReplace(label);
				((WebMarkupContainer) get("label-container")).addOrReplace(new WebMarkupContainer("mandatory") {
					public boolean isVisible() {
						return TextField.this.isRequired();
					}
				});	
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

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (get("horizontal-layout")==null) {
			
 			WebMarkupContainer layout = new WebMarkupContainer("horizontal-layout");
			layout.add(newControlFragment());
			
			add(layout);
			add(newControlFragment());
			
			if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
				get("control").setVisible(false);
			}
			else {
				layout.setVisible(false);
			}
			
			addBehaviors();
		}
	}
	
	public boolean listenEnter() {
		return listenEnter;
	}
	
	public void listenEnter(boolean value) {
		listenEnter = value;
	}
	
	protected void onHelp(AjaxRequestTarget target) {
	}
	
	protected Fragment newControlFragment() {
		ControlFragment fr = new ControlFragment("control", TextField.this);
		StringBuilder str = new StringBuilder();
		
		str.append(infovisible);

		if (isCentered())
			str.append("container-panel-centered ");
		else
			str.append("container-panel ");
		
		if (isHelpVisible())
			str.append("helpvisible");
		
		fr.add(new AttributeModifier("class", str.toString()));
		return fr;
	}
	
	protected Component getControl() {
		if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
			return get("horizontal-layout:control");
		}
		else {
			return get("control");
		}
	}
	
	protected boolean isInputEnabled() {
		return getEditor()!=null ? getEditor().isEditionEnabled() : true;
	}
	
	protected org.apache.wicket.markup.html.form.TextField<?> newTextField() {
		
		org.apache.wicket.markup.html.form.TextField<T>
		
		input = new org.apache.wicket.markup.html.form.TextField<T>("input", new PropertyModel<T>(this, "value")) {
			@Override
			public void validate() {
				TextField.this.validate();
				super.validate();
			}
			@Override
			public boolean isEnabled() {
				return isInputEnabled();
			}
			protected void onComponentTag(final ComponentTag tag) {
				IValueMap attributes = tag.getAttributes();
 				
				if (getInputType()!=null)			
					attributes.put("type",  getInputType());
				else
					attributes.put("type",  "text");
					
 				if (getAutoComplete()!=null)
					attributes.put("autocomplete", getAutoComplete());
				
				if (autofocus())
					attributes.putIfAbsent("autofocus", "");

				super.onComponentTag(tag);
			}

			@Override
			public String getInputName() {
				String overridedName = TextField.this.getInputName();
				if(overridedName != null)
					return overridedName;

				return super.getInputName();
			}
		};

		input.setOutputMarkupId(true);
		
		if (getTabIndex()>0)
			input.add(new AttributeModifier("tabindex", getTabIndex()));
		
		try {
			if (getPlaceHolderLabel()!=null && getPlaceHolderLabel().getObject()!=null) 
				input.add(new AttributeModifier("placeholder", getPlaceHolderLabel()));
		}
		catch (java.util.MissingResourceException e) {
			logger.debug(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName()+ " |  id. " + TextField.this.getId());
		}
			
		return input;
	}
	
	protected void onKey(AjaxRequestTarget target, String jsKeycode) {
		
	}
	
	protected void onUpdate(T oldvalue, T newvalue) {
		if (getEditor()!=null) {
			getEditor().setUpdatedPart(getPart());
		}
	}
	
	protected String getPart() {
		return ((Label)get("label-container:label")).getDefaultModelObjectAsString().toLowerCase();
	}
	
	protected String getInputType() {
		return "text";
	}

	protected String getInputName() {
		return null;
	}
	
	protected String getAutoComplete() {
		return null;
	}
} 