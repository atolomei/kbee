package kbee.web.portal6.factory;

import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.PortalObject;


/*
 * 
 * 
 */
public interface PanelPortalModel<T extends PortalObject> {

	public void setPortalModel (IModel<T> model);
	public IModel<T> getPortalModel ();
	
}
