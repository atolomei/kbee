package com.novamens.kbee.wicket.markup.html.behaviour;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;

public class KeyboardBehavior extends AjaxEventBehavior {
	private static final long serialVersionUID = 1L;
	
	public boolean listenallchars = false;

	public KeyboardBehavior() {
		super("keydown");
	}
	
	public KeyboardBehavior(boolean allchars) {
		super("keydown");
		listenallchars = allchars;
	}
	
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);

		@SuppressWarnings("serial")
		IAjaxCallListener listener = new AjaxCallListener(){
			@Override
			public CharSequence getPrecondition(Component component) {
				return  "var keycode = Wicket.Event.keyCode(attrs.event);" +
						"if ((keycode == 13)) {" +
						"	attrs.event.preventDefault(); return true; }"+
						"else" +
						"	return false;";
			}
		};	
		
		if (!listenallchars) attributes.getAjaxCallListeners().add(listener);
		attributes.getDynamicExtraParameters().add("var eventKeycode = Wicket.Event.keyCode(attrs.event);" +
			"return {keycode: eventKeycode};");
	}
	
	@Override
	protected void onEvent(AjaxRequestTarget target) {
		Request request = RequestCycle.get().getRequest();
		String jsKeycode = request.getRequestParameters().getParameterValue("keycode").toString("");
		onKey(target,jsKeycode);
	}
	
	protected void onKey(AjaxRequestTarget target, String jsKeycode) {
	}
}
