package com.novamens.logging;

import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class CronJobUpdateEvent extends AbstractLogEvent {

    @Column(name = "EVENT_OBJECT_ID")
    private String objectId;


    public CronJobUpdateEvent(AbstractCronJobRequest cronJob) {
        setAuditSet(AuditSet.GENERAL);
        setObjectId(cronJob.getId().toString());
        setParameters("{id:" + cronJob.getId() + ", expression: \"" + cronJob.getCronExpression() + "\"" + ", enabled: \"" + cronJob.isEnabled() + "\"" + ", description: \"" + cronJob.getDescription() + "\" }");
        setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
    }

    public CronJobUpdateEvent() {

    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
}
