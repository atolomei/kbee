package kbee.payment;

import com.mercadopago.resources.Payment;
import com.mercadopago.resources.Preference;
import com.novamens.service.ServiceLocator;

import javax.persistence.*;

@Entity
@DiscriminatorValue("1")
@Table(name = "payment_mercadopago")
public class MercadoPagoKbeePayment extends KbeePayment {

    @Column(name="preference_id")
    private String preferenceId;

    @Column(name="payment_id")
    private String paymentId;


    @Transient
    private volatile Preference preference=null;

    @Transient
    private volatile Payment payment=null;


    public MercadoPagoKbeePayment() {
        super();
       // this.setPaymentPlatform(PaymentPlatform.MERCADO_PAGO);
    }

    public Preference getPreference() {
        if (this.preferenceId == null) {
            return null;
        }

        if (this.preference != null)
            preference =ServiceLocator.getService(KbeeMercadoPagoPaymentService.class).findExternalPreference(this.preferenceId);

        return preference;

    }

    public void setPreference(Preference preference) {
        this.preference = preference;
        this.setPreferenceId(preference.getId());
    }


    public Payment getMPPayment() {
        if (this.paymentId == null) {
            return null;
        }
        if (this.payment == null)
            payment = ServiceLocator.getService(KbeeMercadoPagoPaymentService.class).findExternalPayment(this.paymentId);

        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
        this.paymentId = payment.getId();
    }

    public String getPreferenceId() {
        return preferenceId;

    }

    public void setPreferenceId(String preferenceId) {
        if(preference!=null && !preferenceId.equals(preference.getId())) {
            preference = null;
        }
        this.preferenceId = preferenceId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
        if(payment!=null && !paymentId.equals(payment.getId()))
            payment=null;
    }
}
