package kbee.web.nav;


import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.model.DataSet;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class MembersBatchCreationBC extends BCElement {

	private static final long serialVersionUID = 1L;
	
	IModel<DataSet> model;
	
	public MembersBatchCreationBC(IModel<DataSet> dataset_model) {
		super("bc.membersbatchcreation");
		model = dataset_model;
	}
	
	@Override
	public void onClick() {
		
 		// setResponsePage(new MemberBatchCreationPageV5(model));
		
		 PageParameters pa= new PageParameters();
	     pa.add("id", model.getObject().getId().toString());
	     setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("settings-dataset-members-bulk-page", pa));
		
		
		
	}

}
