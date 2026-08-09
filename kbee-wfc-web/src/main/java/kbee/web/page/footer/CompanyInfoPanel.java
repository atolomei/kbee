package kbee.web.page.footer;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.service.ServiceLocator;

public class CompanyInfoPanel extends Panel {

	
	private static final long serialVersionUID = 1L;

	private static String 	COMPANY_NAME 	= "";
	private static  String  COMPANY_ADDRESS = "";;
	private static  String  COMPANY_STATE 	= "";;
	private static  String  COMPANY_PHONE 	= "";;
	private static  String  COMPANY_URL 	= "";;
	
	private static boolean is_error = false;

	static  {
		try {
			
			ContentDao dao 	 = (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
			
			COMPANY_NAME	 = dao.findSystemParameterValueByKey( "company.name", 		"Novamens");
			COMPANY_ADDRESS  = dao.findSystemParameterValueByKey( "company.addresss", 	"Montañeses 1853");
			COMPANY_STATE    = dao.findSystemParameterValueByKey( "company.state", 		"Ciudad de Buenos Aires");
			COMPANY_PHONE    = dao.findSystemParameterValueByKey( "company.phone", 		"");
			COMPANY_URL		 = dao.findSystemParameterValueByKey( "company.url", 		"https://novamens.com");
			
		} catch (Exception e) {
			is_error = true;
		}
	}

	public CompanyInfoPanel(String id) {
		super(id);
	}
		
		@Override
		public void onInitialize() {
			super.onInitialize();
			
			Link<Void> link = new Link<Void>("company-link") {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick() {
					setResponsePage(new RedirectPage(COMPANY_URL));
				}
			};
			
			add(link);
			
			link.add( (new Label("name", COMPANY_NAME)).setVisible(!is_error));
			// add( (new Label("street", COMPANY_ADDRESS)).setVisible(!is_error));
			link.add( (new Label("county-state", COMPANY_STATE)).setVisible(!is_error));
			link.add( (new Label("phone", COMPANY_PHONE)).setVisible(!is_error));	
		}
		
}


