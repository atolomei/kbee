package com.novamens.wicket.markup.html.panel;

import com.novamens.wicket.markup.html.repeater.util.OnClickListener;

public interface SelectorPanel<T> {
	public void addListener(OnClickListener<T> listener);
}
