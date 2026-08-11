package kbee.payment;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import kbee.payment.exception.ActionNotApplicableException;
import kbee.payment.exception.ValidationException;


@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
public interface PaymentActionProcessor {

    void perform();

    void validate() throws ValidationException;

    void checkIsApplicable() throws ActionNotApplicableException;

    String getPaymentKey();
}
