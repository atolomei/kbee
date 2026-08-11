package kbee.web.content.console;

import com.novamens.wicket.util.BCElement;

public class RecycleBinBC extends BCElement {
	private static final long serialVersionUID = 1L;

	public RecycleBinBC() {
		super("bc.recyclebin");
	}
	
	@Override
	public void onClick() {
		setResponsePage(new RecycleBinPage());
	}
}
