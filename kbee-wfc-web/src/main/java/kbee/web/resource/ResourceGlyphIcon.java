package kbee.web.resource;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;

public class ResourceGlyphIcon extends WebMarkupContainer {

	private static final long serialVersionUID = 1L;

	private static String CSS = "fad fa-file";
	
	
	String css = CSS;
	
	public ResourceGlyphIcon(String id) {
		super(id);
	}
	
	public ResourceGlyphIcon(String id, String css) {
		super(id);
		this.css=css;
	}
	
	public void onInitialize() {
		super.onInitialize();
		add(new AttributeModifier("class", getCss()));
	}
	
	
	public String getCss() {
		return css;
	}

}
