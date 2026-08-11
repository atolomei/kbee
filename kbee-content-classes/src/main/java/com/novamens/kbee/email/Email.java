package com.novamens.kbee.email;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Email {

    private String messageID;
    private Long UID;
    private LocalDateTime receivedDate;
    private String subject;
    private String body;
    private String from;
    private List<String> to;
    private List<String> cc;
    private List<String> bcc;
    private List<Attachment> attachments;

    public Email() {
        attachments=new ArrayList<>();
        to=new ArrayList<>();
        cc=new ArrayList<>();
        bcc=new ArrayList<>();
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public Long getUID() {
        return UID;
    }

    public void setUID(Long UID) {
        this.UID = UID;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }

    public List<String> getCc() {
        return cc;
    }

    public void setCc(List<String> cc) {
        this.cc = cc;
    }

    public List<String> getBcc() {
        return bcc;
    }

    public void setBcc(List<String> bcc) {
        this.bcc = bcc;
    }

    public LocalDateTime getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDateTime receivedDate) {
        this.receivedDate = receivedDate;
    }

    public static class Attachment{
        private String fileName;
        private String contentType;
        private byte[] fileContent;

        public Attachment(String fileName, String contentType, byte[] fileContent) {
            this.fileName = fileName;
            this.contentType = contentType;
            this.fileContent = fileContent;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public InputStream getInputStream(){
            return new ByteArrayInputStream(fileContent);
        }
    }
}
