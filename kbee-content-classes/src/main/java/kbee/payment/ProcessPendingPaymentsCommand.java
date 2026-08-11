package kbee.payment;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

public class ProcessPendingPaymentsCommand extends AsyncCommand {

    static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger( ProcessPendingPaymentsCommand.class.getName());

    public ProcessPendingPaymentsCommand() {
        setName("Process pending payments");
    }

    public void executeAsync() {

        Transaction transaction = null;
        try {
            logger.debug("Starting to process pending payments.");
            final MercadoPagoPaymentService paymentService = ServiceLocator.getService(MercadoPagoPaymentService.class);
            com.novamens.hibernate.session.Session.open();
            transaction = beginTransaction();


            final List<kbee.payment.Payment> paymentsPending = getContentDao().findPaymentsPending(OffsetDateTime.now().minus(Duration.ofDays(30)), 500);
            for (kbee.payment.Payment payment : paymentsPending) {
                try {
                    paymentService.tryProcessPayment(payment);
                } catch (Exception e) {
                    logger.error(e);
                }
            }
            transaction.commit();
        }
        catch (Exception e) {
            logger.error(e);
            stop();
            if(transaction != null){
                transaction.rollback();
            }
        }
        finally {
            com.novamens.hibernate.session.Session.close();
        }
    }

    private ContentDao getContentDao() {
        BeansService beans = ServiceLocator.getService(BeansService.class);
        return  (ContentDao) beans.getBean("contentDao");
    }

    protected Transaction beginTransaction()  {
        return ServiceLocator.getService(TransactionService.class).beginTransaction(false);
    }

    @Override
    public String getConcurrentUniqueKey() {
        return null; /*this.getClass().toString();*/
    }
}
