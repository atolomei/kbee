package kbee.web.portal6.event;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.PortalObject;

public class PortalAjaxStructureShowHierarchyEvent<T extends PortalObject> extends PortalAjaxEvent<T> implements IDetachable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	int va = VIEW_HIERARCHY_YES;
	
	public PortalAjaxStructureShowHierarchyEvent(AjaxRequestTarget requestTarget, IModel<T> model, int va) {
		super(requestTarget, model);
		this.va=va;
	}
	
	public int getShowHierarchy() {
		return this.va;
	}
	
	public void detach() {
		super.detach();
	}
}
