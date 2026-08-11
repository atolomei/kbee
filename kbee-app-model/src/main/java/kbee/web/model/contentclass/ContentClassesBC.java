package kbee.web.model.contentclass;

import com.novamens.wicket.util.BCElement;

public class ContentClassesBC extends BCElement {
	private static final long serialVersionUID = 1L;
	
	public ContentClassesBC() {
		super("bc.templates");
	}
	
	@Override
	public void onClick() {
		setResponsePage(new ContentTemplatesPage());
	}
}
