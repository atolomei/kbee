package kbee.web.nav;

import org.apache.wicket.model.StringResourceModel;

import com.novamens.wicket.util.HREFBCElement;

public class DropDownDomainsBC extends DropDownMenuBC<Void> {

	private static final long serialVersionUID = 1L;
	
	public DropDownDomainsBC() {
		addElement(new DomainsBC(), true);
		addElement(new DomainsBC());
		addElement(new HREFBCElement("link", "/factory/domainrecyclebin", new StringResourceModel("bc.domain-recycle-bin", this, null)));
		addElement(new CommandsBC());
		
	}
}
