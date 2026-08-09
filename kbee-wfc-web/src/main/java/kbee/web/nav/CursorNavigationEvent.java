package kbee.web.nav;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class CursorNavigationEvent<T> extends AbstractWicketAjaxEvent implements IDetachable {
	
	private static final long serialVersionUID = 1L;
	private IModel<T> model;
	private int index;
	
	public CursorNavigationEvent(AjaxRequestTarget target, IModel<T> model) {
		super(target);
		setModel(model);
		this.index = 0;
	}
	public CursorNavigationEvent(AjaxRequestTarget target, IModel<T> model, int index) {
		super(target);
		setModel(model);
		this.index = index;
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
	
	public int getIndex() {
		return this.index;
	}
	@Override
	public void detach() {
		if  (model!=null)
				model.detach();
	}
}
