package kbee.web.payment;

import com.novamens.content.library.Library;
import com.novamens.content.model.UserSet;
import com.novamens.content.web.console.markup.ErrorPanel;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;
import kbee.payment.Payment;
import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.library.LibrariesConsole;
import kbee.web.library.LibrariesPage;
import kbee.web.model.DataSetsPage;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.security.user.UsersPage;
import org.apache.wicket.Page;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

public class PaymentsConsolePage extends ConsolePage<Payment> {
    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PaymentsConsolePage.class.getName());

    final boolean is_support		= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
    final boolean is_root			= ServiceLocator.getService(SecurityService.class).isRoot();
    final boolean is_domain_admin	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

    public PaymentsConsolePage() {
        this(null);
    }

    public PaymentsConsolePage(Query query) {
        super(query);
    }


    @Override
    public void onInitialize() {
        super.onInitialize();

        try {
            setPageTitle(getLabel("payments"));

            PageContentHeaderPanel<Void> panel = new PageContentHeaderPanel<Void>(null);
            panel.setTitle(getLabel("payments"));
            MenuBreadCrumbPanel<?> bc = new MenuBreadCrumbPanel<Void>();
            bc.addElement(new BCElement(getLabel("payments")));
            bc.addElement(new BCElement(getLabel("payments")));
            panel.setBreadcrumbPanel(bc);

            setSearchPanel(false);
            setAdvancedSearch(false);
            setSuggester(false);
            panel.setSearchPanel(getSearchPanel());
            setPageContentHeader(panel);
        }
        catch (Exception e) {
            logger.error(e);
            addOrReplace( new ErrorPanel("console", new Model<String>(e.getClass().getName()),
                    new Model<String>( this.getClass().getName() + " | " + e.getMessage() + " | " + e.getCause())));
            setTopNavigation(new InvisiblePanel("navigation"));
        }
    }

    @Override
    protected Page getConsolePage(Query query, long index) {
        return new PaymentsConsolePage(query);
    }

    @Override
    public boolean hasPermissions() {
        return is_support || is_root || is_domain_admin;
    }

    @Override
    public Console<Payment> newConsole(Query query) {
        return new PaymentsConsole(query) {
            @Override
            public Page getConsolePage(Query query, long index) {
                return PaymentsConsolePage.this.getConsolePage(query, index);
            }
        };
    }
}
