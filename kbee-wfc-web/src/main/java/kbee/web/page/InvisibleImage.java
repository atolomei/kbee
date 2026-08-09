package kbee.web.page;

import org.apache.wicket.markup.html.image.Image;

public class InvisibleImage extends Image {

	private static final long serialVersionUID = -2736730637711570566L;

	public InvisibleImage(String id) {
		super(id);
		
	}
	
	public boolean isVisible() {
		return false;
	}

}
