package kbee.web.searcher.panel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class SearcherSimpleErrorPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public SearcherSimpleErrorPanel(String id, String title, String text) {
		super(id);
		add((new Label("title", title)).setVisible(title!=null));
		add((new Label("text", text)).setEscapeModelStrings(false));
	}

}
