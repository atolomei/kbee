package kbee.payment.paymentAction;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.service.ServiceLocator;
import kbee.payment.PaymentActionProcessor;
import kbee.payment.exception.ActionNotApplicableException;
import kbee.payment.exception.ValidationException;

public class PremiumDomainPurchaseActionProcessor implements PaymentActionProcessor {

    private Long domainId;
    private Integer newDomainTypeId;

    public PremiumDomainPurchaseActionProcessor() {
    }

    public PremiumDomainPurchaseActionProcessor(Long domainId, Integer newDomainType) {
        this.domainId = domainId;
        this.newDomainTypeId = newDomainType;
    }

    @Override
    public void perform() {
        final Domain domain = getContentDao().findDomainById(domainId);
        domain.setDomainType(DomainType.fromId(newDomainTypeId));
        getContentDao().save(domain);
    }

    @Override
    public void checkIsApplicable() throws ActionNotApplicableException {

        final Domain domain = getContentDao().findDomainById(domainId);
        if(domain!=null){
            final DomainType newDomainType = DomainType.fromId(newDomainTypeId);

            if(domain.getDomainType() == DomainType.SYSTEM){
                throw new ActionNotApplicableException(String.format("System domains cannot be upgraded."));
            }
            if(domain.getDomainType() == newDomainType){
                throw new ActionNotApplicableException(String.format("Domain is type '%s' already.", newDomainType.getDisplayName() ));
            }

        }
    }

    @Override
    public void validate() throws ValidationException {
        if(domainId == null || domainId <= 0){
            throw new ValidationException("Invalid user.");
        }
        if(newDomainTypeId == null || newDomainTypeId <= 0){
            throw new ValidationException("Invalid number licence type.");
        }
    }

    protected ContentDao getContentDao() {
        return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
    }


    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(Long domainId) {
        this.domainId = domainId;
    }


    public Integer getNewDomainTypeId() {
        return newDomainTypeId;
    }

    public void setNewDomainTypeId(Integer newDomainTypeId) {
        this.newDomainTypeId = newDomainTypeId;
    }

    @Override
    public String getPaymentKey() {
        return null;
    }
}
