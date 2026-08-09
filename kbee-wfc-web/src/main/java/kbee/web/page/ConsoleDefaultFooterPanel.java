package kbee.web.page;

import org.apache.wicket.markup.html.panel.Panel;

public class ConsoleDefaultFooterPanel extends Panel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *  Spring bean
	 *  
	 *  footer = (Panel) ServiceLocator.getService(BeansService.class).getBean("console-footer", "console-footer");
	 */
	public ConsoleDefaultFooterPanel(String id) {
		super(id);
		
		
	}

}
