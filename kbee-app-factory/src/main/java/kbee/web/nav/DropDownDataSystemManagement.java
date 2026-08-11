package kbee.web.nav;

import org.apache.wicket.model.StringResourceModel;

import com.novamens.wicket.util.HREFBCElement;

public class DropDownDataSystemManagement extends DropDownMenuBC<Void> {
			
	private static final long serialVersionUID = 1L;
	
	public DropDownDataSystemManagement() {
		addElement(new DomainsBC(), true);
		addElement(new DomainsBC());
		addElement(new HREFBCElement("link", "/domains/recyclebin", new StringResourceModel("bc.domain-recycle-bin", this, null)));
		addElement(new CommandsBC());
		
	}
	

}
