package com.novamens.wicket.markup.html.form;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.wicket.markup.html.form.Form.Disposition;

@SuppressWarnings("serial")
public class RadioField<T> extends Field<T> {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(RadioField.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	protected static final ResourceReference AW = new CssResourceReference(Form.class, Form.FONTAWESOME );
	protected static final ResourceReference BL = new CssResourceReference(Form.class, "build.css");

	public class ControlFragment extends Fragment {
		
		WebMarkupContainer sub_container;
		Label subtitle;
		
		public ControlFragment(String id) {
			super(id, "control-fragment", RadioField.this);
			

			
			
			 sub_container = new  WebMarkupContainer("subtitle-container");
			 add(sub_container);
			 sub_container.setVisible(  getSubtitle()!=null );
			 subtitle = new Label("subtitle", getSubtitle());
			 subtitle.setEscapeModelStrings(false);
			 sub_container.add(subtitle);
			 
			 
			 
			Radio<T> selector = new Radio<T>("input", new PropertyModel<T>(RadioField.this, "value")) {
				@Override
				public boolean isEnabled() {
					return getEditor()!=null ? getEditor().isEditionEnabled() : true;
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
		}
	}
	
	public RadioField(String id) {
		this(id, null);
	}
	
	public RadioField(String id, IModel<T> model) {
		super(id, model);
		
		setOutputMarkupId(true);
		
		if (model!=null)
			setValue(model.getObject());
		
		Label label = new Label("label", new StringResourceModel("property."+getProperty(), this, null));
		label.setEscapeModelStrings(false);
		label.add(new AttributeModifier("for", id));
		add(label);
		
		
		
		add(new WebMarkupContainer("readonly") {
			public boolean isVisible() {
				return RadioField.this.isReadOnly();
			}
		});		
		
		
		label.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				return getDisposition()==null||getDisposition()==Disposition.HORIZONTAL ? "col-lg-1 control-label" : "control-label";
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
					getEditor().setUpdatedPart(((Label)get("label")).getDefaultModelObjectAsString().toLowerCase());
				}
				getModel().setObject(getValue());
			}
		}
	}

	public Component getInput() {
		onBeforeRender();
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
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(AW));
		response.render(CssHeaderItem.forReference(BL));
	}
	
	protected String getDisplayValue(String value) {
		return value;
	}
}