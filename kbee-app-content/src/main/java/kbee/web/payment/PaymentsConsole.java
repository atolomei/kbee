package kbee.web.payment;


import com.novamens.content.entity.Person;
import com.novamens.content.library.Library;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserSelfService;
import com.novamens.content.user.UserService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.*;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.model.ObjectModel;
import kbee.payment.MercadoPagoPaymentService;
import kbee.payment.Payment;
import kbee.payment.PaymentNotConfirmedException;
import kbee.payment.PaymentStatus;
import kbee.util.CurrencyFormatter;
import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.Console;
import kbee.web.console.grid.LinkPredicateKbeeGridColumn;
import kbee.web.library.LibrariesConsole;
import kbee.web.library.LibrariesPage;
import kbee.web.object.ObjectStatusColumn;
import kbee.web.report.Row;
import kbee.web.security.user.UsersConsole;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.danekja.java.util.function.serializable.SerializableSupplier;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PaymentsConsole extends AbstractFacetedConsole<Payment> {
    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PaymentsConsole.class.getName());

    private List<GridColumn<SearchResult, String>> columns;

    public PaymentsConsole(Query query) {
        super("payments", query);
    }
    
    
    @Override
	protected String getIcon(IModel<Payment> model) {
		return null;
	}

    @Override
    public List<GridColumn<SearchResult, String>> getColumns() {
        if (this.columns != null)
            return this.columns;

        this.columns = new ArrayList<GridColumn<SearchResult, String>>();
        final Locale sessionUserLocale = ServiceLocator.getService(UserService.class).getSessionUserLocale();

        {
            LinkPredicateKbeeGridColumn<Payment> trxReferenceColumn =
                    new LinkPredicateKbeeGridColumn<Payment>("trxReference", getLabel("payment.trxReference"), col -> col.getTrxReference(), col -> getModel(col));
            trxReferenceColumn.setContextKey(this.getName() + trxReferenceColumn.getContextKey());
            columns.add(trxReferenceColumn);
        }
        {
            KbeePredicateGridColumn<Payment> conceptDetailColumn = new KbeePredicateGridColumn<>("payer", getLabel("payment.payer"), (obj) -> obj.getPayer().getFirstLastName());
            conceptDetailColumn.setContextKey(this.getName() + conceptDetailColumn.getContextKey());
            columns.add(conceptDetailColumn);
        }
        {
            KbeePredicateGridColumn<Payment> conceptDetailColumn = new KbeePredicateGridColumn<>("conceptDetail", getLabel("payment.conceptDetail"), (obj) -> obj.getConceptDetail());
            conceptDetailColumn.setContextKey(this.getName() + conceptDetailColumn.getContextKey());
            columns.add(conceptDetailColumn);
        }

        {

            KbeePredicateGridColumn<Payment> conceptDetailColumn = new KbeePredicateGridColumn<>("amount", getLabel("payment.amount"), (obj) -> CurrencyFormatter.formatAmount(obj.getAmount(), obj.getCurrency(), sessionUserLocale));
            conceptDetailColumn.setContextKey(this.getName() + conceptDetailColumn.getContextKey());
            columns.add(conceptDetailColumn);
        }
        {
            KbeePredicateGridColumn<Payment> statusColumn = new KbeePredicateGridColumn<>("status", getLabel("payment.status"), (obj) -> obj.getStatus().getLabel(sessionUserLocale));
            statusColumn.setContextKey(this.getName() + statusColumn.getContextKey());
            columns.add(statusColumn);
        }

        SerializableSupplier<String> formatSupplier = () -> this.getBrowser().getPanel(GridPanel.class).getDateFormat();
        {
            DateKbeeColumn<Payment> approvalDateColumn = new DateKbeeColumn<Payment>("approvalDate", getLabel("payment.approvalDate"), (obj) -> obj.getApprovalDate(), formatSupplier);
            columns.add(approvalDateColumn);
        }

        {
            DateKbeeColumn<Payment> processDateColumn = new DateKbeeColumn<Payment>("processDate", getLabel("payment.processDate"), (obj) -> obj.getProcessDate(), formatSupplier);
            columns.add(processDateColumn);
        }





        {
            KbeePredicateGridColumn<Payment> platformColumn = new KbeePredicateGridColumn<>("platform", getLabel("payment.platform"), (obj) -> obj.getPaymentPlatform().getLabel(sessionUserLocale));
            platformColumn.setContextKey(this.getName() + platformColumn.getContextKey());
            columns.add(platformColumn);
        }

        return this.columns;
    }


    @Override
    protected void addListeners() {
        super.addListeners();
        add(new WicketEventListener<ClickEvent<Payment>>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void onEvent(ClickEvent<Payment> event) {
                final PaymentDetailsPage paymentDetailsPage = new PaymentDetailsPage(event.getModel());
                setResponsePage(paymentDetailsPage);
            }
        });

    }

    @Override
    public Page getConsolePage(Query query, long index) {
        return new PaymentsConsolePage(query);
    }

    protected IModel<Payment> getModel(Payment object) {
        return new ObjectModel<Payment>(object, true);
    }

    @Override
    protected Panel getMenu(IModel<Payment> model) {
        ContextMenuPanel<Payment> menu = new ContextMenuPanel<>(model);

        menu.addItem(new MenuItemFactory<Payment>() {
            @Override
            public AbstractMenuItemPanelV5<Payment> getItem(String id) {
                return new AjaxMenuItemPanelV5<Payment>(id) {
                    public void onClick(AjaxRequestTarget target) {
                        try {

                            ServiceLocator.getService(MercadoPagoPaymentService.class).tryProcessPayment(getModelObject());
                            FeedbackHelper.showInfoToast(PaymentsConsole.this.getLabel("PaymentConfirmed").getObject());
                        } catch (PaymentNotConfirmedException e) {
                            logger.error(e);
                            FeedbackHelper.showErrorToast(PaymentsConsole.this.getLabel("PaymentCouldNotBeConfirmed").getObject());
                        }catch (Exception e) {
                            logger.error(e);
                            FeedbackHelper.showErrorToast(e.getClass().getName(), e.getMessage());
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getConsoleLabel("payment.reprocess").getObject();
                    }

                    @Override
                    public boolean isEnabled() {
                        return getModelObject().getStatus() == PaymentStatus.PENDING;
                    }
                };
            }
        });

        return menu;
    }

    @Override
    public Query newQuery() {
        return new PaymentsQuery();
    }
}
