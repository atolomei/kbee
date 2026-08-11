package com.novamens.content.web.admin.markup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.LanguageService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.web.report.SystemReportConsole;

public class SystemEmailReportConsole extends SystemReportConsole {
			
	private static final long serialVersionUID = 1L;
	
	private List<GridColumn<SearchResult,String>> columns = null;
	 
	public SystemEmailReportConsole() {
		this("SystemEmailReportConsole");
	}
	
	public SystemEmailReportConsole(String id) {
		super(id, null, id);
		setReportGroup(SYSTEMAUDIT);
	}
	
	
	@Override
	protected boolean isFiltersEnabled() {
		return false;
	}

	public List<GridColumn<SearchResult, String>> getColumns() {
		if (this.columns!=null)
			return this.columns;
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();
		this.columns.add(new InnerStringColumn("action",	new Model<String>("Action"), null));
		this.columns.add(new InnerIntegerColumn("total", 	new Model<String>("Total"),  null, "number-md", true));
		return this.columns;
	}
	
	public IModel<String> getDisplayName() {															
		return new Model<String>(ServiceLocator.getService(LanguageService.class).getString( getKey(), getSessionUser().getLocale()));
	}
	
	
 	@Override
	public void onDetach() {
		super.onDetach();
		this.columns=null;
	}


 	@Override
 	public Query newQuery() {
		return new AuditEmailReportQuery();
	}
	
	@Override
	public boolean isReadable() {
		return 
		 (	ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()) ||
		    ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId())||
			ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId()) );
	}

	
	@Override
	public String getDownloadFileName() {
		return "audit-email-" + (new SimpleDateFormat("YYYY-MM-dd")).format(new Date());
	}

}
