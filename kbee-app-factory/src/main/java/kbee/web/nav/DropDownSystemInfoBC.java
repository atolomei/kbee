package kbee.web.nav;

import org.apache.wicket.model.StringResourceModel;

import com.novamens.wicket.util.HREFBCElement;

public class DropDownSystemInfoBC extends DropDownMenuBC<Void> {
	private static final long serialVersionUID = 1L;
	public DropDownSystemInfoBC() {
		addElement(new SystemInfoBC(), true);
		addElement(new SystemInfoBC());
		addElement(new HREFBCElement("link", "/domains/recyclebin", new StringResourceModel("bc.domain-recycle-bin", this, null)));
		
	}
}
