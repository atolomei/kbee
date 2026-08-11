package com.novamens.kbee.email;

import javax.mail.Session;


public interface MailStoreFactory {
    Session getEmailSession();
}
