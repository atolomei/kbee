package kbee.web.portal6.event;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.PortalObject;

public class PortalEditEvent<T extends PortalObject> extends PortalWicketEvent<T> implements IDetachable {

	private static final long serialVersionUID = 1L;

	public PortalEditEvent(IModel<T> model) {
		super(model);
	}
	

}
