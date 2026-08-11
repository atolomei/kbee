package kbee.web.application;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.component.IRequestablePage;

import com.novamens.beans.BeansService;
import com.novamens.service.ServiceLocator;

public class PageBeanResolver implements PageResolver {

	private String bean;
	private Class<? extends IRequestablePage> pageClass;
	
	public PageBeanResolver(String bean, Class<? extends IRequestablePage> defaultClass) {
		this.bean = bean;
	}
	
	public WebPage getPage() {
		try {
			WebPage page = (WebPage)pageClass.getDeclaredConstructor().newInstance();
			return page;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
    @Override
    public Class<? extends IRequestablePage> resolve(Request request) {
    	if (pageClass==null) {
    		Object page = ServiceLocator.getService(BeansService.class).getBean(bean);
    		pageClass = ((WebPage)page).getClass();
    	}
    	return pageClass;
    }
}