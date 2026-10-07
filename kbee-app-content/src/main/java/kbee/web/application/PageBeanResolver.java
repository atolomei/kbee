package kbee.web.application;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.component.IRequestablePage;

import com.novamens.beans.BeansService;
import com.novamens.service.ServiceLocator;

public class PageBeanResolver implements PageResolver {

	private String bean;
	private Class<? extends IRequestablePage> pageClass;
	private Class<? extends IRequestablePage> defaultClass;
	
	public PageBeanResolver(String bean, Class<? extends IRequestablePage> defaultClass) {
		this.bean = bean;
		this.defaultClass = defaultClass;
	}
	
	public WebPage getPage() {
		try {
			Class<? extends IRequestablePage> clazz = (pageClass!=null) ? pageClass : defaultClass;
			WebPage page = (WebPage)clazz.getDeclaredConstructor().newInstance();
			return page;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
    @Override
    public Class<? extends IRequestablePage> resolve(Request request) {
    	if (pageClass==null) {
    		try {
	    		Object page = ServiceLocator.getService(BeansService.class).getBean(bean);
	    		if (page!=null)
	    			pageClass = ((WebPage)page).getClass();
	    		else
	    			pageClass = defaultClass;
    		}
    		catch (Exception e) {
    			/** there is no bean defined for this page -> use the default one */
    			pageClass = defaultClass;
    		}
    	}
    	return pageClass;
    }
}