package com.novamens.wicket.markup.html.repeater.util;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface OnClickListener<T> extends Serializable {
	public void onClick(AjaxRequestTarget target, T document);
	public void onDblClick(AjaxRequestTarget target, T document);
}
