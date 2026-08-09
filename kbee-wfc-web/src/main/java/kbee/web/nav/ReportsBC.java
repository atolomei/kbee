package kbee.web.nav;


import com.novamens.kbee.content.reportsubscription.ReportExportSchedule;
import com.novamens.security.User;
import com.novamens.service.LanguageService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.report.ReportFactory;
import kbee.web.service.ApplicationSiteMapService;
import kbee.web.service.ReportsLibraryService;

import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReportsBC extends DropDownMenuBC {

    public ReportsBC() {

        this.addElement(new BCElement(new StringResourceModel("bc.reports", this, null)));
        for (String group : getReportsGroups()) {
        	
        	String dname = ServiceLocator.getService(LanguageService.class).getString(group, getSessionUser().getLocale());
        	
             this.addElement(new BCElement(()->dname) {
                public void onClick() {
                    PageParameters pageParameters = new PageParameters();
                    pageParameters.set("reportGroup", group);
                    //kbee.web.report.markup.ReportPage rp = new ReportPage(pageParameters);
                    //setResponsePage(rp);
                    setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("settings-dataset-members-page", pageParameters) ); 
                    
                    
                }
            },false);
        }

        if(getUserSessionReportSchedules().size()>0) {

            this.addElement(new BCElement(new StringResourceModel("reportsubscriptions", this, null)) {
                @Override
                public void onClick() {
                    // setResponsePage(new ReportSubscriptionPage());
                	setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("report-subscription-page"));
                }
            });
        }
    }
    private List<ReportExportSchedule> getUserSessionReportSchedules() {
        return ServiceLocator.getService(ReportsLibraryService.class).getUserDomainReportExportSchedules();
    }

    private List<String> getReportsGroups() {
        List<ReportFactory> list = ServiceLocator.getService(ReportsLibraryService.class).getUserSessionReports();
        Set<String> groups = new HashSet<>();
        list.forEach(rep -> groups.add(rep.getReport().getReportGroup()));
        return new ArrayList<>(groups);
    }

    protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
