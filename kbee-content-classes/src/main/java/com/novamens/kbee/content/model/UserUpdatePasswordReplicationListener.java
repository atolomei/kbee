package com.novamens.kbee.content.model;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.event.AppUpdateEvent;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.hibernate.event.HibernateUpdateEvent;
import com.novamens.indexer.java.BatchIndexTaskServiceRequest;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.command.PasswordReplicationServiceRequest;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;
import com.novamens.util.KbeeRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class UserUpdatePasswordReplicationListener implements EventListener {

    static private Logger logger = LogManager.getLogger(UserUpdatePasswordReplicationListener.class.getName());

    public boolean listen(Event event) {
        return ((event instanceof AppUpdateEvent) && event.getObject() instanceof User);
    }

    public void onEvent(Event event) {
        updateWorkspace(event);
    }

    protected void updateWorkspace(Event event) {
        if (!(event instanceof HibernateUpdateEvent))
            return;

        if (event.getObject() instanceof User) {
            KbeeUser user = (KbeeUser) event.getObject();
            try {
                ServiceLocator.getService(SchedulerService.class).enqueue(new PasswordReplicationServiceRequest(user));
            }catch(SchedulerException e){
                    logger.error(e);
                    throw new KbeeRuntimeException(e);
                }
            }
        }

    }
