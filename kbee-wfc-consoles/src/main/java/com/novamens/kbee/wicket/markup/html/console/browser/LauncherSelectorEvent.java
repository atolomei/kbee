package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class LauncherSelectorEvent<T> extends AbstractWicketAjaxEvent implements IDetachable {

	private static final long serialVersionUID = 1L;
	
	IModel<T> model;
	private long selector_id;
	private String suffix;
	private String key;
	
	
	public LauncherSelectorEvent(AjaxRequestTarget requestTarget, IModel<T> model, long id) {
			this(requestTarget, model, id, null, "one-for-each");
		
			
	}
	public LauncherSelectorEvent(AjaxRequestTarget requestTarget, IModel<T> model, long id, String suffix, String key) {
		super(requestTarget);
		setModel(model);
		selector_id=id;
		this.suffix=suffix;
		this.key=key;
	}
	
	public String getKey() {
		return this.key;
	}
	
	public String getSuffix() {
		return this.suffix;
	}
	
	
	public long getItemId() {
		return selector_id;
	}
	
	
	public void detach() {
		getModel().detach();
	}
	
	public IModel<T> getModel () {
		return this.model;
	}
	
	public void setModel( IModel<T> model ) {
		this.model=model;
	}
	
	public T getModelObject() {
		return getModel().getObject();
	}
	
	
	
}
