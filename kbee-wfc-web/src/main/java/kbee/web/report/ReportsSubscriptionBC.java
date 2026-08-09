package kbee.web.report;

import org.apache.wicket.model.StringResourceModel;

import com.novamens.wicket.util.HREFBCElement;
import com.novamens.wicket.util.IBCElement;

public class ReportsSubscriptionBC extends HREFBCElement implements IBCElement {
			
	private static final long serialVersionUID = 1L;

	public ReportsSubscriptionBC() {
		super("/reports/subscriptions");
		super.label = new StringResourceModel("bc.reportsubscription", this, null);
	}

}
