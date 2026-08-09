package kbee.web.nav;



import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class BulkUploadBC extends BCElement {
			

	private static final long serialVersionUID = 1L;
	
	public BulkUploadBC() {
		super("bc.bulkupload");
	}
	
	@Override
	public void onClick() {
		// 
		// setResponsePage(new TaskBatchCreatePage<Content>());
		
	     //PageParameters pa= new PageParameters();
	     //pa.add("id", dataset.getId().toString());

		setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("task-bulk-upload-page"));

	}
	

}
