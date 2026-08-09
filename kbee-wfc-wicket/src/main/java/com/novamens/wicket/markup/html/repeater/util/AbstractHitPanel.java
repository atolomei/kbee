package com.novamens.wicket.markup.html.repeater.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.wicket.markup.html.panel.KBPanel;

public abstract class AbstractHitPanel<T> extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	private List<OnClickListener<T>> listeners = new ArrayList<OnClickListener<T>>();
	
	private String context;
	
	public AbstractHitPanel(String id) {
		super(id);
	}
	
	public void init(Object object, int tabindex) {
	}
	
	public void addListener(OnClickListener<T> listener) {
		this.listeners.add(listener);
	}
	
	public void onDocumentClick(AjaxRequestTarget target, T document) {
		for (OnClickListener<T> listener : this.listeners) {
			listener.onClick(target, document);
		}
	}
	
	public void onDocumentDblClick(AjaxRequestTarget target, T document) {
		for (OnClickListener<T> listener : this.listeners) {
			listener.onDblClick(target, document);
		}
	}
	
	public String getContainerContext() {
		return context;
	}
	
	public void setContainerContext(String context) {
		this.context=context;
	}
	
	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}

	protected String getLabelString(String key, String... parameter) {
		return getLabel(key, parameter).getObject();
	}
	
	protected IModel<String> getLabel(String key, String... parameter) {
		StringResourceModel model = new StringResourceModel(key, this);
		model.setParameters((Object[]) parameter);
		return model;
	}
}
