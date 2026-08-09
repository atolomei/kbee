package kbee.web.portal6.event;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketAjaxEvent;
import com.novamens.portal6.model.PortalObject;

public class PortalAjaxEvent<T extends PortalObject> extends AbstractWicketAjaxEvent implements WicketAjaxEvent, IDetachable {

	
	
	
	
	public static final String STRUCTURE_VIEW = "edit-structure-view";
	public static final String PAYLOAD_VISIBLE = "edit-payload-visible";
	
	public static final String ARCHIVED_VISIBLE = "edit-archived-visible";
	public static final String DELETED_VISIBLE = "edit-deleted-visible";
	public static final String CONTROLLER_VISIBLE = "edit-controller-visible";
	public static final String PIN_EDITOR = "edit-pin-editor";
	public static final String EDITOR_DISPOSITION = "editor-disposition";
	
 
								
	public static final String CLOSE= "close";
	
	
	
	public static final int PIN_EDITOR_YES = 40000;
	public static final  int PIN_EDITOR_NO = 42000;
	
	// EDIT
	public static final int VIEW_HIERARCHY_NO 	= 10;
	public static final int VIEW_HIERARCHY_YES 	= 20;
	
	// EDIT
	public static final int SHOW_ARCHIVED_YES = 20000;
	public static final int SHOW_ARCHIVED_NO  = 22000;

	// EDIT
	public static final int SHOW_DELETED_YES = 30000;
	public static final int SHOW_DELETED_NO  = 32000;

	// EDIT
	public static final int SHOW_CONTROLLER_YES = 40000;
	public static final int SHOW_CONTROLLER_NO  = 42000;

	public static final int EDITOR_DISPOSITION_LEFT  = 50000;
	public static final int EDITOR_DISPOSITION_RIGHT  = 52000;

	
	
	// EDIT
	public static final int SHOW_PAYLOAD_YES = 53000;
	public static final int SHOW_PAYLOAD_NO  = 54000;

	

	
	public static final int RELEASE_MODE	= 90000;
	public static final int EDIT_MODE	  	= 92000;
	public static final int PREVIEW_MODE	= 94000;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	IModel<T> model;
	
	
	public PortalAjaxEvent(AjaxRequestTarget requestTarget, IModel<T> model) {
		this(requestTarget, model, null);
	}
	
	public PortalAjaxEvent(AjaxRequestTarget requestTarget, IModel<T> model, String action) {
		super(requestTarget);
		this.model=model;
	}
	
	@Override
	public void detach() {
		 if (model!=null)
			 model.detach();
	}

	public IModel<T> getModel() {
		return model;
	}
	
	
	/**
	 * 
	 * Edit
	 * Open
	 * MoveUp
	 * MoveDown
	 * Delete
	 * Archive
	 * Restore
	 * 
	 */

}
