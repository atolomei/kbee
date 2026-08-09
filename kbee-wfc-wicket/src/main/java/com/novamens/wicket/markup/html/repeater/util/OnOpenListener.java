package com.novamens.wicket.markup.html.repeater.util;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;


public interface OnOpenListener<T> extends Serializable {

	void onOpen(AjaxRequestTarget target, T object);

}
