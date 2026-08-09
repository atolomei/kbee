package com.novamens.wicket.markup.html.form;

import java.util.ArrayList;
import java.util.List;


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;


import com.novamens.wicket.markup.html.form.Form.Disposition;

@SuppressWarnings("serial")
public class BooleanField extends Field<Boolean> {
	private static final long serialVersionUID = 1L;
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(BooleanField.class.getName());
	
	
	private String value;
	private Object [] parameters;
	
	private Boolean BT = Boolean.valueOf(true);
	private Boolean BF = Boolean.valueOf(false);
	
	public String TRUE;
	public String FALSE;
	
	
	public class ControlFragment extends Fragment {
	
		WebMarkupContainer sub_container;
		Label subtitle;
		
		
		protected void addHelpLink() {
			IModel<String> help = getHelpText();
			AjaxLink<Void>  hl = new AjaxLink<Void>("help-link") {
				@Override
				public void onClick(AjaxRequestTarget target) {
						ControlFragment.this.get("help").setVisible( !ControlFragment.this.get("help").isVisible());
						target.add(BooleanField.this);
				}
				public boolean isVisible() {
					return (getHelpText()!=null && getHelpText().getObject()!=null && getHelpText().getObject().length()>0); 
				}
			};
			
			hl.add(new Label("helpstr", new StringResourceModel("help", BooleanField.this, null)));
			add(hl);
			if (help!=null && help.getObject()!=null) {
				Label label=new Label ("help", help);
				label.setEscapeModelStrings(false);
				label.setVisible(false);
				add(label);
			}
			else
				add((new Label ("help", "")).setVisible(false));
		}
		
		
		public ControlFragment(String id) {
			super(id, "control-fragment", BooleanField.this);
			
			
			 sub_container = new  WebMarkupContainer("subtitle-container");
			 add(sub_container);
			 sub_container.setVisible(  getSubtitle()!=null );
			 subtitle = new Label("subtitle", getSubtitle());
			 subtitle.setEscapeModelStrings(false);
			 sub_container.add(subtitle);
			
			DropDownChoice<String> selector = new DropDownChoice<String>("input", getChoices()) {
				@Override
				public boolean isEnabled() {
					return isInputEnabled();
				}
			};
			
			selector.setModel(new PropertyModel<String>(this, "stringValue"));
			selector.setChoiceRenderer(new ChoiceRenderer<String>() {
				public String getIdValue(String value, int index){ 
					return value; 
				};
				public String getDisplayValue(String value) {
					return BooleanField.this.getDisplayValue(value);
				};
			});
			add(selector);
			
			if (getTabIndex()>0)
				selector.add(new AttributeModifier("tabindex", getTabIndex()));

			
			selector.add(new AjaxFormComponentUpdatingBehavior("change") {
				protected void onUpdate(AjaxRequestTarget target) {
					BooleanField.this.onUpdate(target);
				}
			});
			
			/**IModel<String> help = getHelpText();
			
			if (help!=null && help.getObject()!=null)
				add( (new Label ("help", help)).setEscapeModelStrings(false));
			else
				add((new Label ("help", "")).setVisible(false));
				**/
			
			addHelpLink();

			
			add(getFeedback());
		}
		public void setStringValue(String value) {
			BooleanField.this.setStringValue(value);
		}
		public String getStringValue() {
			return BooleanField.this.getStringValue();
		}
	}
	
	
	/***
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param id
	 */
	public BooleanField(String id) {
		this(id, null, null);
	}
	
	public BooleanField(String id, IModel<Boolean> model) {
		this(id, model, null);
	}
	
	public BooleanField(String id, IModel<Boolean> model, Object[] parameters) {
		super(id, model);
		
		setOutputMarkupId(true);
		
		this.parameters=parameters;

		TRUE  = getTrueStr();
		FALSE = getFalseStr();
		
		if (model!=null)
			setValue(model.getObject());
		
 		Label label = new Label("label", parameters==null?getLabel():getLabel(parameters));
		label.setEscapeModelStrings(false);
		label.add(new AttributeModifier("for", id));
		add(label);
		
		
		add(new WebMarkupContainer("readonly") {
			public boolean isVisible() {
				return BooleanField.this.isReadOnly();
			}
		});		
		
		AjaxLink<Void> helpLink = new AjaxLink<Void>("help-info") {
			public boolean isVisible() {
				return isHelpInfo();
			}
			@Override
			public void onClick(AjaxRequestTarget target) {
				onHelp(target);
			}
		};

		add(helpLink);

		
		label.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				return getDisposition()==null||getDisposition()==Disposition.HORIZONTAL ? "col-lg-1 control-label" : "control-label";
			}
		}));
	}
	
	
	public Object [] getParameters() {
		return this.parameters;
	}

	public void onUpdate(AjaxRequestTarget target) {
	}
	

	@Override
	public void updateModel() {
		
		if (getInput()==null) 
			return;
			
		IModel<?> model = getInput().getDefaultModel();
		Object input = model.getObject();
		if (input!=null) {
			Boolean value = input.equals(TRUE) ? true : false;
			if (getModel().getObject()!=null && !getModel().getObject().equals(value) || getModel().getObject()==null && input!=null) {
				onUpdate(getModel().getObject(), value);
//				if (getEditor()!=null) 
//					getEditor().setUpdatedPart(((Label)get("label")).getDefaultModelObjectAsString().toLowerCase() + " (" + getValue().toString()+")");
//				logger.debug(getId() + " -> " + getValue().toString());
				getModel().setObject(getValue());
			}
		}
	}

 
	@Override
	public Boolean getValue() {
		return this.value!=null ? (this.value.equals(TRUE) ? BT : BF ) : null;
	}
	

	@Override
	public void setValue(Boolean value) {
		if (value==null) 
			this.value=null;
				else
		this.value = value.booleanValue() ? TRUE : FALSE;
	}


	public void setStringValue(String value) {
		this.value = value;
	}
	
	public String getStringValue() {
		return getValue()!=null ? (getValue() ? TRUE : FALSE) : null;
	}


	List<String> choices;
	
	public List<String> getChoices() {
		
		if (choices!=null)
			return choices;
		
		choices = new ArrayList<String>();
		choices.add(TRUE);
		choices.add(FALSE);
		
		return choices;
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
			layout.add(new ControlFragment("control"));
			add(layout);
			
			add(new ControlFragment("control"));
			if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
				get("control").setVisible(false);
			}
			else {
				layout.setVisible(false);
			}
		}
	}
	
	public IModel<String> getLabel(Object[] parameters) {
		try {
			StringResourceModel model = new StringResourceModel("property."+getProperty(), BooleanField.this, null);
			model.setParameters(parameters);
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
	
	public IModel<String> getLabel() {
		try {
			IModel<String> model = new StringResourceModel("property."+getProperty(), BooleanField.this, null);
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
	
	protected boolean isInputEnabled() {
		return getEditor()!=null ? getEditor().isEditionEnabled() : true;
	}
	
	protected void onUpdate(Boolean oldvalue, Boolean newvalue) {
		if (getEditor()!=null) {
			getEditor().setUpdatedPart(((Label)get("label")).getDefaultModelObjectAsString().toLowerCase() + " (" + getValue().toString()+")");
		}
	}

	protected void onHelp(AjaxRequestTarget target) {
	}
	
	protected String getFalseStr() {
		return new StringResourceModel("no", this, null).getString();
	}

	protected String getTrueStr() {
		return new StringResourceModel("yes", this, null).getString();
	}
	
	protected String getDisplayValue(String value) {
		return value;
	}
}
