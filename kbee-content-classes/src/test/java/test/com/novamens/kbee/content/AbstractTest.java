package test.com.novamens.kbee.content;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.io.XLSModelImport;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.spring.ApplicationContextFactory;
import com.novamens.spring.service.SpringServiceLocator;

import kbee.util.PropertiesFactory;

public abstract class AbstractTest {

	final private org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getName());
	
	private SessionFactory sessionFactory;
	private ContentDao dao;
 
	private Domain domain = null;
	
	protected Domain getDomain() {
		return domain;
	}
	
	protected void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	protected ContentDao getContentDao() { 
		return dao; 
	}
	
	
	//---------------------------------------------------------------------------------------------------------------------------
	//
	@BeforeAll
	public void settUp() throws Exception {
		try {
			PropertiesFactory.getInstance("kbee").getProperties();
			ApplicationContextFactory.getInstance("kbee");
			ServiceLocator.setInstance(new SpringServiceLocator("kbee"));
			BeansService beans = ServiceLocator.getService(BeansService.class);
			sessionFactory = (SessionFactory)beans.getBean("sessionFactory");
			TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(sessionFactory.openSession()));
			
			ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");

//			AuthenticationManager authenticationManager = (AuthenticationManager)beans.getBean("authenticationManager");
//			UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken("root", "root");
//			Authentication authentication = authenticationManager.authenticate(authRequest);
//			SecurityContext securityContext = SecurityContextHolder.getContext();
//			securityContext.setAuthentication(authentication);
		}
		catch (Throwable e) {
			logger.error(e);
			throw e;
		}
	}
	
 
	@AfterAll
	public void tearDown() throws Exception {
		
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
		SessionFactoryUtils.closeSession(sessionHolder.getSession());
	}

	
	

	@Test
	public void test() {
		
	    BeansService beans = ServiceLocator.getService(BeansService.class);
		dao =  (ContentDao) beans.getBean("contentDao");
		run();
	}
	
	
	protected void createDomainIfNotExists(String domainname, File xlsmodel) {
		Domain domain = getContentDao().findDomainByName(domainname);
		if (domain==null) {
			XLSModelImport xmodel = new XLSModelImport(xlsmodel);
		try {
				xmodel.execute();
			} catch (IOException e) {
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			}
		}

	}
	
	public abstract void run();
	
}
