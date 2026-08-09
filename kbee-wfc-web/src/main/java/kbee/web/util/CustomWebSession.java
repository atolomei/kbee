package kbee.web.util;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;

import kbee.web.page.CustomBrowserInfoPage;

public class CustomWebSession extends WebSession {
	  /**
	 * 
	 */
	private static final long serialVersionUID = -6691757386512797665L;

	public CustomWebSession(Request request) {
		super(request);
	 
	}

	@Override 
	    protected WebPage newBrowserInfoPage() { 
	        return new CustomBrowserInfoPage(); 
	    } 
}
