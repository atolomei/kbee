package com.novamens.wicket.markup.html.actions;

import com.novamens.wicket.markup.html.actions.ActionEventListener;

public interface Action {
	public String getLabel();
	public void addListener(ActionEventListener listener);
}
