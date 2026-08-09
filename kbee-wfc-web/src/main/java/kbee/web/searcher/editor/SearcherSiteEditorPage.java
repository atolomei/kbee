package kbee.web.searcher.editor;


import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.PortalDao;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;


import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ApplicationMenuSection;

public class SearcherSiteEditorPage extends AbstractApplicationPage<Site> {

	private static final long serialVersionUID = 1L;

	private boolean isNew = false;
	
	
	public SearcherSiteEditorPage(PageParameters parameters) {
		Site site = getSite(parameters);
		if (site!=null) {
			setModel(new ObjectModel<Site>(site));
			setTopNavigation(getMainTopbar());
			setMenu(getMainLaternalMenu());
			setLogVisit(true);
			addComponents(getModel());
		}
	}
	
	public SearcherSiteEditorPage(IModel<Site> model) {
		this(model, false);
	}
	
	public SearcherSiteEditorPage(IModel<Site> model, boolean isNew) {
		setModel(model);
		this.isNew=isNew;
		setTopNavigation(getMainTopbar());
		setMenu(getMainLaternalMenu());
		setLogVisit(true);
		addComponents(getModel());
	}


	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SITES;
	}
			
	
	private PortalDao getPortalDao() {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		return (PortalDao) beans.getBean("portalDao");
	}
	
	protected Site getSite(PageParameters parameters) {
		Site site = null;
		StringValue id = parameters.get("id");
		if (!id.isNull() && !id.isEmpty()) {
			site = getPortalDao().findSiteById(id.toLong());
			if (site!=null && !site.getDomain().equals(getDomain())) {
				site = null;
			}
		}	
		return site;
	}

	
	
	
	private void addComponents(IModel<Site> model) {
		setPageTitle(new StringResourceModel("searcher-site", SearcherSiteEditorPage.this, null));
		getPageParameters().set("id", model.getObject().getId());
		setPageDescription(getPageTitle());
		add(new SearcherMainPanel(model, isNew));
	}
}
