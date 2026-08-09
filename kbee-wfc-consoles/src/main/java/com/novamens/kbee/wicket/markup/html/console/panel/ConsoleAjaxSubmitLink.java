package com.novamens.kbee.wicket.markup.html.console.panel;

import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;

public class ConsoleAjaxSubmitLink extends AjaxSubmitLink {
 
	private static final long serialVersionUID = 7098749242418484227L;

	public ConsoleAjaxSubmitLink(String id, final Form<?> form) { 
	   super(id, form);
	}
}
