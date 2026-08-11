package com.novamens.content.reportsubscription;

import com.novamens.dom.DomainObject;
import com.novamens.dom.Indexable;
import com.novamens.security.Auditable;
import com.novamens.security.Identifiable;
import com.novamens.security.User;

import java.time.OffsetDateTime;


public interface ReportSubscription extends Auditable, Identifiable, DomainObject {

//public interface ReportSubscription extends Auditable, Indexable, Identifiable, DomainObject {

    public String getReportExportScheduleId();
    public void setReportExportScheduleId(String reportExportScheduleId);
    public boolean isEnabled();
    public void setEnabled(boolean enabled);
    public User getUsr();
    public void setUsr(User usr);
    public OffsetDateTime getLastExportSent();
    public void setLastExportSent(OffsetDateTime lastExportSent);
}
