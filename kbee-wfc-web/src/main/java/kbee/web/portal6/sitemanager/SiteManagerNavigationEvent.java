package kbee.web.portal6.sitemanager;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.Site;

public class SiteManagerNavigationEvent extends SiteAdminEvent<Site> {

	private String key;
	
	public SiteManagerNavigationEvent(AjaxRequestTarget requestTarget, IModel<Site> model, String key) {
		super(requestTarget, model);
		this.key=key;
	}
	
	
	public String getNavigationKey() {
		return key;
	}
	

}
