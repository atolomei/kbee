package kbee.web.portal6.sitemanager;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import com.novamens.beans.BeansService;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ExternalDao;
//import com.novamens.content.web.searcher.page.AbstractSearcherPage;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.portal6.editor.PortalSiteEditor;
import kbee.web.portal6.editor.PortalSitePagesPanel;
import kbee.web.portal6.panel.PortalPanel;


/**
 * Menu
 * Close
 * Breadcrumb
 * 
 *  Home
 *  Site Editor
 *  Page Editor
 *  Area Editor
 *  Block Editor
 *
 */
public class SimpleSiteManager extends PortalPanel<Site> {
				
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger =  kbee.util.logging.Logger.getLogger(SimpleSiteManager.class.getName());
	
	
	public SimpleSiteManager(IModel<Site> model) {
		this("site-manager", model);
	}
	
	public SimpleSiteManager(String id, IModel<Site> model) {
		super(id, model);
		setOutputMarkupId(true);
	}

	
	@Override
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<SiteAdminOpenEvent<? extends PortalObject>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(SiteAdminOpenEvent<? extends PortalObject> event) {
				logger.debug(event.getClass().getName() + " " + event.getModel().getObject().getTitle());
				//event.getRequestTarget().add(AbstractSearcherPage.this);
			}
		});
		
		add(new WicketEventListener<SiteManagerNavigationEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(SiteManagerNavigationEvent event) {
				showPanel(event);
				// event.getRequestTarget().add(AbstractSearcherPage.this);
			}
		});

	}

	
	
	/**
	private IModel<Site> model;
	public  void setModel(IModel<Site> model) {
		this.model=model;
	}
	
	public IModel<Site> getModel() {
		return this.model;
	}
	*/
	public void onDetach() {
		super.onDetach();
		//if (model!=null)
		//	model.detach();
	}
	
	
	
	public void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer cont=new WebMarkupContainer("site-manager-ajax-container");
		cont.setOutputMarkupId(true);
		add(cont);
				
		cont.add (new SimpleSiteManagerTopbar(getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void close(AjaxRequestTarget target) {
				logger.debug("close site manager");
				SimpleSiteManager.this.close(target);
			}
		});
		
									
		//cont.add (new SimpleSiteDashboard("site-manager-main-panel", getModel()));
		cont.add (new SimpleSiteContentsEditor("site-manager-main-panel", getModel()));
		//add (new SimpleSiteEditor("editor", getModel()));
	}

	public void close(AjaxRequestTarget target) {
		fire(new SiteAdminCloseEvent<Site>(target, getModel()));
		
	}
	
	
	protected void showPanel(SiteManagerNavigationEvent event) {
		
		String key=event.getNavigationKey();
		
		if (key.equals(SiteAdminEvent.NAV_HOME))	{
			((WebMarkupContainer) get("site-manager-ajax-container")).addOrReplace(new SimpleSiteDashboard("site-manager-main-panel", getModel()));
		}
		else if (key.equals(SiteAdminEvent.NAV_SITE_EDITOR)) {
			((WebMarkupContainer) get("site-manager-ajax-container")).addOrReplace(new PortalSiteEditor("site-manager-main-panel", getModel()));
		}
		else if (key.equals(SiteAdminEvent.NAV_SITE_CONTENTS)) {
			((WebMarkupContainer) get("site-manager-ajax-container")).addOrReplace(new SimpleSiteContentsEditor("site-manager-main-panel", getModel()));
		}
		else if (key.equals(SiteAdminEvent.NAV_SITE_ATTRIBUTES)) {
			((WebMarkupContainer) get("site-manager-ajax-container")).addOrReplace(new SimpleSiteTagsEditor("site-manager-main-panel", getModel(), getSiteDataSetMemberModel()));
		}
		else if (key.equals(SiteAdminEvent.NAV_SITE_DASHBOARD)) { 
			((WebMarkupContainer) get("site-manager-ajax-container")).addOrReplace(new PortalSiteEditor("site-manager-main-panel", getModel()));
		}
		
		
		else if (key.equals(SiteAdminEvent.NAV_SITE_PAGES)) { 
			((WebMarkupContainer) get("site-manager-ajax-container")).addOrReplace(new PortalSitePagesPanel("site-manager-main-panel", getModel()));
		}
		else if (key.equals(SiteAdminEvent.NAV_SITE_REPORTS))	{
			((WebMarkupContainer) get("site-manager-ajax-container")).addOrReplace(new PortalSiteEditor("site-manager-main-panel", getModel()));
		}
		else if (key.equals(SiteAdminEvent.NAV_SITE_SECURITY)) {
			((WebMarkupContainer) get("site-manager-ajax-container")).addOrReplace(new PortalSiteEditor("site-manager-main-panel", getModel()));
		}
		else
			throw new KbeeRuntimeException("Event key not found -> " + key );
		
		
		event.getRequestTarget().add((WebMarkupContainer) get("site-manager-ajax-container"));
		
		
		
	}
	
	
	protected IModel<DataSetMember> getSiteDataSetMemberModel() {
		DataSet dataset = getExternalDao().getSiteDataSet(getDomain());
		DataSetMember  member = getExternalDao().findMemberByExternalId(getModel().getObject().getOId(), dataset);
		if (member!=null)
			return new ObjectModel<DataSetMember>(member);
		return null;
	}

	
	private ExternalDao getExternalDao() {
		return (ExternalDao) ServiceLocator.getService(BeansService.class).getBean("externalDao");
	}
	
 	
		
}
