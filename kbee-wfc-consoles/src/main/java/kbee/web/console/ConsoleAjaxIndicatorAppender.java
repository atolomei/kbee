package kbee.web.console;

import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;

public class ConsoleAjaxIndicatorAppender extends AjaxIndicatorAppender {
	private static final long serialVersionUID = 8248334309480564785L;
	
	private boolean show = false;
 	public boolean getShow() { return show;}
 	public void setShow(boolean s) {show=s;}
}
