package kbee.web.searcher.page;

import java.util.Calendar;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;

public class CopyrightPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	static String year;
	
	static { 
		Calendar now = Calendar.getInstance();
		year = String.valueOf(now.get(Calendar.YEAR));
	}
	
	public CopyrightPanel(String id) {
		super(id);
		
		add(new Label("text", "© 2000 - " + year));
		add(new Label("rights-reserved", new StringResourceModel("reserved", this, null)));
	}

}
