package com.novamens.kbee.content.reportsubscription;


import com.novamens.kbee.content.service.datamanagement.SQLCronJobRequest;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;


/**
 * Sample XML configuration:
 * <p>
 * {@code
 *
 *
 * <bean id="ReportExportScheduleFileStatusMonthly" class="com.novamens.kbee.content.reportsubscription.ReportExportSchedule">
 * <property name="id" value="ReportExportScheduleFileStatusMonthly" />
 *
 * <property name="report" value="File Status" />
 * <property name="description" value="This report generates real-time file status data to allow any level user immediately identify which files are required to be completed and sent to the compliance library. This subscription will send a monthly report on the 1st of each month." />
 * <property name="hoursRangeSamePeriod" value="168" />
 *
 * <property name="exporterClass" value="com.novamens.content.web.console.tools.ExcelReportExporter" />
 * <property name="reportClass" value="com.novamens.windsor.markup.WindsorFileStatusReportConsole" />
 *
 *
 * <property name="reportParameters">
 * <map>
 * <entry key="nLastMonthsPeriods" value="#{1L}" />
 * </map>
 * </property>
 * </bean>
 *
 *
 * <bean id="ReportExportScheduleFileStatusMonthlyCron" class="com.novamens.kbee.content.reportsubscription.SubscriptionReportExportRequest">
 * <property name="reportExportScheduleId" value="ReportExportScheduleFileStatusMonthly"/>
 * <property name="cronExpression" value="15 15 3 1 * *"/>
 * </bean>
 * <p>
 * }
 */
public class SubscriptionReportExportRequest extends AbstractCronJobRequest {
			
    private static final long serialVersionUID = 1L;
    static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SubscriptionReportExportRequest.class.getName());
    String reportExportScheduleId;

    public SubscriptionReportExportRequest() {
        this.setParameters(new HashMap<>());
        setName("SubscriptionReportExport");
        setDescription("Report Subscription");
    }

    @Override
    public void execute() {
        SubscriptionReportExportCommand cmd = new SubscriptionReportExportCommand(this.getReportExportScheduleId());
        long hoursSinceSchedule = ChronoUnit.HOURS.between(this.getTime(), ZonedDateTime.now());
        if (hoursSinceSchedule < cmd.getExportScheduleBean().getHoursRangeSamePeriod()) {
            try {
                final SchedulerService schedulerService = ServiceLocator.getService(SchedulerService.class);
                SubscriptionReportExportOneTimeRequest clone = new SubscriptionReportExportOneTimeRequest();
                clone.setParameters(this.getParameters());
                clone.setExecuteAfter(OffsetDateTime.now().plusDays(1));
                schedulerService.enqueue(clone);

                clone = new SubscriptionReportExportOneTimeRequest();
                clone.setParameters(this.getParameters());
                clone.setExecuteAfter(OffsetDateTime.now().plusDays(2));
                schedulerService.enqueue(clone);
            } catch (SchedulerException e) {
                logger.error(e);
            }
            cmd.execute();
        } else
            logger.error("Skipping SubscriptionReportExportCommand execution, schedule date is to old. Schedule date=" + this.getTime() + ", Hours after schedule=" + hoursSinceSchedule);

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


    @Override
    public AbstractCronJobRequest clone() {
        SubscriptionReportExportRequest cronJob = (SubscriptionReportExportRequest) super.clone();
        cronJob.setReportExportScheduleId(this.getReportExportScheduleId());
        return cronJob;
    }


}
