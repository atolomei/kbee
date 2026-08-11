package com.novamens.kbee.content.reportsubscription;


import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;


public class SubscriptionReportExportOneTimeRequest extends AbstractServiceRequest {

    private static final long serialVersionUID = 1L;
    static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SubscriptionReportExportOneTimeRequest.class.getName());
    String reportExportScheduleId;

    public SubscriptionReportExportOneTimeRequest() {
        this.setParameters(new HashMap<>());
        setName("SubscriptionReportExport");
        setDescription("Report Subscription");
    }

    @Override
    public void execute() {
        SubscriptionReportExportCommand cmd = new SubscriptionReportExportCommand(this.getReportExportScheduleId());
        cmd.execute();

    }

    public String getReportExportScheduleId() {
        if (getParameters().containsKey("reportexportscheduleid"))
            return getParameters().get("reportexportscheduleid");
        return reportExportScheduleId;
    }

    public void setReportExportScheduleId(String reportExportScheduleId) {
        this.reportExportScheduleId = reportExportScheduleId;
        getParameters().put("reportexportscheduleid", reportExportScheduleId);
        setDescription("Report Subscription -> " + reportExportScheduleId != null ? reportExportScheduleId : "");
    }
}
