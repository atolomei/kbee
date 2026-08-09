package kbee.web.portal6.sitemanager;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketAjaxEvent;
import com.novamens.portal6.model.PortalObject;

public abstract class SiteAdminEvent<T extends PortalObject> extends AbstractWicketAjaxEvent implements WicketAjaxEvent {
					
	IModel<T> model;

	public static final String NAV_SITE_ATTRIBUTES  = "site_attributes";
	public static final String NAV_HOME   = "site_home";
	public static final String NAV_SITE_EDITOR   = "site_editor";
	public static final String NAV_SITE_REPORTS  = "site_reports";
	public static final String NAV_SITE_CONTENTS = "site_contents";
	public static final String NAV_SITE_SECURITY = "site_security";
	public static final String NAV_SITE_PAGES 	 = "site_pages";
	public static final String NAV_SITE_DASHBOARD = "site_dashboard";

	
	
	
	// int site_mode = 0;
	boolean is_detail_page = false;

	public SiteAdminEvent(AjaxRequestTarget requestTarget, IModel<T> model) {
		super(requestTarget);
		this.model = model;
	}
	
	public SiteAdminEvent(AjaxRequestTarget requestTarget, IModel<T> model, int site_mode) {
		super(requestTarget);
		this.model = model;
	}

	//public int getSiteEditorMode() {
	//	return this.site_mode;
	//}

	//public void setDetailPage(boolean b) {
	//	is_detail_page = true;
	//}

	//public boolean isDetailPage() {
	//	return is_detail_page;
	//}

	public IModel<T> getModel() {
		return model;
	}

}
