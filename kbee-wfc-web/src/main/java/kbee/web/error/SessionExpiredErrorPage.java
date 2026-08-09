package kbee.web.error;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;

import kbee.util.PropertiesFactory;

public class SessionExpiredErrorPage extends AbstractKbeeWebPage {

	private static final long serialVersionUID = 4590933068140921666L;

	private static final String XUA_Compatible =  PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.portal.xuacompatible", "IE=Edge");

	@SuppressWarnings("unused")
	static private String xtitle;
	
	@SuppressWarnings("unused")
	static private String xdescription;
	
	@SuppressWarnings("unused")
	static private String xkeywords="Document Director";
	
	static {
			xdescription = PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.content.web.description", "kbee Session expired");
			xtitle 		 = PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.content.web.title", "kbee");
			xkeywords	 = PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.content.web.keywords", "kbee");
	}
	
	public SessionExpiredErrorPage(PageParameters param) {
		init(param);
	}
	
	public SessionExpiredErrorPage() {
		init(null);
	}
		
	private void init(PageParameters param) {
		setPageFonts(getFonts());
		setPageXUACompatible(XUA_Compatible);
		add(new Label("errormsg", new StringResourceModel("session-expired.message", SessionExpiredErrorPage.this, null)));
		
	}
	
	
}
