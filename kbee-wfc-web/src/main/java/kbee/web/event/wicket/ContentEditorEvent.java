package kbee.web.event.wicket;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.content.base.Content;
import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class ContentEditorEvent extends AbstractWicketAjaxEvent  {
	
	Content content = null;
	
	
	public ContentEditorEvent(AjaxRequestTarget target, Content content) {
		super(target);
		this.content = content;
	}
	
	public Content getContent() {
		return content;
	}
}
