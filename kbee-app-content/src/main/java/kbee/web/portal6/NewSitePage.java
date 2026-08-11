package kbee.web.portal6;


import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.portal6.model.Site;

import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ApplicationMenuSection;

import com.novamens.content.web.nav.markup.GlobalNavigationBar;

public class NewSitePage extends AbstractApplicationPage<NewSiteData> {

	private static final long serialVersionUID = 1L;

	public NewSitePage(IModel<NewSiteData> model) {
		setModel(model);

		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());
		addComponents(getModel());
	}

	public void onEdit(IModel<Person> model) {

	}

	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SITES;
	}

	private void addComponents(IModel<NewSiteData> model) {

		setPageTitle(new StringResourceModel("new-site", NewSitePage.this, null));
		setPageDescription(getPageTitle());

		add(new NewSiteEditor("editor", model) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onEdit(IModel<Site> model) {
				// Panel navigation = new NavigationBar<Site>("navigation");
				// -------------------------------------------------------
				// Editar Un Sitio existente
				// Page page = new NewSitePage(model, navigation, true);
				// setResponsePage(page);
			}

			@Override
			public void onCancel(AjaxRequestTarget target) {
				// Panel navigation = getNavigation();
				// if (navigation instanceof NavigationBar<?>) {
				// ((NavigationBar<?>)navigation).onReturn();
				// }
				setResponsePage(new SitesPage());
			}
		});

		// TODO BREADCRUMB
		// setBreadCrumb(new SiteBC(), new BCElement(new StringResourceModel("new-site",
		// NewSitePage.this, null)));
	}
}
