package com.novamens.portal.model.diagrammablesite;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.event.Event;
import com.novamens.portal6.model.Site;

/**
 * 
 *
 */
public interface PortalPage {

	/** SiteMode (for admin) */
	static public final int NORMAL 				= 0;
	static public final int SHOW_UNPUBLISHED	= 2;
	static public final int DO_NOT_SHOW_AREAS	= 1;
	
	
	public void fire(Event event);

	// Para Directorio
	//
	void onSiteAdmin(IModel<Site> model, AjaxRequestTarget target);
	void onSiteAdmin(IModel<Site> model, AjaxRequestTarget target, int site_mode);
 
	// Para Sitios
    //
	void onSiteAdmin(AjaxRequestTarget target);
	void onSiteAdmin(AjaxRequestTarget target, int site_mode);
 	void onPageAdmin(AjaxRequestTarget target);
  
}
