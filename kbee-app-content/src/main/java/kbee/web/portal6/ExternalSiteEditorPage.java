package kbee.web.portal6;


import org.apache.wicket.ajax.AjaxRequestTarget;
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

import com.novamens.content.web.nav.markup.GlobalNavigationBar;

public class ExternalSiteEditorPage extends AbstractApplicationPage<Site> {

	private static final long serialVersionUID = 1L;

	IModel<Site> model;

	
	public ExternalSiteEditorPage(PageParameters parameters) {
		Site site = getSite(parameters);
		if (site!=null) {
			setModel(new ObjectModel<Site>(site));
			setTopNavigation(getMainTopbar());  
			setMenu(getMainLaternalMenu());
			addComponents();
		}
	}
	
	public ExternalSiteEditorPage(IModel<Site> model) {
		setModel(model);
		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());
		addComponents();
	}

	private void addComponents() {

		getPageParameters().set("id", model.getObject().getId());
		
		setPageTitle(new StringResourceModel("edit-site", ExternalSiteEditorPage.this, null));
		setPageDescription(getPageTitle());

		add(new ExternalSiteEditor("editor", getModel()) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onEdit(IModel<Site> model) {
				setResponsePage(new SitesPage());
			}

			@Override
			public void onCancel(AjaxRequestTarget target) {
				setResponsePage(new SitesPage());
			}
		});
	}

	public void setModel(IModel<Site> model) {
		this.model = model;
	}

	public IModel<Site> getModel() {
		return model;
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
	
	private PortalDao getPortalDao() {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		return (PortalDao) beans.getBean("portalDao");
	}

}
