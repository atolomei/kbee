package kbee.web.nav;

import com.novamens.wicket.markup.html.panel.KBPanel;

/**
 * 
 * 
 *
 * @param <T>
 */
public abstract class NavigationPanel<T> extends KBPanel {

	private static final long serialVersionUID = 1L;

	public NavigationPanel(String id) {
		super(id);
	}
	
	public abstract void navigate();
	
	public void onNavigate(T object) {};
	
	public abstract boolean isFromContentBase();
}