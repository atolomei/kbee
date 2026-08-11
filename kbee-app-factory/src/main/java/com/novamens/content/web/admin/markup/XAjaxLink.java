package com.novamens.content.web.admin.markup;

import org.apache.wicket.model.IModel;

public class XAjaxLink extends BCAjaxElement implements XLink {
	
	private static final long serialVersionUID = 1L;
	
	private String localpath;

	public XAjaxLink(IModel<String> label) {
		super(label);
	}
	
	public XAjaxLink(IModel<String> label, String lp) {
		super(label);
		this.localpath=lp;
	}
	
	public String getLocalPath() {
		return localpath;
	}
}
