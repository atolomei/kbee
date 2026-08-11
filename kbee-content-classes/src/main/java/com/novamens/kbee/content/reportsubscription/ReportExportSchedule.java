package com.novamens.kbee.content.reportsubscription;

import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.CronSchedulerService;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportExportSchedule implements Serializable {


	private static final long serialVersionUID = 1L;
	
	private String id;
    private String report;
    private String description;
    private Integer hoursRangeSamePeriod;

    private List<String> domainNames = null;

    private String exporterClass;
    private String reportClass;
    private Map<String, Object> reportParameters;

    public ReportExportSchedule() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExporterClass(String exporterClass) {
        this.exporterClass = exporterClass;
    }

    public void setReportClass(String reportClass) {
        this.reportClass = reportClass;
    }

    public String getExporterClass() {
        return exporterClass;
    }

    public String getReportClass() {
        return reportClass;
    }

    public Map<String, Object> getReportParameters() {
        return reportParameters;
    }

    public void setReportParameters(Map<String, Object> reportParameters) {
        this.reportParameters = reportParameters;
    }

    public Integer getHoursRangeSamePeriod() {
        return hoursRangeSamePeriod;
    }

    public void setHoursRangeSamePeriod(Integer hoursRangeSamePeriod) {
        this.hoursRangeSamePeriod = hoursRangeSamePeriod;
    }

    public List<String> getDomainNames() {
        return domainNames;
    }

    public void setDomainNames(List<String> domainNames) {
        this.domainNames = domainNames;
    }

    public List<SubscriptionReportExportRequest> getCronSchedules(){
        CronSchedulerService service = ServiceLocator.getService(CronSchedulerService.class);
        List<AbstractCronJobRequest> cronJobs = service.getCronJobs();
        List<SubscriptionReportExportRequest> mySchedules = cronJobs.stream().filter(cron -> (cron instanceof SubscriptionReportExportRequest))
                                                                                .map(cron -> (SubscriptionReportExportRequest) cron)
                                                                                .filter(cron -> cron.getReportExportScheduleId() != null && cron.getReportExportScheduleId().equals(this.getId()))
                                                                                .collect(Collectors.toList());

        return mySchedules;
    }
}
