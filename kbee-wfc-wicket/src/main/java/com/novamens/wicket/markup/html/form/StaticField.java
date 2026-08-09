package com.novamens.wicket.markup.html.form;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.wicket.markup.html.form.Form.Disposition;

@SuppressWarnings("serial")
public class StaticField<T> extends Field<T> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(StaticField.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	
	public class ControlFragment extends Fragment {
		
		WebMarkupContainer sub_container;
		Label subtitle;
		
		
		public ControlFragment(String id) {
			super(id, "control-fragment", StaticField.this);
			
			
			 sub_container = new  WebMarkupContainer("subtitle-container");
			 add(sub_container);
			 sub_container.setVisible(  getSubtitle()!=null );
			 subtitle = new Label("subtitle", getSubtitle());
			 subtitle.setEscapeModelStrings(false);
			 sub_container.add(subtitle);
			 
			 
			 
			Label value = new Label("input", new Model<String>() {
				public String getObject() {
					if (StaticField.this.getModel()!=null && StaticField.this.getModel().getObject()!=null)
						return StaticField.this.getModel().getObject().toString();
					else
						return "";
				}
			});
			
			if (getTabIndex()>0)
				value.add(new AttributeModifier("tabindex", getTabIndex()));

			add(value);
		}
	}
	
	public StaticField(String id) {
		this(id, null);
	}
	
	
	public StaticField(String id, IModel<T> model) {
		super(id, model);
		
		setOutputMarkupId(true);
	
		if (model!=null)
			setValue(model.getObject());
		
		Label label = new Label("label", new StringResourceModel("property."+id, StaticField.this, null));
		label.add(new AttributeModifier("for", id));
		label.setEscapeModelStrings(false);
		add(label);
		label.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL)
					return "col-lg-1 control-label";
				else
					return "control-label";
			}
		}));
	}
	
	public void onUpdate(AjaxRequestTarget target) {
		
	}
	

	
	public boolean isReadOnly() {
		return true;
	}
	
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("horizontal-layout")==null) {
			WebMarkupContainer layout = new WebMarkupContainer("horizontal-layout");
			layout.add(new AttributeModifier("class", getWidth().getCss()));
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
		//if (getDisposition()==Disposition.VERTICAL) {
		//	get("label").add(new AttributeModifier("style", "margin-bottom:5px;"));
		//}
	}
}
