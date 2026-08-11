package kbee.payment;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class PaymentStatusConverter implements AttributeConverter<PaymentStatus, Integer> {
    @Override
    public Integer convertToDatabaseColumn(PaymentStatus value) {
        return value!= null ? value.getId() : null;
    }

    @Override
    public PaymentStatus convertToEntityAttribute(Integer integer) {
        return integer != null ? PaymentStatus.fromId(integer) : null;
    }
}
