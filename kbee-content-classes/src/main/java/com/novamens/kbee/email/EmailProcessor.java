package com.novamens.kbee.email;

import com.novamens.util.KbeeRuntimeException;
import com.sun.mail.imap.IMAPFolder;

import java.time.LocalDateTime;

import javax.mail.*;

public abstract class EmailProcessor {
    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailProcessor.class.getName());


    public abstract void process(Message message,Store store);

    public void process(MailStoreFactory mailStoreFactory, String folder) {
        try {
            final Store store = mailStoreFactory.getEmailSession().getStore();
            store.connect();
            // create the folder object and open it
            IMAPFolder emailFolder = (IMAPFolder) store.getFolder(folder);
            emailFolder.open(Folder.READ_WRITE);

            Message[] messages = emailFolder.getMessages();

            for (Message message : messages) {
                process(message,store);
            }

            emailFolder.close(false);
            store.close();

        } catch (Exception e) {
           throw new KbeeRuntimeException("Exception while fetching pending emails.", e);
        }
    }

}
