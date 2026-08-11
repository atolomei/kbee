package kbee.payment;

import com.mercadopago.resources.Preference;
import com.novamens.service.BusinessSystemService;

import kbee.payment.exception.ActionNotApplicableException;
import kbee.payment.exception.PaymentWithSameKeyAlreadyConfirmedException;
import kbee.payment.exception.ValidationException;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

public interface MercadoPagoPaymentService extends BusinessSystemService {

    void initialize();

    @Transactional
    Payment startPaymentFlow(BigDecimal amount, Currency currency, String key, String details, PaymentActionProcessor paymentActionProcessor,String redirectUrl) throws PaymentWithSameKeyAlreadyConfirmedException;

    @Transactional
    MercadoPagoKbeePayment startPaymentFlow(Preference preference,String key, PaymentActionProcessor paymentActionProcessor,String redirectUrl) throws PaymentWithSameKeyAlreadyConfirmedException;

    @Transactional
    void refundPayment(Payment kbeePayment) throws ValidationException;

    @Transactional
    void tryProcessPayment(Payment kbeePayment) throws ValidationException, PaymentNotConfirmedException, ActionNotApplicableException;

    @Transactional
    void tryProcessPayment(com.mercadopago.resources.Payment mpExternalPayment) throws ValidationException, PaymentNotConfirmedException, ActionNotApplicableException;

    Preference findExternalPreference(String preferenceId);

    List<com.mercadopago.resources.Payment> findExternalPaymentByRef(String trxReference);

    com.mercadopago.resources.Payment findExternalPayment(String paymentId);

    Payment findPaymentByTrxReference(String trxReference);

    List<Payment> findKbeePaymentByKey(String key);

    void processPendingPayments();
}
