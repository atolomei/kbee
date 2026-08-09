package kbee.web.event.wicket;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.content.model.ModelElement;
import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class EditorEvent extends AbstractWicketAjaxEvent  {
	
	private String key =null;
	ModelElement element = null;
	
	public EditorEvent(AjaxRequestTarget target) {
		super(target);
	}
	
	public EditorEvent(AjaxRequestTarget target, ModelElement element) {
		super(target);
		this.element = element;
	}
	
	public EditorEvent(AjaxRequestTarget target, String key) {
		super(target);
		this.key = key;
	}
	
	public ModelElement getElement() {
		return element;
	}
	
	public String getKey() {
		return key;
	}
}
