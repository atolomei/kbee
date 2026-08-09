package com.novamens.wicket.markup.html.form;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.wicket.util.DummyBlockPanel;

/**
 * <p>
 * Choice Field with custom selector, more elegant than the HTML standard selector
 * Similar to {@link ChoiceField} 
 * <p>
 *
 * [example: GridConfigPanel in novamens-consoles uses it]
 *  
 * @param <T>
 */
@SuppressWarnings("serial")
public class ExtendedChoiceField<T> extends Panel {
	private static final long serialVersionUID = 1L;
	int selectedIndex = -1; 
	IModel<List<T>> choices;
	IModel<T> model;
	boolean init = false;
	
	protected static final ResourceReference BS = new CssResourceReference(Form.class, "bootstrap-select.css");
	protected static final ResourceReference BSJS = new JavaScriptResourceReference(Form.class, "bootstrap-select.js");
	
	public class AjaxBehavior extends AbstractDefaultAjaxBehavior {
		
		protected void respond(AjaxRequestTarget target) {
			Request request = RequestCycle.get().getRequest();
			String index = request.getRequestParameters().getParameterValue("index").toString("");
			setSelected(Integer.valueOf(index));
			ExtendedChoiceField.this.onUpdate(target);
		}
		
		public void renderHead(final Component component, final IHeaderResponse response) {
			super.renderHead(component, response);
			
			StringBuilder script = new StringBuilder();
			
			String selectid = ExtendedChoiceField.this.get("select").getMarkupId();
			
 			script.append("$('#"+selectid+"').on('changed.bs.select', function (e, clickedIndex, newValue, oldValue) {");
			
 			script.append(getCallbackScript()+"e.preventDefault();e.stopPropagation();");
 			
 			
			script.append("});");
			
			if (getValue()!=null)
			script.append("$('#"+selectid+"').val('"+getDisplayValue(getValue())+"');");
			
			response.render(OnDomReadyHeaderItem.forScript(script.toString()));
				
			init = true;
		}
		
		@Override
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
			super.updateAjaxAttributes(attributes);
			attributes.getDynamicExtraParameters().add("return {index: clickedIndex};");
			attributes.setEventPropagation(EventPropagation.STOP);
		}
	}
	
	
	
	public ExtendedChoiceField(String id, IModel<T> model, IModel<List<T>> choices) {
		super(id);
		
		this.choices = choices;
		this.model = model;
		this.selectedIndex = 0;
		this.setOutputMarkupId(true);

		
		if (model!=null && model.getObject()!=null) {
			String selected = getIdValue(model.getObject());
			int index = 0;
			for (T object : choices.getObject()) {
				if (selected.equals(getIdValue(object))) {
					this.selectedIndex = index;
					break;
				}
				else {
					index++;
				}
			}
		}
		
		WebMarkupContainer select = new WebMarkupContainer("select");
		
		select.setOutputMarkupId(true);
		
		if (getCss()!=null)
			select.add(new AttributeModifier("class", getCss()));
		
		select.add(new ListView<T>("choice", choices) {
			public void populateItem(final ListItem<T> item) {
				
				item.add( getElement(item.getModelObject()) );
				
				//item.add((new Label("label",	"<i class=\"fal fa-birthday-cake\"></i>" + "sss" + getDisplayValue(item.getModelObject()))).setEscapeModelStrings(false));
				
				
			} 
		});
		
		add(select);
		
		Label js = new Label("js", new Model<String>() {
			public String getObject() {
				String selectid = ExtendedChoiceField.this.get("select").getMarkupId();
				String script = "";
				if (getValue()!=null)
					script +="$('#"+selectid+"').val('"+getDisplayValue(getValue())+"');";
				script += "$('#"+selectid+"').selectpicker('refresh');";
				return script;
			}
		});
		
		js.setEscapeModelStrings(false);
		
		add(js);
		
		add(new AjaxBehavior());
	}
	
	public void onUpdate(AjaxRequestTarget target) {
	}
	
	
	public Component getElement(T object) {
		return new Label("label",getDisplayValue(object));
	}
	
	public T getValue() {
		return this.model.getObject();
	}
	
	public void setSelected(int index) {
		if (index>=0 && index<this.choices.getObject().size()) {
			this.model.setObject(this.choices.getObject().get(index));
		}
		this.selectedIndex = index;
	}
	
	public String getIdValue(T value) {
		return value.toString();
	}
	
	public String getDisplayValue(T value) {
		return value.toString();
	}
	
	public String getCss() {
		return null;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(BS));
		response.render(JavaScriptHeaderItem.forReference(BSJS));
	}
}
