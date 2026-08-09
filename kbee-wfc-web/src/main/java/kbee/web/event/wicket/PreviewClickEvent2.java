package kbee.web.event.wicket;



import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class PreviewClickEvent2<T extends Content> extends AbstractWicketAjaxEvent {
	
	IModel<T> model;
								
	public PreviewClickEvent2(IModel<T> model) {
		super(null);
		this.model=model;
	}

	public PreviewClickEvent2(AjaxRequestTarget target, IModel<T> model) {
		super(target);
		this.model=model;
	}
	
	public IModel<T> getModel() {
		return this.model;
	}
	
	public void setModel(IModel<T> model) {
		this.model = model;
	}
	
	public T getModelObject() {
		return this.model.getObject();
	}

}
