package kbee.web.payment;

import com.novamens.content.base.Content;
import com.novamens.content.model.ObjectId;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;
import kbee.payment.Payment;
import kbee.web.error.ErrorPanel;
import kbee.web.nav.DomainsBC;
import kbee.web.nav.SettingsBC;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

public class PaymentDetailsPage extends ApplicationPage<Payment> {

    final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
    final boolean is_domain_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

    public PaymentDetailsPage(PageParameters parameters) {
        super();
        Payment payment = getPayment(parameters);
        if (payment != null) {
            setModel(new ObjectModel<Payment>(payment));

        }
    }

    public PaymentDetailsPage(IModel<Payment> model) {
        super(model);
        getPageParameters().set("id", model.getObject().getId().toString());
    }

    @Override
    public void onInitialize() {
        super.onInitialize();


        setTopNavigation(getMainTopbar());
        setMenu(getMainLaternalMenu());

        PageContentHeaderPanel<Void> panel = new PageContentHeaderPanel<Void>(null);
        panel.setTitle(getLabel("paymentDetails"));
        MenuBreadCrumbPanel<?> bc = new MenuBreadCrumbPanel<Void>();
        bc.addElement(new BCElement(getLabel("payments")));
        bc.addElement(new BCElement(getLabel("paymentDetails")));
        panel.setBreadcrumbPanel(bc);

        setSearchPanel(false);
        setClearAllSearch(false);
        setAdvancedSearch(false);
        setSuggester(false);
        setPageContentHeader(panel);

        if (getModel() != null && hasPermissions() )
            add(new PaymentDetailsMainPanel("paymentDetailsMainPanel", getModel()));
        else
            add(new ErrorPanel("info-panel", "Payment not found", ""));
    }

    @Override
    protected boolean hasPermissions() {
        boolean hasPermission = super.hasPermissions();
        hasPermission |= is_root || is_domain_admin;
        hasPermission |= (getModel() != null && getModel().isPresent().getObject()) && getModel().getObject().getPayer().equals(getSessionUser());

        return hasPermission;
    }




    protected Payment getPayment(PageParameters parameters) {
        Payment payment = null;

        StringValue id = parameters.get("id");
        if (!id.isNull() && !id.isEmpty())
            payment = getContentDao().findPaymentById(Long.valueOf(id.toString()));
        return payment;
    }
}
