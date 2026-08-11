package kbee.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novamens.content.document.TreeFile;
import com.novamens.logging.AbstractLogEvent;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;

@Entity
@DiscriminatorValue("Payment")
public class PaymentEvent extends AbstractLogEvent {

	static ObjectMapper mapper = new ObjectMapper();
	
    @Column(name = "EVENT_TITLE")
    private String title;

    @Column(name = "EVENT_DOMAIN_ID")
    private Long domainId;

    public PaymentEvent() {
    }

    public PaymentEvent(MercadoPagoKbeePayment payment) {

        setAuditSet(AuditSet.GENERAL);

        HashMap<String,Object> parameters= new HashMap<>();
        parameters.put("id", payment.getPaymentId());
        parameters.put("amount", payment.getAmount());


        parameters.put("currency", payment.getCurrency());
        parameters.put("processDate", payment.getProcessDate());
        parameters.put("approvalDate", payment.getApprovalDate());
        parameters.put("conceptDetail", payment.getConceptDetail());
        parameters.put("trxReference", payment.getTrxReference());
        setTitle(payment.getConceptDetail());

        setDomainId((Long) payment.getDomain().getId());
        try {
            
            String jsonParameters = mapper.writeValueAsString(parameters);
            setParameters(jsonParameters);
        } catch (JsonProcessingException e) {
            throw new KbeeRuntimeException(e);
        }


        final User sessionUser = ServiceLocator.getService(SecurityService.class).getSessionUser();
        setEventUser(sessionUser);

    }



    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(Long domainId) {
        this.domainId = domainId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return getEventType() + "|" + getTitle();
    }

    public String getEventType() {
        return  "Payment";
    }

    @Override
    public String getTarget() {
        return super.getTarget();
    }


}
