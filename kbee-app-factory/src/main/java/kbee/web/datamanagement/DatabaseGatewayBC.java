package kbee.web.datamanagement;

import org.apache.wicket.markup.html.pages.RedirectPage;

import com.novamens.wicket.util.BCElement;

			
public class DatabaseGatewayBC extends BCElement {
	
	public DatabaseGatewayBC() {
		super("mainmenu.sqlgateway");
	}

	@Override
	public void onClick() {
	    setResponsePage( new RedirectPage("/datamanagement/sql-gateway"));
	}


	
}
