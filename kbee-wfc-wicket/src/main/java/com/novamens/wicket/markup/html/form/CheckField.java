package com.novamens.wicket.markup.html.form;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.wicket.markup.html.form.Form.Disposition;

@SuppressWarnings("serial")
public class CheckField extends Field<Boolean> {
			
	//private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(CheckField.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	protected static final ResourceReference AW = new CssResourceReference(Form.class, Form.FONTAWESOME );
	protected static final ResourceReference BL = new CssResourceReference(Form.class, "build.css");

	public class ControlFragment extends Fragment {
		
		WebMarkupContainer sub_container;
		Label subtitle;
		
		public ControlFragment(String id) {
			super(id, "control-fragment", CheckField.this);
			

			
			 sub_container = new  WebMarkupContainer("subtitle-container");
			 add(sub_container);
			 sub_container.setVisible(  getSubtitle()!=null );
			 subtitle = new Label("subtitle", getSubtitle());
			 subtitle.setEscapeModelStrings(false);
			 sub_container.add(subtitle);
			 
			
			
			AjaxCheckBox selector = new AjaxCheckBox("input", new PropertyModel<Boolean>(CheckField.this, "value")) {
				@Override
				public boolean isEnabled() {
					return getEditor()!=null ? getEditor().isEditionEnabled() : true;
				}
				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					CheckField.this.onUpdate(target);
				}
			};
			
			if (getTabIndex()>0)
				selector.add(new AttributeModifier("tabindex", getTabIndex()));

			
			IModel<String> help = getHelpText();
			
			if (help!=null && help.getObject()!=null)
				add(new Label ("help", help));
			else
				add((new Label ("help", "")).setVisible(false));

			add(selector);
			
			add(getFeedback());
			
			add(new Label("text", new Model<String>() {
				public String getObject() {
					return CheckField.this.getText()!=null?CheckField.this.getText().getObject():null;
				}
			}));
		}
	}
	
	
	/**
	 * 
	 * 
	 * 
	 * @param id
	 */
	public CheckField(String id) {
		this(id, null);
	}
	
	public CheckField(String id, IModel<Boolean> model) {
		super(id, model);
		
		setOutputMarkupId(true);
		
		if (model!=null)
			setValue(model.getObject());
		
		Label label = new Label("label", getLabel());
		label.setEscapeModelStrings(false);
		label.add(new AttributeModifier("for", id));
		add(label);
		
		add(new WebMarkupContainer("readonly") {
			public boolean isVisible() {
				return CheckField.this.isReadOnly();
			}
		});		
		
		
		label.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				return getDisposition()==null||getDisposition()==Disposition.HORIZONTAL ? "col-lg-2 control-label" : "control-label";
			}
		}));
	}
	
	public void onUpdate(AjaxRequestTarget target) {
	}
	

	/**----------------------------------------------------------------------------------------------- 
	 */
	@Override
	public void updateModel() {
		IModel<?> model = getInput().getDefaultModel();
		Object input = model.getObject();
		if (input!=null) {
			if (getModel().getObject()!=null && !getModel().getObject().equals(getValue()) || getModel().getObject()==null && input!=null) {
				if (getEditor()!=null) {
					getEditor().setUpdatedPart(getPart());
				}
				getModel().setObject(getValue());
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
	
	public IModel<String> getLabel() {
		try {
			IModel<String> model = new StringResourceModel("property."+getProperty(), this, null);
			return model;
		} 
		catch (java.util.MissingResourceException e) {
			return null;
		} 
		catch (Exception e2) {
			return null;
		}
	}
	
	public IModel<String> getText() {
		return new Model<String>("");
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(AW));
		response.render(CssHeaderItem.forReference(BL));
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
	
	protected String getPart() {
		return getLabel()!=null ? getLabel().getObject() : null;
	}
	
	protected String getDisplayValue(String value) {
		return value;
	}
}
