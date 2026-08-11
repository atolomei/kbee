package kbee.payment;

import java.util.Locale;
import java.util.ResourceBundle;

public enum PaymentStatus{
    PENDING(0),
    CONFIRMED(1),
    CANCELED(2),
    REFUNDED(3);

    private int id;

    PaymentStatus(int Id) {

        id = Id;
    }

    public int getId() {
        return id;
    }


    public static PaymentStatus fromId(int id) {
        for (PaymentStatus e : values()) {
            if (e.getId() == id) return e;
        }
        return null;
    }

    public String getLabel(Locale locale) {
        ResourceBundle res = ResourceBundle.getBundle(PaymentStatus.this.getClass().getName(), locale);
        return res.getString(this.name());
    }

}
