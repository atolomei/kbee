package kbee.payment;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class PaymentPlatformConverter implements AttributeConverter<PaymentPlatform, Integer> {
    @Override
    public Integer convertToDatabaseColumn(PaymentPlatform value) {
        return value != null ? value.getId() : null;
    }

    @Override
    public PaymentPlatform convertToEntityAttribute(Integer integer) {
        return integer != null ? PaymentPlatform.fromId(integer) : null;
    }
}
