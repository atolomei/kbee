package com.novamens.kbee.email;

import com.novamens.util.KbeeRuntimeException;

import javax.mail.*;
import java.util.Map;
import java.util.Properties;

public class IMAPMailSessionFactory implements MailStoreFactory {


    String account;
    String p;
    Map<String, String> properties;

    public IMAPMailSessionFactory() {
    }

    @Override
    public Session getEmailSession() {

        Properties properties = new Properties();

        properties.putAll(this.getProperties());
        properties.setProperty("mail.store.protocol", "imaps");

        Session emailSession = Session.getInstance(properties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(getAccount(), getP());
                    }
                });

        return emailSession;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
