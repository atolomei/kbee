package kbee.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novamens.dom.Domain;
import com.novamens.kbee.domain.KbeeDomain;
import com.novamens.kbee.kbfs.encryption.interfaces.StreamEncryptorInfo;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Currency;

@Entity
//@DiscriminatorColumn(name = "payment_platform", discriminatorType=DiscriminatorType.INTEGER)
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "payment")
public class KbeePayment implements Payment {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeDomain.class)
    @Fetch(FetchMode.SELECT)
    @JoinColumn(name = "domain_id", updatable=false)
    private Domain domain;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
    @Fetch(FetchMode.SELECT)
    @JoinColumn(name ="payer", nullable=true)
    private User payer;

    @Column(name = "redirect_url")
    private String redirectUrl;

    @Convert(converter = PaymentPlatformConverter.class)
    @Column(name = "payment_platform")
    private PaymentPlatform paymentPlatform;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "currency_code")
    private String currency;

    @Column(name = "concept_detail")
    private String conceptDetail;

    @Column(name = "trx_reference")
    private String trxReference;

    @Convert(converter = PaymentStatusConverter.class)
    @Column(name = "status")
    private PaymentStatus status;

    @Column(name = "approval_date")
    private OffsetDateTime approvalDate;

    @Column(name = "process_date")
    private OffsetDateTime processDate;

    @Column(name = "create_date")
    private OffsetDateTime createDate;

    @Column(name = "last_check")
    private OffsetDateTime lastCheck;

    @Column(name = "payment_key")
    private String key;

    @Column(name = "payment_action")
    @Access(value = AccessType.FIELD)
    private String paymentAction;

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return "Payment flow " + this.getId();
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }


    @Override
    public User getPayer() {
        return payer;
    }

    @Override
    public void setPayer(User payer) {
        this.payer = payer;
    }

    @Override
    public PaymentPlatform getPaymentPlatform() {
        return paymentPlatform;
    }

    @Override
    public void setPaymentPlatform(PaymentPlatform paymentPlatform) {
        this.paymentPlatform = paymentPlatform;
    }


    @Override
    public String getRedirectUrl() {
        return redirectUrl;
    }

    @Override
    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }


    @Override
    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public void setCurrency(Currency currency) {
        this.currency = currency.getCurrencyCode();
    }

    @Override
    public Currency getCurrency() {
        return Currency.getInstance(currency);
    }

    @Override
    public String getConceptDetail() {
        return conceptDetail;
    }

    @Override
    public void setConceptDetail(String conceptDetail) {
        this.conceptDetail = conceptDetail;
    }

    @Override
    public String getTrxReference() {
        return trxReference;
    }

    @Override
    public void setTrxReference(String trxReference) {
        this.trxReference = trxReference;
    }

    @Override
    public PaymentStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    @Override
    public OffsetDateTime getApprovalDate() {
        return approvalDate;
    }

    @Override
    public void setApprovalDate(OffsetDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }

    @Override
    public OffsetDateTime getProcessDate() {
        return processDate;
    }

    @Override
    public void setProcessDate(OffsetDateTime processDate) {
        this.processDate = processDate;
    }

    @Override
    public OffsetDateTime getCreateDate() {
        return createDate;
    }

    @Override
    public void setCreateDate(OffsetDateTime createDate) {
        this.createDate = createDate;
    }

    @Override
    public OffsetDateTime getLastCheck() {
        return lastCheck;
    }

    @Override
    public void setLastCheck(OffsetDateTime lastCheck) {
        this.lastCheck = lastCheck;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

	static private ObjectMapper mapper = new ObjectMapper();
	
    @Override
    public PaymentActionProcessor getPaymentAction() {
        try {
            return mapper.readValue(this.paymentAction, PaymentActionProcessor.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not deserialize PaymentActionProcessor.",e );
        }
    }

    @Override
    public void setPaymentAction(PaymentActionProcessor paymentAction) {
        try {
            this.paymentAction = mapper.writeValueAsString(paymentAction);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not serialize PaymentActionProcessor.", e);
        }
    }
}