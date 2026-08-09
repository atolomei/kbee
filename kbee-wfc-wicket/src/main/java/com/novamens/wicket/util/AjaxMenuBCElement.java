package com.novamens.wicket.util;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;


/**
 * 
 * whaR IS THIS ???
 * 
 *
 */
public class AjaxMenuBCElement extends Panel {
			
	private static final long serialVersionUID = 1L;

	private IBCElement element;
	
	public AjaxMenuBCElement(IBCElement bce) {
		super("bc-menu-item");
		
		this.element=bce;
		
		AjaxLink<Void> link = new AjaxLink<Void>("link") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				
				
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
