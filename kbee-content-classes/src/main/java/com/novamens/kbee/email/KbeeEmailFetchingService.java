package com.novamens.kbee.email;

import com.novamens.beans.BeansService;
import com.novamens.email.EmailFetchingService;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;
import com.sun.mail.imap.IMAPFolder;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import java.time.OffsetDateTime;

@Deprecated
public class KbeeEmailFetchingService implements EmailFetchingService, Runnable {

    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeEmailFetchingService.class.getName());
    private Thread thread;
    private OffsetDateTime started = OffsetDateTime.now();
    private boolean isRunning;


    @Override
    public void start() {
        logger.info("Starting Email fetching service");
        this.thread = new Thread(this);
        this.thread.setDaemon(true);
        this.thread.setName("Email fetching service");
        this.thread.start();
    }

    @Override
    public void run() {
        try {
            isRunning = true;
            final String mailStoreFactoryBean = "mailStoreFactory";
            MailStoreFactory mailStoreFactory = ((MailStoreFactory) ServiceLocator.getService(BeansService.class).getBean(mailStoreFactoryBean));
            final TransactionService transactionService = ServiceLocator.getService(TransactionService.class);
            final String OUT_FOLDER = "KBEE_OUT";
            final String IN_FOLDER = "KBEE_IN";
            EmailProcessor emailFetcher = new EmailProcessor() {
                @Override
                public void process(Message message, Store store) {
                    try {

                        final IMAPFolder folderOUT = (IMAPFolder) store.getFolder(OUT_FOLDER);
                        folderOUT.open(Folder.READ_WRITE);
                        IMAPFolder folderIN = (IMAPFolder) message.getFolder();

                        ProcessContentEmailRequest contentEmailRequest = new ProcessContentEmailRequest();
                        contentEmailRequest.setMailUID(folderIN.getUID(message));
                        contentEmailRequest.setStoreFactoryBean(mailStoreFactoryBean);
                        contentEmailRequest.setFolder(OUT_FOLDER);

                        SchedulerService service = ServiceLocator.getService(SchedulerService.class);
                        Transaction transaction = null;
                        try {
                            transaction = transactionService.beginTransaction(false);
                            service.enqueue(contentEmailRequest);
                            folderIN.moveMessages(new Message[]{message}, folderOUT);
                            transaction.commit();
                        } catch (Exception e) {
                            if (transaction != null)
                                transaction.rollback();
                        }
                    } catch (MessagingException e) {
                        logger.error(e);
                    }
                }
            };

            while (true) {
                emailFetcher.process(mailStoreFactory, IN_FOLDER);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                	logger.error(e);
                }
            }

        } finally {
            isRunning = false;
        }

    }

    @Override
    public OffsetDateTime getStartDateTime() {
        return this.started;
    }

    @Override
    public boolean isStarted() {
        return this.isRunning;
    }

}
