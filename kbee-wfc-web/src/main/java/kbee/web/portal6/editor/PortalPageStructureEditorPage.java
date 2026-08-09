package kbee.web.portal6.editor;


import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.PortalDao;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorPanel;
import kbee.web.nav.SitesBC;
import kbee.web.page.ConsoleObjectPage;
import kbee.web.page.PageContentHeaderPanel;


public class PortalPageStructureEditorPage extends ConsoleObjectPage<Page> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalPageStructureEditorPage.class.getName());
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PortalPageStructureEditorPage() {
	}
	
	public PortalPageStructureEditorPage(PageParameters parameters) {
		
		Page site = getPage(parameters);
		
		if (site!=null) {
			setModel(new ObjectModel<Page>(site));
			getPageParameters().set("id", site.getId().toString());
		}
	}
	

	public PortalPageStructureEditorPage(IModel<Page> model) {
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
		
		setTopNavigation(getMainTopbar());
		setMenu(getMainLaternalMenu());

		setPageTitle(new Model<String>(getModel().getObject().getDisplayName()));

		PageContentHeaderPanel<Site> panel=new PageContentHeaderPanel<Site>(null);
		panel.setTitle( getModel().getObject().getDisplayName());
		panel.setSubLine(new Model<String>("Page"));
		
		MenuBreadCrumbPanel<Void>  bc = new MenuBreadCrumbPanel<Void>();
		
		bc.addElement(new SitesBC());
		
		if (getModel().getObject().getSite()!=null)
			bc.addElement(new SiteDropDownBC(new ObjectModel<Site>(getModel().getObject().getSite())));
		
		bc.addElement(new SitePageBC(getModel()));
		
	 	
		panel.setBreadcrumbPanel(bc);
		
		setSearchPanel(false);
		setAdvancedSearch(false);
		setSuggester(false);
		setPageContentHeader(panel);

		  
		add(new PortalPageStructurePanel("panel", getModel()));
				
	}
	
	public PortalDao getPortalDao() {
		return (PortalDao)ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}

	

	protected Page getPage(PageParameters parameters) {
		
		if (parameters==null)
			return null;
		
		com.novamens.portal6.model.Page site = null;
		
		StringValue id = parameters.get("id");
		if (!id.isNull() && !id.isEmpty()) {
			try {
			site = getPortalDao().findPageById(id.toLong());
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

}
