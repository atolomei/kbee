package com.novamens.wicket.markup.html.form;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;

@SuppressWarnings("serial")
public class CollapsibleBehavior extends Behavior{

	private static final long serialVersionUID = 1L;
	private Component toogle, target;
	
	public CollapsibleBehavior(Component toggle, Component target) {
		this.toogle = toggle;
		this.target = target;
		
		target.setVisible(false);
		
		toogle.add(new AjaxEventBehavior("click") {
			public void onEvent(AjaxRequestTarget target) {
				CollapsibleBehavior.this.target.setVisible(!CollapsibleBehavior.this.target.isVisible());
				target.add(CollapsibleBehavior.this.target.getParent());
			}
		});
	}
	


}
