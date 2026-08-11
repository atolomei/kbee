package kbee.web.model;

import com.novamens.wicket.util.BCElement;

public class ClassifiersBC extends BCElement {
	private static final long serialVersionUID = 1L;
	
	public ClassifiersBC() {
		super("bc.classifiers");
	}
	
	@Override
	public void onClick() {
		setResponsePage(new kbee.web.model.ClassifiersPage());
	}
}
