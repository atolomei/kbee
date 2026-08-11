package com.novamens.kbee.content.repository;

import org.springframework.stereotype.Component;

import com.novamens.content.notification.Notification;
import com.novamens.kbee.content.notification.KbeeNotification;
import com.novamens.kbee.repository.AbstractDomRepository;


@Component
public class NotificationRepository extends AbstractDomRepository<KbeeNotification, Notification> {

}