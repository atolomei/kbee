package kbee.web.portal6.editor;


import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.document.IDoc;
import com.novamens.content.entity.Person;
import com.novamens.content.notes.Billboard;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorPanel;
import kbee.web.nav.SitesBC;
import kbee.web.page.ConsoleObjectPage;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.portal6.sitemanager.PortalNavigationBar;

public class PortalSiteEditorPage extends ConsoleObjectPage<Site> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalSiteEditorPage.class.getName());
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PortalSiteEditorPage() {
	}
	
	public PortalSiteEditorPage(PageParameters parameters) {
		
		Site site = getSite(parameters);
		
		if (site!=null) {
			setModel(new ObjectModel<Site>(site));
			getPageParameters().set("id", site.getId().toString());
		}
	}
	
	protected Site getSite(PageParameters parameters) {
		
		if (parameters==null)
			return null;
		
		com.novamens.portal6.model.Site site = null;
		StringValue id = parameters.get("id");
		if (!id.isNull() && !id.isEmpty()) {
			try {
			site = getPortalDao().findSiteById(id.toLong());
			if (site!=null && !site.getDomain().equals(getDomain())) {
				site = null;
			}
			} catch (Exception e) {
				logger.error(e);
				return null;
			}
		}	
		return site;
	}

	public PortalSiteEditorPage(IModel<Site> model) {
		super(model);
		getPageParameters().set("id", model.getObject().getId().toString());
	}
	
	public void onInitialize()  {
		super.onInitialize();

		if (getModel()==null) {
			add(new ErrorPanel("panel"));
			add(new ErrorPanel("info-panel"));
			return;
		}

		
		setLogVisit(true);		
		
		//setMenu(new InvisiblePanel("menu"));
		//setTopNavigation(new PortalNavigationBar("navigation", getModel()));
		
		setTopNavigation(getMainTopbar());
		setMenu(getMainLaternalMenu());

		
		setPageTitle(new Model<String>(getModel().getObject().getDisplayName() ));

		PageContentHeaderPanel<Site> panel=new PageContentHeaderPanel<Site>(null);
		panel.setTitle(getModel().getObject().getDisplayName());
		panel.setSubLine(new Model<String>("Portal"));
		 
		 
		
		MenuBreadCrumbPanel<Void>  bc = new MenuBreadCrumbPanel<Void>();
		
		bc.addElement(new SitesBC());
		bc.addElement(new SiteDropDownBC(getModel()));
		bc.addElement(new BCElement(new Model<String>(getModel().getObject().getDisplayName())));
		panel.setBreadcrumbPanel(bc);
		//setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.sites", this, null).getObject()));
		setSearchPanel(false);
		setAdvancedSearch(false);
		setSuggester(false);
		setPageContentHeader(panel);
		

		add(new PortalSiteMainPanel("panel", getModel()));

		
		/**
		Site site= getModel().getObject();
		Page page=site.getHomePage();
		if (page!=null) {
			add(page.getService(PortalObjectViewerRenderService.class).build("panel"));
			//add(page.getService(PortalObjectViewerRenderService.class).getMetaInfoPanel("info-panel"));
		}
		else {
			add(new ErrorPanel("panel", new Model<String>("no home page")));
		}
		**/
		
	}
	
	public PortalDao getPortalDao() {
		return (PortalDao)ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}
	
}
