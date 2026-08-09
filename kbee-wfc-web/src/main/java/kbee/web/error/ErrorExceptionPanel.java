package kbee.web.error;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class ErrorExceptionPanel extends Panel {
			
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ErrorExceptionPanel.class.getName());

	
	public ErrorExceptionPanel(String id, Exception e) {
		super(id);
		add((new Label("title", e.getClass().getName())));
		add((new Label("text", e.getMessage())));
		
		if (logger.isDebugEnabled() ) {
			add((new Label("stacktrace", e.getStackTrace())));
		}
		else
			add((new Label("stacktrace", "")));
		
	}
	
}
