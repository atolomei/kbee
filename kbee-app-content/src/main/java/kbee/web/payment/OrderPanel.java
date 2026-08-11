package kbee.web.payment;

import com.mercadopago.resources.Preference;
import com.mercadopago.resources.datastructures.preference.Item;
import com.novamens.content.model.Classifier;
import com.novamens.content.user.UserService;
import com.novamens.event.Event;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;
import kbee.payment.Payment;
import kbee.payment.PaymentActionProcessor;
import kbee.payment.paymentAction.DummyActionProcessor;
import kbee.util.CurrencyFormatter;
import kbee.web.console.grid.KbeeTitleColumnPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public abstract class OrderPanel extends KBPanel {

    private String orderItemTitle;
    private BigDecimal price;
    private Currency currency;

    private WebMarkupContainer paymentMethodPanel;
    private WebMarkupContainer alreadyPaidPanel;
    private ButtonMercadoPago buttonMercadoPago;

    public OrderPanel(String orderPanel, String orderItemTitle, BigDecimal price, Currency currency) {
        super(orderPanel);
        this.orderItemTitle = orderItemTitle;
        this.price = price;
        this.currency = currency;
    }

    //Override to setup a payment key
    protected String getPaymentKey(){
        return null;
    }

    public abstract DummyActionProcessor getPaymentActionProcessor();



    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        final boolean alreadyPaid = buttonMercadoPago.isAlreadyPaid();
        alreadyPaidPanel.setVisible(alreadyPaid);
        paymentMethodPanel.setVisible(!alreadyPaid);

    }


    @Override
    protected void onInitialize() {
        super.onInitialize();

        Locale locale =ServiceLocator.getService(UserService.class).getSessionUserLocale();

        add(new Label("itemDescription", this::getOrderItemTitle));
        add(new Label("itemPrice", ()-> CurrencyFormatter.formatAmount(this.getPrice(), this.getCurrency(), locale)));
        add(new Label("totalPrice", ()-> CurrencyFormatter.formatAmount(this.getPrice(), this.getCurrency(), locale)));

        paymentMethodPanel = new WebMarkupContainer("paymentMethodPanel");
        paymentMethodPanel.setOutputMarkupId(true);
        buttonMercadoPago = new ButtonMercadoPago("btnMercadoPagoPayment") {
            @Override
            protected Preference setupPreference() {
                Preference preference = new Preference();
                Item item = new Item();
                item.setTitle(getOrderItemTitle())
                        .setQuantity(1)
                        .setCurrencyId(getCurrency().getCurrencyCode())
                        .setUnitPrice(getPrice().floatValue());
                preference.appendItem(item);
                return preference;
            }

            @Override
            public PaymentActionProcessor getPaymentActionProcessor() {
                return OrderPanel.this.getPaymentActionProcessor();
            }

            @Override
            protected String getPaymentKey() {
                return OrderPanel.this.getPaymentKey();
            }
        };
        paymentMethodPanel.add(buttonMercadoPago);
        add(paymentMethodPanel);



        alreadyPaidPanel = new WebMarkupContainer("alreadyPaidPanel");
        paymentMethodPanel.setOutputMarkupId(true);

        Link<Payment> link = new Link<Payment>("paymentLink", () -> buttonMercadoPago.findKeyConfirmedPayment()) {
            private static final long serialVersionUID = 1L;
            public void onClick() {
                final ClickEvent<Payment> event = new ClickEvent<>(null, getModel() , 0);
                fireScanAll(event);
            }
        };
        link.add(new Label("paymentLinkTitle", () -> link.getModelObject().getTrxReference()));


        alreadyPaidPanel.add(link);
        add(alreadyPaidPanel);
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

    public String getOrderItemTitle() {
        return orderItemTitle;
    }

    public void setOrderItemTitle(String orderItemTitle) {
        this.orderItemTitle = orderItemTitle;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}
