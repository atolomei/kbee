package kbee.web.page;

import org.apache.wicket.model.IModel;

public abstract class ObjectPage<T extends com.novamens.dom.Object> extends ApplicationPage<T> {
	private static final long serialVersionUID = 1L;

	public ObjectPage() {
		super(null, null, null, null);
	}
	
	public ObjectPage(IModel<T> model) {
		super(model);
	}
}