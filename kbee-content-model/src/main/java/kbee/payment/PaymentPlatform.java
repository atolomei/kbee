package kbee.payment;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Locale;
import java.util.ResourceBundle;

public enum PaymentPlatform {
    MERCADO_PAGO(1, "Mercado Pago");

    private int id;
    private String label;

    PaymentPlatform(int Id,String label) {

        id = Id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public static PaymentPlatform fromId(int id) {
        for (PaymentPlatform e : values()) {
            if (e.getId() == id) return e;
        }
        return null;
    }

    public String getLabel(Locale locale) {
        return label;
    }


}
