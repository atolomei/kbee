package kbee.web.portal6.event;


import java.time.Instant;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.markup.html.event.WicketEvent;
import com.novamens.portal6.model.PortalObject;

public abstract class PortalWicketEvent<T extends PortalObject> implements WicketEvent, IDetachable  {

	
	private static final long serialVersionUID = 1L;
	
	
	IModel<T> model;
	
	
	PortalWicketEvent(IModel<T> model) {
		this.model=model;
	}

	@Override
	public void detach() {
			if (model!=null)
				model.detach();
	}
	
	@Override
	public Instant getTime() {
		return Instant.now();
	}
	
	public 	IModel<T> getModel() {
		return model;
	}
	
	@Override
	public Object getObject() {
		if (model!=null)
			return model.getObject();
		return null;
	}

}
