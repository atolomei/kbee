package kbee.web.error;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class SimpleErrorPanel extends Panel {
	private static final long serialVersionUID = 1L;

	public SimpleErrorPanel(String id, String title, String text) {
		super(id);
		add(new Label("title", title));
		add(new Label("text", text));
	}
}
