package com.novamens.content.web.report.markup;

import com.novamens.kbee.content.reportsubscription.ReportExportSchedule;

import java.util.List;

public class SubscriptionScheduleAdminInfo {
    ReportExportSchedule reportExportSchedule;
    List<String> cronExpressions;

    public SubscriptionScheduleAdminInfo(ReportExportSchedule reportExportSchedule, List<String> cronExpressions) {
        this.reportExportSchedule = reportExportSchedule;
        this.cronExpressions = cronExpressions;
    }


    public ReportExportSchedule getReportExportSchedule() {
        return reportExportSchedule;
    }

    public void setReportExportSchedule(ReportExportSchedule reportExportSchedule) {
        this.reportExportSchedule = reportExportSchedule;
    }

    public List<String> getCronExpressions() {
        return cronExpressions;
    }

    public void setCronExpressions(List<String> cronExpressions) {
        this.cronExpressions = cronExpressions;
    }
}
