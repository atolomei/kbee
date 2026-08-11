package kbee.web.payment;

import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPConfException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.Payment;
import com.mercadopago.resources.Preference;
import com.mercadopago.resources.datastructures.preference.Item;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.DomainType;
import com.novamens.service.ServiceLocator;
import kbee.payment.MercadoPagoPaymentService;
import kbee.payment.PaymentActionProcessor;
import kbee.payment.PaymentNotConfirmedException;
import kbee.payment.exception.ActionNotApplicableException;
import kbee.payment.exception.ValidationException;
import kbee.payment.paymentAction.DummyActionProcessor;
import kbee.payment.paymentAction.PremiumDomainPurchaseActionProcessor;
import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ApplicationPage;
import kbee.web.page.ConsoleSectionHomePage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.util.string.StringValue;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

public class TestPaymentPage extends ApplicationPage<Void> {

    public TestPaymentPage() {

        //processPaymentRequestRedirect();

        setTopNavigation(getMainTopbar());
        setMenu(getMainLaternalMenu());



        add(new ButtonMercadoPago("btnPaymentPremium") {

            @Override
            protected Preference setupPreference() {
                Preference preference = new Preference();
                Item item = new Item();
                item.setTitle("1 year kbee licence")
                        .setQuantity(1)
                        .setCurrencyId(Currency.getInstance("UYU").getCurrencyCode())
                        .setUnitPrice((float) 600);
                preference.appendItem(item);
                return preference;
            }


            @Override
            public PaymentActionProcessor getPaymentActionProcessor() {
                return new PremiumDomainPurchaseActionProcessor((Long) getSessionUserProfile().getDomain().getId(), DomainType.PREMIUM.getId());
            }

            @Override
            protected String getPaymentKey() {
                return super.getPaymentKey();
            }
        });


        add(new OrderPanel("orderPanel", "Premium domain upgrade", new BigDecimal(100d), Currency.getInstance("UYU")){

            @Override
            public DummyActionProcessor getPaymentActionProcessor() {
                return new DummyActionProcessor();
            }
        });

        final List<StringValue> parameterValues = this.getRequest().getQueryParameters().getParameterValues("contentid");

        final String contentId = (parameterValues != null) ? parameterValues.get(0).toString() : "123";
        /*add(new ButtonMercadoPago("btnPaymentContent") {

            @Override
            protected Preference setupPreference() {
                Preference preference = new Preference();
                Item item = new Item();
                item.setTitle("Payment  for content " + contentId)
                        .setQuantity(1)
                        .setCurrencyId(Currency.getInstance("UYU").getCurrencyCode())
                        .setUnitPrice((float) 25);
                preference.appendItem(item);
                return preference;
            }

            @Override
            public PaymentActionProcessor getPaymentActionProcessor() {
                return new DummyActionProcessor();
            }

            @Override
            protected String getPaymentKey() {
                return "btnPaymentContent-" + contentId;
            }
        });*/

        add(new OrderPanel("btnPaymentContent", "Payment  for content " + contentId, new BigDecimal(25d), Currency.getInstance("UYU")){

            @Override
            public DummyActionProcessor getPaymentActionProcessor() {
                return new DummyActionProcessor();
            }
            @Override
            protected String getPaymentKey() {
                return "btnPaymentContent-" + contentId;
            }
        });


    }

    public UserProfile getSessionUserProfile() {
        try {
            return ServiceLocator.getService(UserService.class).getSessionUserProfile();
        } catch (Exception e) {
            return null;
        }
    }

    //collection_id=1240161730&
    // collection_status=approved&
    // payment_id=1240161730&
    // status=approved&external_reference=3111779&
    // payment_type=credit_card&
    // merchant_order_id=3151642209&
    // preference_id=33952599-e67da7ec-ea75-4238-af08-e1be3dcf4ecb&
    // site_id=MLU&processing_mode=aggregator&
    // merchant_account_id=null
    /*private void processPaymentRequestRedirect() {
        final List<StringValue> paymentIdValues = this.getRequest().getQueryParameters().getParameterValues("payment_id");
        final List<StringValue> externalReferenceValues = this.getRequest().getQueryParameters().getParameterValues("external_reference");
        final List<StringValue> statusValues = this.getRequest().getQueryParameters().getParameterValues("status");
        if (paymentIdValues != null && externalReferenceValues != null && statusValues != null) {
            if (statusValues.get(0).toString().equals("approved")) {
                try {
                    final MercadoPagoPaymentService paymentService = ServiceLocator.getService(MercadoPagoPaymentService.class);
                    final Payment externalPayment = paymentService.findExternalPayment(paymentIdValues.get(0).toString());
                    if (externalPayment != null) {
                        paymentService.tryProcessPayment(externalPayment);
                    }
                } catch (Exception e) {
                    e.printStack Trace();
                }
            }
        }
    }*/

    @Override
    public void onBeforeRender() {
        super.onBeforeRender();
    }
}
