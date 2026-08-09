package kbee.web.console;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.wicket.markup.html.panel.KBPanel;

public abstract class Layout extends KBPanel {
	
	public static int MAIN_DISPOSITION = 0;
	public static int SIDE_DISPOSITION = 1;
	public static int TOP_DISPOSITION  = 2;
	
	private static final long serialVersionUID = 1L;
	
	public Layout(String id) {
		super(id);
	}
	
	public abstract <P extends WebMarkupContainer> P getPanel(Class<P> panelclass);
	public abstract <P extends WebMarkupContainer> void togglePanel(Class<P> panelclass);
	public abstract <P extends WebMarkupContainer> P getPanel(int disposition);
	
	public abstract void addPanel(Panel panel, int disposition);
	public abstract void addPanel(Panel panel);
	
	public abstract <P extends WebMarkupContainer> int getDisposition(Class<P> panelclass);
	
}