package kbee.web.model;

import com.novamens.wicket.util.BCElement;

public class ResourceTagsBC extends BCElement {
	private static final long serialVersionUID = 1L;
	
	public ResourceTagsBC() {
		super("bc.resourcetags");
	}
	
	@Override
	public void onClick() {
		setResponsePage(new kbee.web.model.ResourceTagsPage());
	}
}