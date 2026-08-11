package kbee.web.model;


import com.novamens.wicket.util.BCElement;

public class AttributesBC extends BCElement {
	private static final long serialVersionUID = 1L;
	
	public AttributesBC() {
		super("bc.attributes");
	}
	
	@Override
	public void onClick() {
		setResponsePage(new kbee.web.model.AttributesPage());
	}
}