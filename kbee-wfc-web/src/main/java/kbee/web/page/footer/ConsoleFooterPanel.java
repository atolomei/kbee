package kbee.web.page.footer;
		
import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.kbee.wicket.util.InvisiblePanel;

import kbee.web.searcher.page.CopyrightPanel;
import kbee.web.searcher.page.LegalPanel;
import kbee.web.support.SupportFooterPanel;

/**
 *  Spring bean
 *  
 *  footer = (Panel) ServiceLocator.getService(BeansService.class).getBean("console-footer", "console-footer");
 *  
 *  
 *  com.novamens.content.web.base.page.markup.ConsoleFooterPanel
 *  
 *  
 *  <!-- bean id="console-footer" class="com.novamens.content.web.base.page.markup.ConsoleFooterPanel"  scope="prototype"/-->
 *  
 *  
 */
public class ConsoleFooterPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public ConsoleFooterPanel(String id) {
		super(id);
	
	}
	
	@Override
	public void onInitialize() {
			super.onInitialize();
		
		add (new CompanyInfoPanel("company"));
		// add( new SocialPanel("social"));
		//add (new InvisiblePanel("company"));
		//add (new InvisiblePanel("social"));
			
		// add(new CopyrightPanel("copyright"));
		add(new LegalPanel("legal", null));
		add (new SupportFooterPanel("support"));
		
		
	}
	
	

	/**
	 
	 *  Consulte términos y condicione


     ---------------------------------------------------------------------------------------------------------------------
     [info organization]
     ---------------------------------------------------------------------------------------------------------------------
     																			Social Networks [facebook], link
     copyright 																	legal menu
	 ---------------------------------------------------------------------------------------------------------------------
	 */
}
