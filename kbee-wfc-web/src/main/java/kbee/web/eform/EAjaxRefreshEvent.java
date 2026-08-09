package kbee.web.eform;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class EAjaxRefreshEvent extends AbstractWicketAjaxEvent   {
	
	String componentId;
	
	public EAjaxRefreshEvent(AjaxRequestTarget target, String componentId) {
		super(target);
		this.componentId = componentId;
	}
	
	public String getComponentId() {
		return componentId;
	}
}