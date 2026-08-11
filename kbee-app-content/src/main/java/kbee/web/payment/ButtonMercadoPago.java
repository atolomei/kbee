package kbee.web.payment;

import com.mercadopago.resources.Preference;
import com.mercadopago.resources.datastructures.preference.BackUrls;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;
import kbee.payment.*;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;

import java.util.List;

public abstract class ButtonMercadoPago extends Panel {

    //protected String mlPublicKey;
   // CallFromJavascriptBehavior callback;
    String successBackUrl;
    String pendingBackUrl;
    String failureBackUrl;


    String paymentRedirectUrl;

    public ButtonMercadoPago(String id) {
        super(id);
        //this.mlPublicKey = "TEST-fab2dbf6-0d89-4f30-a493-9f7b53746414";
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
/*
        callback=new CallFromJavascriptBehavior();
        add(callback);*/


        String currentRelativeUrl=RequestCycle.get().urlFor(RequestCycle.get().getActiveRequestHandler()).toString();
        setPaymentRedirectUrl(getAbsoluteUrlForRelative(currentRelativeUrl));

        setFailureBackUrl(getAbsoluteUrlForRelative("./api/mp/paymentFailure"));
        setSuccessBackUrl(getAbsoluteUrlForRelative("./api/mp/paymentSuccess"));
        setPendingBackUrl(getAbsoluteUrlForRelative("./api/mp/paymentPending"));



        add(new AjaxLink<Void>("link") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                Transaction transaction=null;

                try {
                    transaction = beginTransaction();
                    final PaymentActionProcessor paymentActionProcessor = getPaymentActionProcessor();

                    paymentActionProcessor.checkIsApplicable();

                    final MercadoPagoPaymentService paymentService = ServiceLocator.getService(MercadoPagoPaymentService.class);
                    Preference preference = setupPreference();


                    preference.setAutoReturn(Preference.AutoReturn.approved);
                    preference.setBackUrls(new BackUrls(getSuccessBackUrl(), getPendingBackUrl(), getFailureBackUrl()));

                    final MercadoPagoKbeePayment mercadoPagoKbeePayment = paymentService.startPaymentFlow(preference, getPaymentKey(),paymentActionProcessor, getPaymentRedirectUrl());
                    //ajaxRequestTarget.appendJavaScript(getCheckoutScript(preference.getId(), ButtonMercadoPago.this.mlPublicKey));
                    transaction.commit();
                    ajaxRequestTarget.appendJavaScript(getCheckoutScript(preference));
                }catch (Exception e){
                    FeedbackHelper.showErrorToast(e.getClass().getSimpleName(), e.getMessage());
                    if(transaction!=null && !transaction.isCompleted())
                        transaction.rollback();
                }
            }
        });

        //add(getPaidLabel());

    }

    private String getAbsoluteUrlForRelative(String relativeUrl){
        return RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(relativeUrl));
    }

    public abstract PaymentActionProcessor getPaymentActionProcessor();


    public String getPaymentRedirectUrl() {
        return paymentRedirectUrl;
    }

    public void setPaymentRedirectUrl(String paymentRedirectUrl) {
        this.paymentRedirectUrl = paymentRedirectUrl;
    }

    public String getSuccessBackUrl() {
        return successBackUrl;
    }

    public void setSuccessBackUrl(String successBackUrl) {
        this.successBackUrl = successBackUrl;
    }

    public String getPendingBackUrl() {
        return pendingBackUrl;
    }

    public void setPendingBackUrl(String pendingBackUrl) {
        this.pendingBackUrl = pendingBackUrl;
    }

    public String getFailureBackUrl() {
        return failureBackUrl;
    }

    public void setFailureBackUrl(String failureBackUrl) {
        this.failureBackUrl = failureBackUrl;
    }

    protected Transaction beginTransaction()  {
        return ServiceLocator.getService(TransactionService.class).beginTransaction(false);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
    }

    /*
    private Label getPaidLabel(){
        final Label isPaidLabel = new Label("isPaid", isAlreadyPaid() ? "Paid" : "Not Paid");
        isPaidLabel.setOutputMarkupId(true);
        return isPaidLabel;

    }*/

    public boolean isAlreadyPaid(){
        return findKeyConfirmedPayment() != null;
    }

    public Payment findKeyConfirmedPayment(){
        Payment payment = null;
        String paymentKey = getPaymentKey();
        if(paymentKey != null) {
            final List<Payment> kbeePaymentByKey = ServiceLocator.getService(MercadoPagoPaymentService.class).findKbeePaymentByKey(paymentKey);
            payment= kbeePaymentByKey.stream().filter(p -> p.getStatus() == PaymentStatus.CONFIRMED).findFirst().orElse(null);
        }
        return payment;
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
       // addOrReplace(getPaidLabel());
    }

    protected String getPaymentKey(){
        return null;
    }

    protected abstract Preference setupPreference();
/*
    private String getCheckoutScript(String mlIdPreference, String mlPublicKey) {
        return "new MercadoPago('" + mlPublicKey + "', {" +
                "        locale: 'es-UY'" +
                "  }).checkout({" +
                "      preference: {" +
                "          id: '" + mlIdPreference + "'" +
                "      }," +
                "      autoOpen: true" +
                "});";
    }*/

    private String getCheckoutScript(Preference preference) {
        return "window.location.href = '" + preference.getInitPoint() +"';";
    }
/*
    public class CallFromJavascriptBehavior extends AbstractDefaultAjaxBehavior {
        @Override
        protected void respond(AjaxRequestTarget target) {
            final StringValue parameterValue = RequestCycle.get().getRequest().getQueryParameters().getParameterValue("yourName");
            System.out.pr intln(String.format("Hello %s", parameterValue.toString()));

            setResponsePage(this.getComponent().getPage());
        }

    }*/

}
