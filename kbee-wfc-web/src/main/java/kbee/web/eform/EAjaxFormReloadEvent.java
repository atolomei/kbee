package kbee.web.eform;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.content.base.ResourceTag;
import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class EAjaxFormReloadEvent extends AbstractWicketAjaxEvent  {
	
	ResourceTag tag;
	
	public EAjaxFormReloadEvent(AjaxRequestTarget target, ResourceTag tag) {
		super(target);
		setTag(tag);
	}
	
	public ResourceTag getTag() {
		return tag;
	}

	public void setTag(ResourceTag tag) {
		this.tag = tag;
	}
}