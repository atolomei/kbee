package kbee.payment;

import com.novamens.security.Identifiable;
import com.novamens.security.User;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Currency;

public interface Payment extends Identifiable {
    Long getId();

    void setId(Long id);

    User getPayer();

    void setPayer(User user);

    PaymentPlatform getPaymentPlatform();

    void setPaymentPlatform(PaymentPlatform paymentPlatform);

    String getRedirectUrl();

    void setRedirectUrl(String redirectUrl);

    BigDecimal getAmount();

    void setAmount(BigDecimal amount);

    void setCurrency(Currency currency);

    Currency getCurrency();

    String getConceptDetail();

    void setConceptDetail(String conceptDetail);

    String getTrxReference();

    void setTrxReference(String trxReference);

    PaymentStatus getStatus();

    void setStatus(PaymentStatus status);


    OffsetDateTime getApprovalDate();

    void setApprovalDate(OffsetDateTime approvalDate);

    OffsetDateTime getProcessDate();

    void setProcessDate(OffsetDateTime processDate);

    OffsetDateTime getCreateDate();

    void setCreateDate(OffsetDateTime createDate);

    OffsetDateTime getLastCheck();

    void setLastCheck(OffsetDateTime lastCheck);

    PaymentActionProcessor getPaymentAction();

    void setPaymentAction(PaymentActionProcessor paymentAction);
}
