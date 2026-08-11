package kbee.web.payment;

import com.novamens.content.user.UserService;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.service.ServiceLocator;
import kbee.payment.Payment;
import kbee.util.CurrencyFormatter;
import kbee.web.command.panel.CommandAttributePanelV5;
import org.apache.poi.ss.formula.functions.T;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;
import java.util.Optional;

public class PaymentDetailsMainPanel extends ModelPanel<Payment> {

    private ArrayList<Panel> panels;

    public PaymentDetailsMainPanel(String id, IModel<Payment> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        IModel<String> kcss = new Model<String>("col-lg-3 col-md-7 col-xs-7 keyc");
        IModel<String> vcss = new Model<String>("col-lg-9 col-md-5 col-xs-5 valuec");

        final Locale locale = ServiceLocator.getService(UserService.class).getSessionUserLocale();


        this.panels = new ArrayList<Panel>();

        final String amount = CurrencyFormatter.formatAmount(getModelObject().getAmount(), getModelObject().getCurrency(), locale);


        this.panels.add(new CommandAttributePanelV5("row", getLabel("payment.trxReference"), () -> getModelObject().getTrxReference(), kcss, vcss));
        this.panels.add(new CommandAttributePanelV5("row", getLabel("payment.conceptDetail"), () -> getModelObject().getConceptDetail(), kcss, vcss));
        this.panels.add(new CommandAttributePanelV5("row", getLabel("payment.amount"), () -> amount, kcss, vcss));
        this.panels.add(new CommandAttributePanelV5("row", getLabel("payment.status"), () -> getModelObject().getStatus().getLabel(locale), kcss, vcss));
        this.panels.add(new CommandAttributePanelV5("row", getLabel("payment.approvalDate"), getModelObject().getApprovalDate(), kcss, vcss));
        this.panels.add(new CommandAttributePanelV5("row", getLabel("payment.processDate"), getModelObject().getProcessDate(), kcss, vcss));
        this.panels.add(new CommandAttributePanelV5("row", getLabel("payment.platform"), () -> getModelObject().getPaymentPlatform().getLabel(locale), kcss, vcss));
        this.panels.add(new CommandAttributePanelV5("row", getLabel("payment.payer"), () -> getModelObject().getPayer().getFirstLastName(), kcss, vcss));

        add(new ListView<Panel>("paymentAttributes", this.panels) {
            private static final long serialVersionUID = 1L;

            protected void populateItem(ListItem<Panel> item) {
                item.setOutputMarkupId(true);
                item.add(item.getModelObject());
                item.setVisible(item.getModelObject().isVisible());
            }
        });
    }


}
