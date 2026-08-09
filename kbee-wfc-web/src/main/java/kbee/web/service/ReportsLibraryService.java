package kbee.web.service;

import java.util.List;

import com.novamens.dom.Domain;
import com.novamens.kbee.content.reportsubscription.ReportExportSchedule;
import com.novamens.service.SystemService;

import kbee.web.report.ReportFactory;
			
public interface ReportsLibraryService  extends SystemService {

	public List<ReportFactory> getReports(Domain domain);

	public boolean hasReports(Domain domain);
	public List<ReportFactory> getUserSessionReports();

	public List<ReportExportSchedule> getUserDomainReportExportSchedules();
	
}
