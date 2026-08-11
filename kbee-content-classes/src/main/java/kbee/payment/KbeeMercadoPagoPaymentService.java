package kbee.payment;

import com.mercadopago.MercadoPago;
import com.mercadopago.core.MPResourceArray;
import com.mercadopago.exceptions.MPConfException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.Payment;
import com.mercadopago.resources.Preference;
import com.mercadopago.resources.datastructures.preference.Item;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

import kbee.payment.exception.ActionNotApplicableException;
import kbee.payment.exception.PaymentWithSameKeyAlreadyConfirmedException;
import kbee.payment.exception.ValidationException;
import kbee.util.PropertiesFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

public class KbeeMercadoPagoPaymentService implements MercadoPagoPaymentService {

    private ContentDao contentDao;
    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeMercadoPagoPaymentService.class.getName());
    static private Logger txlogger = LogManager.getLogger("TxLogger");


    @Override
    public void initialize(){
        final String mlAccessToken = PropertiesFactory.getInstance("kbee").getProperties().getProperty("mercadopago.accesstoken", null);
       if(mlAccessToken != null) {
           try {
               logger.info("Setting Mercado Pago access token.");
               MercadoPago.SDK.setAccessToken(mlAccessToken);
               logger.info("Mercado Pago access token set.");
           } catch (MPConfException e) {
               logger.error(e);
           }
       }else{
           logger.info("Mercado Pago access token not set.");
       }
    }


    @Override
    @Transactional
    public kbee.payment.Payment startPaymentFlow(BigDecimal amount, Currency currency,String key, String details, PaymentActionProcessor paymentActionProcessor, String redirectUrl) throws PaymentWithSameKeyAlreadyConfirmedException {
        Preference preference = new Preference();
        Item item = new Item();
        item.setTitle(details)
                .setQuantity(1)
                .setCurrencyId(currency.getCurrencyCode())
                .setUnitPrice(amount.floatValue());
        preference.appendItem(item);

        return startPaymentFlow(preference,key, paymentActionProcessor,redirectUrl);
    }

    @Override
    @Transactional
    public MercadoPagoKbeePayment startPaymentFlow(Preference preference, String key, PaymentActionProcessor paymentActionProcessor, String redirectUrl) throws PaymentWithSameKeyAlreadyConfirmedException {
        if(key!=null)
            checkPaymentNotAlreadyDone(key);

        MercadoPagoKbeePayment payment = new MercadoPagoKbeePayment();

        final Float totalAmount = getTotalAmount(preference);
        if (totalAmount <= 0)
            throw new KbeeRuntimeException("Payments must have an amount greater than zero.");


        payment.setKey(key);
        payment.setAmount(new BigDecimal(totalAmount));
        payment.setCurrency(getValidatedCurrency(preference));
        payment.setRedirectUrl(redirectUrl);
        payment.setCreateDate(OffsetDateTime.now());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentPlatform(PaymentPlatform.MERCADO_PAGO);

        payment.setConceptDetail(getConceptDetails(preference));
        final UserProfile sessionUserProfile = getSessionUserProfile();
        payment.setPayer(sessionUserProfile.getUser());
        payment.setDomain(sessionUserProfile.getDomain());

        payment.setPaymentAction(paymentActionProcessor);

        getContentDao().save(payment);
        getContentDao().flush();

        payment.setTrxReference(payment.getId().toString());


        try {
            preference.setExternalReference(payment.getTrxReference());
            preference.save();
            payment.setPreference(preference);
        } catch (MPException e) {
            throw new KbeeRuntimeException("Exception thrown while saving preference.", e);
        }

        txlogger.info(new PaymentEvent(payment));

        return payment;
    }

    private void checkPaymentNotAlreadyDone(String paymentKey) throws PaymentWithSameKeyAlreadyConfirmedException {
        final List<kbee.payment.Payment> kbeePaymentByKey = this.findKbeePaymentByKey(paymentKey);
        boolean done= kbeePaymentByKey.stream().anyMatch(p -> p.getStatus() == PaymentStatus.CONFIRMED);
        if(done)
            throw new PaymentWithSameKeyAlreadyConfirmedException();
    }

    @Override
    @Transactional
    public void refundPayment(kbee.payment.Payment kbeePayment) throws ValidationException {
        if(kbeePayment.getStatus() != PaymentStatus.PENDING)
            throw new ValidationException("Only pending payments can be cancelled.");

        if(!(kbeePayment instanceof MercadoPagoKbeePayment)){
            throw new ValidationException("Incorrect payment instance type.");
        }

        final Payment mpPayment = ((MercadoPagoKbeePayment) kbeePayment).getMPPayment();


        try {
            mpPayment.refund();
        } catch (MPException e) {
            throw new KbeeRuntimeException("Exception while executing payment refund.", e);
        }
        kbeePayment.setStatus(PaymentStatus.CANCELED);
        getContentDao().save(kbeePayment);

    }




    @Override
    @Transactional
    public void tryProcessPayment(kbee.payment.Payment kbeePayment) throws ValidationException, PaymentNotConfirmedException, ActionNotApplicableException {
        try {

            final List<Payment> payments = findExternalPaymentByRef(kbeePayment.getTrxReference());
            final Optional<Payment> approvedMLPayment = payments.stream().filter(p -> p.getStatus() == Payment.Status.approved).findFirst();

            if(!approvedMLPayment.isPresent()) {
                throw new PaymentNotConfirmedException();
            }

            confirmPayment(kbeePayment, approvedMLPayment.get());
        }catch(Exception e){
            logger.error(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void tryProcessPayment(Payment mpExternalPayment) throws ValidationException, PaymentNotConfirmedException, ActionNotApplicableException {
        try {
            logger.debug("Starting to process MercadoPago external payment id:" +mpExternalPayment.getId());
            if(mpExternalPayment.getStatus() != Payment.Status.approved){
                throw new PaymentNotConfirmedException();
            }

            if(mpExternalPayment.getExternalReference() ==null){
                throw new ValidationException("Payment without external reference cannot be processed.");
            }

            logger.debug("Searching payment for trx reference: mpExternalPayment.getExternalReference()");
            final kbee.payment.Payment kbeePayment = getContentDao().findPaymentsByTrxReference(mpExternalPayment.getExternalReference());

            if(kbeePayment==null){
                throw new ValidationException("Cannot find a pending payment with same trxReference.");
            }

            if(kbeePayment.getStatus() != PaymentStatus.PENDING){
                throw new ValidationException("Found Payment is not pending.");
            }


            confirmPayment(kbeePayment, mpExternalPayment);
        }catch(Exception e){
            logger.error(e);
            throw e;
        }
    }

    private void confirmPayment(kbee.payment.Payment kbeePayment,  Payment mpExternalPayment) throws ValidationException, PaymentNotConfirmedException, ActionNotApplicableException {
        logger.info("Confirming payment external id:" + mpExternalPayment.getId()+ " internal id:" +kbeePayment.getId());

        if(mpExternalPayment.getStatus() != Payment.Status.approved){
            throw new PaymentNotConfirmedException();
        }

        if(kbeePayment.getStatus() != PaymentStatus.PENDING){
            throw new ValidationException("Payment status different from pending.");
        }

        if(!(kbeePayment instanceof MercadoPagoKbeePayment)){
            throw new ValidationException("Incorrect payment instance type.");
        }
        MercadoPagoKbeePayment mpKbeePayment = (MercadoPagoKbeePayment) kbeePayment;
        mpKbeePayment.setPayment(mpExternalPayment);

        final PaymentActionProcessor paymentAction = mpKbeePayment.getPaymentAction();
        paymentAction.validate();
        paymentAction.checkIsApplicable();


        OffsetDateTime approvalDate = mpExternalPayment.getDateApproved().toInstant().atOffset(ZoneOffset.UTC);
        mpKbeePayment.setApprovalDate(approvalDate);
        mpKbeePayment.setProcessDate(OffsetDateTime.now());

        mpKbeePayment.setStatus(PaymentStatus.CONFIRMED);
        logger.info("Payment confirmed external id:" + mpKbeePayment.getMPPayment().getId()+ " internal id:" +mpKbeePayment.getId());
        logger.info("Performing payment action." + paymentAction.toString());
        paymentAction.perform();
        logger.info("Payment action performed.");
        getContentDao().save(kbeePayment);
    }




    @Override
    public Preference findExternalPreference(String preferenceId) {
        try {
            return Preference.findById(preferenceId);
        } catch (MPException e) {
            throw new KbeeRuntimeException("Exception thrown while searching preference.", e);
        }
    }

    @Override
    public List<Payment> findExternalPaymentByRef(String trxReference) {
        try {
            HashMap<String, String> params = new HashMap<>();
            params.put("external_reference", trxReference);

            final MPResourceArray result = Payment.search(params, false);

            List<Payment> payments = new ArrayList<>();
            for(int idx = 0; idx < result.size(); idx++){
                payments.add(result.getByIndex(idx));
            }
            return payments;
        } catch (MPException e) {
            throw new KbeeRuntimeException("Exception thrown while searching payment.", e);
        }
    }

    @Override
    public Payment findExternalPayment(String paymentId) {
        try {
            return Payment.findById(paymentId, false);

        } catch (MPException e) {
            throw new KbeeRuntimeException("Exception thrown while searching payment.", e);
        }
    }

    @Override
    public kbee.payment.Payment findPaymentByTrxReference(String trxReference){
        return getContentDao().findPaymentsByTrxReference(trxReference);
    }

    @Override
    public List<kbee.payment.Payment> findKbeePaymentByKey(String key) {
            return getContentDao().findPaymentsByKey(key, true);

    }


    @Override
    public void processPendingPayments() {
        final List<kbee.payment.Payment> paymentsPending = getContentDao().findPaymentsPending(OffsetDateTime.now().minus(Duration.ofDays(30)), 500);
        for (kbee.payment.Payment payment : paymentsPending) {
            try {
                tryProcessPayment(payment);
            } catch (Exception e) {
               logger.error(e);
            }
        }

    }

    private Float getTotalAmount(Preference preference) {
        final Float itemsPrices = preference.getItems().stream().map(it -> it.getUnitPrice() * it.getQuantity()).reduce(0f, Float::sum);
        return itemsPrices;
    }

    private Currency getValidatedCurrency(Preference preference) {
        final long count = preference.getItems().stream().map(it -> it.getCurrencyId()).distinct().count();
        if (count != 1)
            throw new KbeeRuntimeException("All items must have the same valid currency.");

        final String firstCurrencyId = preference.getItems().get(0).getCurrencyId();
        return Currency.getInstance(firstCurrencyId);
    }

    private String getConceptDetails(Preference preference) {
        final int itmCount = preference.getItems().size();
        if (itmCount == 1)
            return preference.getItems().get(0).getTitle();
        else if (itmCount > 1)
            return preference.getItems().stream().map(it -> "* " + it.getTitle()).collect(Collectors.joining("\n"));
        else
            throw new KbeeRuntimeException("At least one item is required.");
    }


    private UserProfile getSessionUserProfile() {
        try {
            return ServiceLocator.getService(UserService.class).getSessionUserProfile();
        } catch (Exception e) {
            return null;
        }
    }

    public ContentDao getContentDao() {
        return contentDao;
    }

    public void setContentDao(ContentDao contentDao) {
        this.contentDao = contentDao;
    }
}
