package com.novamens.kbee.email;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "EmailFileImport")
public class EmailFileImport {

    @Id
    @Column(name = "message_id")
    String messageId;

    @Column(name = "content_id")
    Long contentId;

    public EmailFileImport() {
    }

    public EmailFileImport(String messageId, Long contentId) {
        this.messageId = messageId;
        this.contentId = contentId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }
}
