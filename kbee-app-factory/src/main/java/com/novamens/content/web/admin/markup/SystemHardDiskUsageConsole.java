package com.novamens.content.web.admin.markup;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wicket.model.Model;

import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.web.report.SystemReportConsole;
			
public class SystemHardDiskUsageConsole extends SystemReportConsole {
			
		private static final long serialVersionUID = 1L;
		
		private List<GridColumn<SearchResult,String>> columns = null;
		
		public SystemHardDiskUsageConsole() {
					this("SystemDiskUsageReport");
		}
		
		public SystemHardDiskUsageConsole(String id) {
			super(id, null, id);
			//setConsole(id);
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
																			
		this.columns.add(new InnerStringColumn("date", 						new Model<String>("Date"), "date"));
		
		this.columns.add(new InnerFloatColumn("hd_total", 	    			new Model<String>("Hard Disk Total (GB)"),  null, "number-mdx", false));
		this.columns.add(new InnerFloatColumn("hd_total_gateway",	    	new Model<String>("Hard Disk Gateway (GB)"),  null, "number-mdx", false));
		
		if (getDomain().getName().equals("kbee"))
			this.columns.add(new InnerFloatColumn("hd_total_db",	    	new Model<String>("Database (GB)"),  null, "number-mdx", false));
			
		this.columns.add(new InnerIntegerColumn("contents", 				new Model<String>("Contents"),  null, "number-xl", false));
		this.columns.add(new InnerIntegerColumn("resources", 				new Model<String>("Resources"),  null, "number-xl", false));
		
		this.columns.add(new InnerIntegerColumn("users", 	    			new Model<String>("Users"),  null, "number-md", false));
		
		this.columns.add(new InnerFloatColumn("File System", 	    				new Model<String>("File System"),  null, "number-xl", false));
		this.columns.add(new InnerFloatColumn("Minio", 	    				new Model<String>("Minio"),  null, "number-xl", false));
		this.columns.add(new InnerIntegerColumn("resources_external",     	new Model<String>("Resources Gateway"),  null, "number-xl", false));
		
		return this.columns;
		}
		
		@Override
		public void onDetach() {
		super.onDetach();
		this.columns=null;
		}
		
		@Override
		public Query newQuery() {
		return new  SystemHardDiskUsageQuery();
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
		DateFormat dateparameterformat = new SimpleDateFormat("YYYY-MM-dd");
		return "audit-hd-usage-" + dateparameterformat.format(new Date()); 
		}

}
