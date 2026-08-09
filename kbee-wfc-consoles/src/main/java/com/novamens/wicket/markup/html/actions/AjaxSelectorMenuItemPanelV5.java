package com.novamens.wicket.markup.html.actions;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;

import com.novamens.wicket.markup.html.form.ExtendedChoiceField;
import com.novamens.wicket.markup.html.form.ExtendedChoiceField.AjaxBehavior;

public class AjaxSelectorMenuItemPanelV5<T,V> extends AbstractMenuItemPanelV5<T> {

	private static final long serialVersionUID = 1L;

	IModel<T> model;
	IModel<V> selected_model;
	IModel<List<V>> choices;
	boolean init = false;
	
	public class AjaxBehavior extends AbstractDefaultAjaxBehavior {
		
		protected void respond(AjaxRequestTarget target) {
			//Request request = RequestCycle.get().getRequest();
			//String index = request.getRequestParameters().getParameterValue("index").toString("");
			//setSelected(Integer.valueOf(index));
			AjaxSelectorMenuItemPanelV5.this.onUpdate(target);
		}
		
		public void renderHead(final Component component, final IHeaderResponse response) {
			super.renderHead(component, response);
			
			//StringBuilder script = new StringBuilder();
			//String selectid = AjaxSelectorMenuItemPanelV5.this.get("select").getMarkupId();
 			//script.append("$('#"+selectid+"').on('changed.bs.select', function (e, clickedIndex, newValue, oldValue) {");
			//script.append(getCallbackScript()+";");
			//script.append("});");
			//if (getValue()!=null)
			//script.append("$('#"+selectid+"').val('"+getDisplayValue(getValue())+"');");
			//response.render(OnDomReadyHeaderItem.forScript(script.toString()));
				
			init = true;
		}
		
		
		@Override
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
			super.updateAjaxAttributes(attributes);
			//attributes.getDynamicExtraParameters().add("return {index: clickedIndex};");
			attributes.setEventPropagation(EventPropagation.STOP);
		}
	}

	
	
	public AjaxSelectorMenuItemPanelV5(String id, IModel<T> model, IModel<V> selected_model, IModel<List<V>> choices) {
		super(id);
		this.setOutputMarkupId(true);
		this.model=model;
		this.choices=choices;
		this.selected_model= selected_model;
		add(new AjaxBehavior());
		addListeners();
	}
	

	//@Override
	//protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
	//	super.updateAjaxAttributes(attributes);
	//	attributes.setEventPropagation(EventPropagation.STOP); 
	//}
	

	
	
	@Override
	public void onClick() throws Exception {
	}


	@Override
	public String getBeforeClick() {
		return null;
	}
	
	
	
	public void addListeners() {}
		
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		Label la=new Label("item-label", getLabel());
		la.setEscapeModelStrings(false);
		la.setVisible(getLabel()!=null);
		add(la);
		
		ExtendedChoiceField<V> pa=new ExtendedChoiceField<V>("selector", selected_model, choices) {
			private static final long serialVersionUID = 1L;
			@Override
			public String getIdValue(V value) {
				return  AjaxSelectorMenuItemPanelV5.this.getIdValue(value);
			
			}
			@Override
			public String getDisplayValue(V value) {
				return  AjaxSelectorMenuItemPanelV5.this.getDisplayValue(value);
			}
		};
		add(pa);
	}
	
	
	


	@Override
	public String getLabel() {
		return null;
	}

	
	
	public boolean isIconVisible() {
		return false;
	}
	
	@Override
	public String getCssClass() {
		//if (isIconVisible())
		//	return "label-selected";
		//else
		//	return "label-no-selected";
		return null;
	}

	@Override
	public String getIconCssClass() {
		return  null;
	}
	
	public void onUpdate(AjaxRequestTarget target) {
	}
	
	
	public T getValue() {
		return null;
	}
	
	public void setSelected(int index) {
		//if (index>=0 && index<this.choices.getObject().size()) {
		//	this.model.setObject(this.choices.getObject().get(index));
		//}
		//this.selectedIndex = index;
	}
	

	public String getIdValue(V value) {
		return value.toString();
	}
	
	public String getDisplayValue(V value) {
		return value.toString();
	}


	

	public void onDetach() {
		super.onDetach();
		if (model!=null)
			model.detach();
		if (choices!=null)
			choices.detach();
	}


	
	
	
	


}
