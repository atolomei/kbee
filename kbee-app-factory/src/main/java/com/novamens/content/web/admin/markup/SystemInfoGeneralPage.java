package com.novamens.content.web.admin.markup;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.entity.Person;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorPanel;
import kbee.web.nav.HomeBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;
import kbee.web.page.FactoryPage;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.service.ApplicationSiteMapService;
import kbee.web.systeminfo.FactorySystemInfoDropdownBC;

public class SystemInfoGeneralPage extends ApplicationPage<Person> implements FactoryPage {

    private static final long serialVersionUID = 1L;

    static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SystemInfoGeneralPage.class.getName());

    String name;

    public SystemInfoGeneralPage(PageParameters parameters) {
        StringValue id = parameters.get("id");
        if (id != null)
            name = id.toOptionalString();
        init(name);
    }

    /**
     * 
     * @param panel
     * @param key
     */

    /**
     * @param key
     */
    public SystemInfoGeneralPage(String key) {
        this.name = key;
        getPageParameters().add("id", key);
        init(key);
    }

    /**
     * @param key
     */
    private void init(String key) {

        logger.debug(name);

        setPageTitle(new Model<String>(key));

        Person person = getPerson();

        if (person != null) {
            if (hasPermissions()) {
                setTopNavigation(getMainTopbar());
                setMenu(getMainLaternalMenu());
                setModel(new ObjectModel<Person>(person));

                /** see -> KbeeApplicationSiteMapService */
                add(ServiceLocator.getService(ApplicationSiteMapService.class).getFactoryPanel("editor", key));
            } else
                addOrReplace(new ErrorPanel("editor", "not authorized", ""));
        } else
            addOrReplace(new ErrorPanel("editor", "person not found", ""));

        PageContentHeaderPanel<Domain> panel = new PageContentHeaderPanel<Domain>(null);
        setPageTitle(new Model<String>(key));
        panel.setTitle(new Model<String>(key));
        panel.setBreadcrumbPanel(getBreadcrumbPanel());
        setSearchPanel(false);
        setClearAllSearch(false);
        setAdvancedSearch(false);
        setSuggester(false);
        setPageContentHeader(panel);

    }

    protected Panel getBreadcrumbPanel() {
        MenuBreadCrumbPanel<?> bc = new MenuBreadCrumbPanel<>();

        bc.addElement(new HomeBC());

        bc.addElement(new FactorySystemInfoDropdownBC());
        bc.addElement(new BCElement(getPageTitle()));
        return bc;
    }

    protected String getName() {
        return name;
    }

    final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();

    @Override
    public String getPageHelpKey() {
        return super.getPageHelpKey() + "-" + getName();
    }

    @Override
    public ApplicationMenuSection getApplicationMenuSection() {
        return ApplicationMenuSection.INFO;
    }

    @Override
    public boolean hasPermissions() {
        return isDomainKbee() || is_root;
    }
}
