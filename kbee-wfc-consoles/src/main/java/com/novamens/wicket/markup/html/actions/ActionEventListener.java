package com.novamens.wicket.markup.html.actions;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.wicket.markup.html.actions.Action;

public interface ActionEventListener {
	public void onAfterExecute(AjaxRequestTarget target, com.novamens.wicket.markup.html.actions.Action action);
}
