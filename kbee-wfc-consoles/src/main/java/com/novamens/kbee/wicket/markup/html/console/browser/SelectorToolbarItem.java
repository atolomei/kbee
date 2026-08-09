package com.novamens.kbee.wicket.markup.html.console.browser;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.kbee.wicket.markup.html.console.event.SelectionEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.wicket.markup.html.form.ExtendedChoiceField;

import kbee.web.console.BaseBrowser;
			
public abstract class SelectorToolbarItem<T> extends ToolbarItem {

	private static final long serialVersionUID = 1L;

	protected String getStringValue( T value) {
		return value.toString();
	}
			
	protected void onUpdate(AjaxRequestTarget target) {
		
	}
	
	protected List<T> getList() {
			return new ArrayList<T>();
	}

	
	public SelectorToolbarItem(BaseBrowser<?> browser, Align align) {
		super(browser, align);
		
		setOutputMarkupId(true);
		
		add(new WicketEventListener<SelectionEvent>() {
			private static final long serialVersionUID = 1L;
			public void onEvent(SelectionEvent event) {
				event.getRequestTarget().add(SelectorToolbarItem.this);
			}
		});
	
	}
	
	
	IModel<T> model;

	public void setModel(IModel<T> model) {
		this.model=model;
	}

	public IModel<T> getModel() {
		return model;
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer icon = new WebMarkupContainer("icon");
		
		if (getIconCss()!=null)
			icon.add(new AttributeModifier("class", getIconCss()));
		else
			icon.setVisible(false);

		add(icon);
												
		add(new ExtendedChoiceField<T>("selector", getModel(), new PropertyModel<List<T>>(this, "list")) {
			private static final long serialVersionUID = 1L;
			public void onUpdate(AjaxRequestTarget target) {
				SelectorToolbarItem.this.onUpdate(target);
			}
			@Override
			public String getDisplayValue(T value) {
				return SelectorToolbarItem.this.getStringValue(value); 
			}
		});
		
		
		
		
		
		
		
		
		
		
		
	}
	
	
	
	


	protected IModel<String> getIconCss() {
		return null;
	}
}
