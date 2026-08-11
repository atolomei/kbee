package kbee.payment.paymentAction;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.service.ServiceLocator;
import kbee.payment.PaymentActionProcessor;
import kbee.payment.exception.ActionNotApplicableException;
import kbee.payment.exception.ValidationException;

public class DummyActionProcessor implements PaymentActionProcessor {


    public DummyActionProcessor() {
    }

    public DummyActionProcessor(Long domainId, Integer newDomainType) {

    }

    @Override
    public void perform() {
    }

    @Override
    public void checkIsApplicable() throws ActionNotApplicableException {

    }

    @Override
    public void validate() throws ValidationException {
    }

    @Override
    public String getPaymentKey() {
        return null;
    }
}
