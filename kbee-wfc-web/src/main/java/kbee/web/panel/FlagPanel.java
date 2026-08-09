package kbee.web.panel;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

public class FlagPanel extends Panel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FlagPanel(String id) {
		super(id);
			add( new WebMarkupContainer ("flag"));
	}

}
