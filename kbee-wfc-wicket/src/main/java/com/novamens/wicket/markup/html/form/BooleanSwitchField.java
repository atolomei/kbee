package com.novamens.wicket.markup.html.form;
 
import java.util.MissingResourceException;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

 import com.novamens.wicket.markup.html.form.Form.Disposition;

@SuppressWarnings("serial")
public class BooleanSwitchField extends Field<Boolean> {
	private static final long serialVersionUID = 1L;
	
	private Object [] parameters;

	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(BooleanSwitchField.class.getName());
	
	public String TRUE;
	public String FALSE;
	
	private Boolean selected = Boolean.valueOf(false);
	private boolean isborder;
	private boolean islabel = true;
	private Boolean is_enabled = null;
	
	public class ControlFragment extends Fragment {
		
		WebMarkupContainer sub_container;
		Label subtitle;
		
		public ControlFragment(String id) {
			super(id, "control-fragment", BooleanSwitchField.this);
			
			 sub_container = new  WebMarkupContainer("subtitle-container");
			 add(sub_container);
			 sub_container.setVisible(  getSubtitle()!=null );
			 subtitle = new Label("subtitle", getSubtitle());
			 subtitle.setEscapeModelStrings(false);
			 sub_container.add(subtitle);
			 
			 WebMarkupContainer cc= new WebMarkupContainer("control-container");
			 add(cc);
			
			 cc.add(new AttributeModifier("class", isBorder() ? "control-container control-container-border" : "control-container control-container-plain"));
			
			 AjaxLink<Void> sw = new AjaxLink<Void>("switch-link") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					setSelected(!isSelected());
					target.add(BooleanSwitchField.this);
					BooleanSwitchField.this.onUpdate(target);
				}
				@Override
				public boolean isEnabled() {
						return BooleanSwitchField.this.isEnabled();
				}
			};
 			
			sw.add(new AttributeModifier("title", new Model<String>() {
				public String getObject() {
					return getStringValue();
				}
			}));
			
			if (!isLabel()) {
				sw.add(new AttributeModifier("style", "margin-top:0;"));
			}
			
			sw.add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					StringBuilder str = new StringBuilder();
					
					if (getHelpText()!=null && getHelpText().getObject()!=null) {
						if (!isBorder())
							str.append(" mt25 ");
					}
					
					str.append( isSelected()?" switch ":" switch ");
					str.append(BooleanSwitchField.this.isEnabled()?"": " isDisabled");
					return str.toString();
				}
			}));

			cc.add(sw);
			
			
			if (getTabIndex()>0)
				cc.add(new AttributeModifier("tabindex", getTabIndex()));

			
			WebMarkupContainer swic= new WebMarkupContainer("switch");
			
			swic.add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					return isSelected()?"switch-on fas fa-circle":"switch-off fal fa-circle";
				}
			}));
			
			sw.add(swic);


 			
			IModel<String> help = getHelpText();
			
			if (help!=null && help.getObject()!=null)
				cc.add( (new Label ("help", help)).setEscapeModelStrings(false));
			else
				cc.add((new Label ("help", "")).setVisible(false));

			cc.add(getFeedback());
			
			IModel<String> help2 = getText();
			if (help2!=null && help2.getObject()!=null)
				add( (new Label ("help2", help2)).setEscapeModelStrings(false));
			else
				add((new Label ("help2", "")).setVisible(false));
			
		}
		
	}
	
	public BooleanSwitchField(String id) {
		this(id, null, null);
	}
	

	public BooleanSwitchField(String id, IModel<Boolean> model) {
		this(id, model, null);
	}
	

	public BooleanSwitchField(String id, IModel<Boolean> model, Object[] parameters) {
		this(id, model, null, parameters);
	}

	
	public BooleanSwitchField(String id, IModel<Boolean> model, IModel<String> labelmodel, Object[] parameters) {
		super(id, model);
		
		setOutputMarkupId(true);
		this.parameters=parameters;
		TRUE  = getTrueStr();
		FALSE = getFalseStr();

		if (model!=null)
			setValue(model.getObject());

		if(labelmodel == null){
			labelmodel = new StringResourceModel("property."+getProperty(), BooleanSwitchField.this);
			((StringResourceModel)labelmodel).setParameters(parameters);
		}
		
		WebMarkupContainer labelcontainer = new WebMarkupContainer("label-container") {
			public boolean isVisible() {
				return isLabel();
			}
		};

		Label label = new Label("label", labelmodel); 
		label.setEscapeModelStrings(false);
		label.add(new AttributeModifier("for", id));
		labelcontainer.add(label);
		add(labelcontainer);

		label.add(new AttributeModifier("class", new Model<String>() {
			private static final long serialVersionUID = 1L;
			public String getObject() {
				return getDisposition()==null||getDisposition()== Disposition.HORIZONTAL ? "col-lg-2 control-label" : "control-label";
			}
		}));
	}


	public Object [] getParameters() {
		return this.parameters;
	}

	public void onUpdate(AjaxRequestTarget target) {
	}

	public boolean isBorder() {
		return this.isborder;
	}
	
	public void setBorder(boolean b) {
		this.isborder=b;
	}
	
	public boolean isLabel() {
		return this.islabel;
	}
	
	public void setLabel(boolean b) {
		this.islabel=b;
	}
	
	@Override
	public void updateModel() {

		if (getModel().getObject()==null || 
			!getModel().getObject().equals(getValue())) {

			getModel().setObject(getValue());
				
				if (getEditor()!=null)  
					getEditor().setUpdatedPart(((Label)get("label-container:label")).getDefaultModelObjectAsString());
		}
	}

	
	@Override
	public Boolean getValue() {
		return this.selected;
	}
	

	@Override
	public void setValue(Boolean value) {
		this.selected=value;
	}

	public boolean isSelected() {
		return selected.booleanValue();
	}

	public void setSelected(boolean b) {
		selected=b;
	}
	
	@Override
	public boolean isEnabled() {
		
		if (this.is_enabled!=null)
			return this.is_enabled.booleanValue();
		
		return getEditor()!=null ? getEditor().isEditionEnabled() : true;
	}
	
	public String getStringValue() {
		return getValue()!=null ? (getValue().booleanValue() ? TRUE : FALSE) : null;
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

	protected IModel<String> getText() {
 		IModel<String> model = new StringResourceModel(getProperty()+".text", BooleanSwitchField.this, null);
		try {
			if (model!=null && model.getObject()!=null)
				return model;
			return null;
		}
		catch (MissingResourceException e) {
			return null;
		}
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
