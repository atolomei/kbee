package com.novamens.kbee.email;

import com.novamens.util.KbeeRuntimeException;
import com.sun.mail.imap.IMAPFolder;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class EmailParser {

    public Email getEmail(MimeMessage p,boolean skipBody, boolean skipAttachments) throws MessagingException, IOException {
        final Email email = new Email();

        final Folder folder = p.getFolder();
        if(folder instanceof IMAPFolder)
            email.setUID(((IMAPFolder) folder).getUID(p));

        email.setMessageID(p.getMessageID());

        final LocalDateTime receivedDate = p.getReceivedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        email.setReceivedDate(receivedDate);

        Address[] a;
        if ((a = p.getFrom()) != null) {
            if (a.length > 0) {
                InternetAddress address = (InternetAddress) a[0];
                email.setFrom(address.getAddress());
            }

        }
        if ((a = p.getRecipients(Message.RecipientType.TO)) != null) {
            for (int j = 0; j < a.length; j++) {
                final InternetAddress address = (InternetAddress) a[j];
                email.getTo().add(address.getAddress());
            }
        }
        if ((a = p.getRecipients(Message.RecipientType.CC)) != null) {
            for (int j = 0; j < a.length; j++) {
                final InternetAddress address = (InternetAddress) a[j];
                email.getCc().add(address.getAddress());
            }
        }
        if ((a = p.getRecipients(Message.RecipientType.BCC)) != null) {
            for (int j = 0; j < a.length; j++) {
                final InternetAddress address = (InternetAddress) a[j];
                email.getBcc().add(address.getAddress());
            }
        }

        email.setSubject(p.getSubject());

        if(!skipBody)
            email.setBody(getText(p));
        if(!skipAttachments)
            fillAttachments(p, 0, email);
        return email;
    }

    private String getText(Part p) throws
            MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            String s = (String) p.getContent();
            //textIsHtml = p.isMimeType("text/html");
            return s;
        }
        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                    if (text != null)
                        return text;
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    continue;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }
        return null;
    }

    private void fillAttachments(Part p, int level, Email email) throws MessagingException, IOException {
        if (p.isMimeType("text/plain")) {
            //Not an attachment
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            int count = mp.getCount();
            int start = level == 0 ? 1 : 0;
            for (; start < count; start++)
                fillAttachments(mp.getBodyPart(start), level + 1, email);
        } else if (p.isMimeType("message/rfc822")) {
            fillAttachments((Part) p.getContent(), level + 1, email);
        }
        if (level != 0 && p instanceof MimeBodyPart &&
                !p.isMimeType("multipart/*")) {
            String disp = p.getDisposition();
            if (disp == null || disp.equalsIgnoreCase(Part.ATTACHMENT)) {
                String filename = p.getFileName();
                String contentType = p.getContentType();

                if (filename == null)
                    filename = "Attachment " + email.getAttachments().size() + 1;
                try {
                    Path temp = Files.createTempFile(filename, ".attachment");
                    ;
                    final File tempFile = temp.toFile();
                    final MimeBodyPart mimeBodyPart = (MimeBodyPart) p;
                    mimeBodyPart.saveFile(tempFile);

                    final byte[] fileContent = Files.readAllBytes(tempFile.toPath());

                    final Email.Attachment attachment = new Email.Attachment(filename, contentType, fileContent);
                    email.getAttachments().add(attachment);
                } catch (IOException ex) {
                    throw new KbeeRuntimeException("Cannot get email attachment", ex);
                }
            }
        }
    }

}
