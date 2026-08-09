package kbee.web.nav;

import org.apache.wicket.model.StringResourceModel;

import com.novamens.wicket.util.HREFBCElement;

public class InformationModelSectionBC extends HREFBCElement  {
	private static final long serialVersionUID = 1L;
	public InformationModelSectionBC() {
		super("/model");
		super.label = new StringResourceModel("information-model-home", this, null);
	}
}
