package kbee.web.nav;

import org.apache.wicket.model.StringResourceModel;

import com.novamens.wicket.util.HREFBCElement;

public class DataSetMembersSectionBC extends HREFBCElement {

	private static final long serialVersionUID = 1L;

	public DataSetMembersSectionBC() {
		super("/datasetmembers");
		super.label = new StringResourceModel("bc.dataset.members.home", this, null); 
	}
	

}
