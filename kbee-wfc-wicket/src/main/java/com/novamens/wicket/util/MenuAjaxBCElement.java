package com.novamens.wicket.util;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class MenuAjaxBCElement extends Panel {

	private static final long serialVersionUID = 1L;

	private AjaxIBCElement  element;
	
	public MenuAjaxBCElement(AjaxIBCElement bce) {
		super("bc-menu-item");
		
		this.element=bce;
		
		AjaxLink<Void> link = new AjaxLink<Void>("link") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				element.onClick(target);
			}
		};
		
		link.add(new Label("label", bce.getLabel()));
		add(link);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (this.element!=null)
			this.element.detach();
	}

}
