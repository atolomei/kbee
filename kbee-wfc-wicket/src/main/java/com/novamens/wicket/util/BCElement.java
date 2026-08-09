package com.novamens.wicket.util;

import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

/**
 * TODO: onClick doesnt seem to be working
 *
 */
public class BCElement extends Link<Void> implements IBCElement {
	private static final long serialVersionUID = 1L;
	
	private boolean isNewTab = false;
	
	private IModel<String> html_title;
	private IModel<String> label;
	private String resourceKey;
	private String key;
	
	public BCElement(String resourceKey) {
		super("link");
		this.resourceKey = resourceKey;
		label = newLabel();
	}
	
	public BCElement() {
		super("link");
		label = newLabel();
	}
	
	public BCElement(IModel<String> model) {
		super("link");
		label=model;
	}
	
	public BCElement(IModel<String> model, String key) {
		super("link");
		label=model;
		this.key=key;
	}
	
	public void setKey(String key) {
		this.key=key;
	}
	
	public String getKey() {
		return this.key;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	}
	
	/**
	 * NOTE THIS COMPONENT IS NEVER RENDERED
	 * IT IS USED BY 
	 * 
	 * {@link MenuBreadCrumbPanel<T>}
	 * 
	 */
	public void onBeforeRender() {
		super.onBeforeRender();
	}
	
	public void setHTMLTitleAttribute(IModel<String> ht) {
		html_title=ht;
	}
	
	public IModel<String> getHTMLTitleAttribute() {
		return html_title;
	}
	
	public IModel<String> getLabel() {
		return label;
	}

	public void onClick() {
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if(label!=null)
			label.detach();
	}
	
	public AbstractLink getLink(String id) {
		return this;
	}

	public void setIsNewTab(boolean nt) {
		this.isNewTab=nt;
		
	}
	
	public boolean isNewTab() {
		return this.isNewTab;
	}

	@Override
	public boolean isAjax() {
		return false;
	}
	
	protected IModel<String> newLabel() {
		return  new ResourceModel(resourceKey);
	}
	
	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}
	
	protected String getLabelString(String key) {
		return getLabel(key).getObject();
	}
}