package kbee.web.page.footer;

import org.apache.wicket.markup.html.panel.Panel;

public class ConsoleMiniFooterPanel extends Panel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *  Spring bean
	 *  
	 *  footer = (Panel) ServiceLocator.getService(BeansService.class).getBean("console-footer", "console-footer");
	 */
	public ConsoleMiniFooterPanel(String id) {
		super(id);
		
		
	}

}
