package kbee.payment.exception;

public class PaymentWithSameKeyAlreadyConfirmedException extends Exception {
    public PaymentWithSameKeyAlreadyConfirmedException() {
        super("A payment with same key have been already confirmed.");

    }
}
