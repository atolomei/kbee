package kbee.web.nav;

import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.HREFBCElement;

public class ReportsSectionBC extends HREFBCElement {
			
	private static final long serialVersionUID = 1L;
	
	public ReportsSectionBC() {
		super("link", "/reports", new Model<String>("reports"));
		super.label = new StringResourceModel("bc.reports", this, null);
		
	}
	
	//@Override
	//public void onClick() {
	//	setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("reports-home-age"));
	//}
}
