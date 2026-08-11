package com.novamens.kbee.email;

import com.novamens.beans.BeansService;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.IMAPFolder;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;

public class ProcessPendingContentMailRequest extends AbstractCronJobRequest {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ProcessPendingContentMailRequest.class.getName());

    //String mailStoreFactoryBean = "mailStoreFactory";
    //final String OUT_FOLDER = "KBEE_OUT";
    //final String IN_FOLDER = "KBEE_IN";


    public ProcessPendingContentMailRequest() {
        setName(this.getClass().getName());
        setDescription("Process pending");
    }

    @Override
    public void execute() {
        final String inFolder = getInFolder();
        final String mailStoreFactoryBean = getMailStoreFactoryBean();
        final String outFolder = getOutFolder();


        MailStoreFactory mailStoreFactory = ((MailStoreFactory) ServiceLocator.getService(BeansService.class).getBean(mailStoreFactoryBean));
        final TransactionService transactionService = ServiceLocator.getService(TransactionService.class);

        EmailProcessor emailFetcher = new EmailProcessor() {
            @Override
            public void process(Message message, Store store) {
                IMAPFolder folderOUT=null;
                try {

                    folderOUT = (IMAPFolder) store.getFolder(outFolder);
                    folderOUT.open(Folder.READ_WRITE);
                    IMAPFolder folderIN = (IMAPFolder) message.getFolder();

                    ProcessContentEmailRequest contentEmailRequest = new ProcessContentEmailRequest();
                       contentEmailRequest.setStoreFactoryBean(mailStoreFactoryBean);
                    contentEmailRequest.setFolder(outFolder);

                    SchedulerService service = ServiceLocator.getService(SchedulerService.class);
                    Transaction transaction = null;
                    try {
                        transaction = transactionService.beginTransaction(false);
                        final AppendUID[] appendUIDS = folderIN.moveUIDMessages(new Message[]{message}, folderOUT);
                        contentEmailRequest.setMailUID(appendUIDS[0].uid);
                        service.enqueue(contentEmailRequest);
                        transaction.commit();
                    } catch (Exception e) {
                        if (transaction != null)
                            transaction.rollback();
                    }
                } catch (MessagingException e) {
                    logger.error(e);
                }finally {
                    if(folderOUT!=null){
                        try {
                            folderOUT.close();
                        } catch (MessagingException e) {
                            logger.error(e);
                        }
                    }
                }
            }
        };


        emailFetcher.process(mailStoreFactory, inFolder);

    }

    public String getMailStoreFactoryBean() {
        if (getParameters().containsKey("mailStoreFactoryBean".toLowerCase()))
            return getParameters().get("mailStoreFactoryBean".toLowerCase());
        return "";
    }

    public void setMailStoreFactoryBean(String mailStoreFactoryBean) {
        getParameters().put("mailStoreFactoryBean".toLowerCase(), mailStoreFactoryBean);
    }

    public String getInFolder() {
        if (getParameters().containsKey("inFolder".toLowerCase()))
            return getParameters().get("inFolder".toLowerCase());
        return "";
    }

    public void setInFolder(String inFolder) {
        getParameters().put("inFolder".toLowerCase(), inFolder);
    }

    public String getOutFolder() {
        if (getParameters().containsKey("outFolder".toLowerCase()))
            return getParameters().get("outFolder".toLowerCase());
        return "";
    }

    public void setOutFolder(String inFolder) {
        getParameters().put("outFolder".toLowerCase(), inFolder);
    }
}
