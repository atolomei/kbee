package com.novamens.wicket.markup.html.repeater.util;

import java.io.Serializable;

import org.apache.wicket.model.IModel;

public class NavigationOrder implements Serializable {
	private static final long serialVersionUID = 1L;
	private IModel<String> label;
	private String property;
	private boolean ascending;
	
	public NavigationOrder(IModel<String> label, String property, boolean ascending) {
		this.label = label;
		this.property = property;
		this.ascending = ascending;
	}
	public String getProperty() {
		return property;
	}
	
	public String getLabel() {
		return label.getObject();
	}
	
	public boolean isAscending() {
		return ascending;
	}
	
	@Override
	public boolean equals(Object order) {
		if (order instanceof NavigationOrder) {
			return getLabel().equals(((NavigationOrder)order).getLabel());
		}
		return false;
	}
}
