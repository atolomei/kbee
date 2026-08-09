package kbee.web.portal6.event;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.PortalObject;

public class PortalAjaxStructureShowArchivedEvent<T extends PortalObject> extends PortalAjaxEvent<T> implements IDetachable {

	private static final long serialVersionUID = 1L;

	int show_archive = PortalAjaxEvent.SHOW_ARCHIVED_YES;
	
	public PortalAjaxStructureShowArchivedEvent(AjaxRequestTarget requestTarget, IModel<T> model, int showArchived) {
		super(requestTarget, model);
		show_archive = showArchived;
	}
	
	public int getShowArchived() {
		return this.show_archive;
	}
	

}
