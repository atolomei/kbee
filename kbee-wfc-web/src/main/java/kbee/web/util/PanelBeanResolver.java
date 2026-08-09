package kbee.web.util;

import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.beans.BeansService;
import com.novamens.service.ServiceLocator;

public class PanelBeanResolver  {

	private String bean;
	private Object[] args;
//	private Class<? extends IRequestablePage> pageClass;
	
	public PanelBeanResolver(String bean, Object... args) {
		this.bean = bean;
		this.args = args;
	}
	
	public Panel getPanel() {
		try {
			Panel panel = (Panel)ServiceLocator.getService(BeansService.class).getBean(bean, args);
			return panel;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}