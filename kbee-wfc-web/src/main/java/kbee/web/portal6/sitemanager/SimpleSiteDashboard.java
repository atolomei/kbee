package kbee.web.portal6.sitemanager;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.Site;

import kbee.web.portal6.panel.PortalPanel;

/**
 * 
 */
public class SimpleSiteDashboard extends PortalPanel<Site> {

	private static final long serialVersionUID = 1L;

	public SimpleSiteDashboard(String id, IModel<Site> model) {
		super(id, model);
		setOutputMarkupId(true);
	}

	/**
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add (new SiteBCPanel("bc.site-home", getModel()));
		
		addRecentAccesseslinks();
		addSitelinks();
		addSiteKeyStats();
	}
	
	
	/**
	 * 
	 * 
	 */
	private void addSitelinks() {
					
		AjaxLink<Site> site_security=new AjaxLink<Site>("site_security", getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				fire (new SiteManagerNavigationEvent(target, getModel(), SiteAdminEvent.NAV_SITE_SECURITY));
			}
		};
		
		AjaxLink<Site> site_reports=new AjaxLink<Site>("site_reports", getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				fire (new SiteManagerNavigationEvent(target, getModel(), SiteAdminEvent.NAV_SITE_REPORTS));
			}
		};

		AjaxLink<Site> site_editor=new AjaxLink<Site>("site_editor", getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				fire (new SiteManagerNavigationEvent(target, getModel(), SiteAdminEvent.NAV_SITE_EDITOR));
			}
		};

		AjaxLink<Site> site_contents=new AjaxLink<Site>("site_contents", getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				fire (new SiteManagerNavigationEvent(target, getModel(), SiteAdminEvent.NAV_SITE_CONTENTS));
			}
		};
		
		AjaxLink<Site> site_pages=new AjaxLink<Site>("site_pages", getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				fire (new SiteManagerNavigationEvent(target, getModel(), SiteAdminEvent.NAV_SITE_PAGES));
			}
		};

		add(site_contents);
		add(site_reports);
		add(site_security);
		add(site_pages);
		add(site_editor);
		
	}
	
	/**
	 * site info
	 * 
	 */
	private void addSiteKeyStats() {
		// TODO Auto-generated method stub
	}

	private void addRecentAccesseslinks() {
		// TODO Auto-generated method stub
	}

}
