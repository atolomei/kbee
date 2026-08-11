package com.novamens.kbee.content.notification;

import com.novamens.content.notification.Notification;
import com.novamens.kbee.content.service.KbeeDomService;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component @Scope("prototype")
public class KbeeNotificationDomService extends KbeeDomService<KbeeNotification, Notification> {

}