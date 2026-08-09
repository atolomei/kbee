package kbee.web.searcher.searchform;

import java.lang.reflect.InvocationTargetException;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;

public class KbeeSearcherFormFactory implements SearcherFormFactory {
																								
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeSearcherFormFactory.class.getName());

	private String name;
	private String id;
	private String className;
	private String usage;
	private String domainName;

	public KbeeSearcherFormFactory() {
		super();
	}
	
	public KbeeSearcherFormFactory(String id, String name, String className, String usage, String domainName) {
		this.name = name;
		this.id = id;
		this.className = className;
		this.usage=usage;
		this.domainName=domainName;
	}
	
	public SearcherFormPanel<Site> create() {
		return create(null);
			
	}
	@SuppressWarnings("unchecked")
	@Override
	public SearcherFormPanel<Site> create(String id) {
		try {
			
				SearcherFormPanel<Site> panel;
				
				if (id!=null)
					panel = (SearcherFormPanel<Site>) Class.forName(className).getDeclaredConstructor(String.class).newInstance(id);
				else
					panel = (SearcherFormPanel<Site>) Class.forName(className).getDeclaredConstructor().newInstance();
				
				panel.setTitle(this.name);
				panel.setName(this.name);
				panel.setUsageInfo(this.usage);
				panel.setDomainName(this.domainName);
				return panel;				
				
		} catch (InvocationTargetException | NoSuchMethodException e) {			logger.error(e);
		} catch (InstantiationException e							) {			logger.error(e);
		} catch (IllegalAccessException e							) {			logger.error(e);
		} catch (RuntimeException e									) {			logger.error(e);
		} catch (ClassNotFoundException e							) {			logger.error(e);
		}
		return null;
	}
	


	public String getId() {
		return this.id;
	}
	

	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	public void setDomainName(String name) {
		this.domainName = name;
	}
	
	
	public String getDomainName() {
		return this.domainName;
	}
	
	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	@SuppressWarnings("unused")
	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
	}

	@SuppressWarnings("unused")
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	

		
	
}
