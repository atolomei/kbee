package kbee.web.model.procedure;

import com.novamens.wicket.util.BCElement;

public class LauncherGroupsBC extends BCElement {
	private static final long serialVersionUID = 1L;
	
	public LauncherGroupsBC() {
		super("bc.launchergroups");
	}
	
	@Override
	public void onClick() {
		setResponsePage(new LauncherGroupsPage());
	}
}