package com.novamens.content.web.admin.markup;

import com.novamens.content.user.UserService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleSidePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.system.parameters.SystemParameterService;

import kbee.web.report.DateRangeReportSidePanel;
import kbee.web.report.ReportBaseParameterPanel;
import kbee.web.report.SystemReportConsole;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class BillingReportConsole extends SystemReportConsole {

    private static final long serialVersionUID = 1L;

    private List<GridColumn<SearchResult, String>> columns = null;

    public BillingReportConsole() {
        this("BillingReport");
    }

    public BillingReportConsole(String id) {
        super(id, null, id);
        //setConsole(id);
        setReportGroup(SYSTEMAUDIT);
    }

    @Override
    protected boolean isFiltersEnabled() {
        return false;
    }

    public IModel<String> getReportDescription() {
		
    	String paramName = "report." + (this.getName()!=null?this.getName().toLowerCase().trim(): this.getClass().getSimpleName().toLowerCase()) +".description";
		String s=ServiceLocator.getService(SystemParameterService.class).getParameter(paramName, null);
		
		if (s!=null)
			return new Model<String>(s);
		
		return new StringResourceModel("report.description", this, null);
	}
    
    
    public List<GridColumn<SearchResult, String>> getColumns() {

        if (this.columns != null)
            return this.columns;

        this.columns = new ArrayList<GridColumn<SearchResult, String>>();

        this.columns.add(new InnerStringColumn("domain", getLabel("column.domain"), "domain"));
        
        this.columns.add(new InnerStringColumn("organization", getLabel("organization"), "organization"));
        this.columns.add(new InnerStringColumn("type", getLabel("domaintype"), "type"));
        
        this.columns.add(new InnerStringColumn("date", getLabel("column.date"), "date"));
        
        this.columns.add(new InnerIntegerColumn("billable_sites", getLabel("column.billable_sites"), null, "number-xl", false));
        this.columns.add(new InnerIntegerColumn("billable_users", getLabel("column.billable_users"), null, "number-xl", false));
        
        this.columns.add(new InnerIntegerColumn("users", getLabel("column.users"), null, "number-xl", false));
        this.columns.add(new InnerIntegerColumn("contents", getLabel("column.contents"), null, "number-xl", false));
        this.columns.add(new InnerFloatColumn("hd_total",	getLabel("column.hd_total"),  null, "number-mdx", false));

        //	
        // this.columns.add(new InnerFloatColumn("db_total",	    new Model<String>("Database (GB)"),  null, "number-mdx", false));
        //

        return this.columns;
    }
    
    
    // billable_sites = sites that have 1 content or more
    // users = 
    // billable users = 

    @Override
    protected ConsoleSidePanel getRightPanel() {
        DateRangeReportSidePanel pa = (DateRangeReportSidePanel) super.getRightPanel();
        pa.setShowFromDatePicker(false);
        return pa;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        this.columns = null;
    }

    @Override
    public Query newQuery() {
        return new BillingReportQuery();
    }


    @Override
    public boolean isReadable() {
        SecurityService service = ServiceLocator.getService(SecurityService.class);

        return service.isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId()) && ServiceLocator.getService(UserService.class).getDomain().getName().equals("kbee");
    }

    @Override
    public String getDownloadFileName() {
        DateFormat dateparameterformat = new SimpleDateFormat("YYYY-MM-dd");
        return "billing-" + dateparameterformat.format(new Date());
    }

    public String getTitle() {
        return getLabel("report.name").getObject();
    }
    @Override
    public IModel<String> getDisplayName() {
        return getLabel("report.name");
    }


}
