package fileserver;


import java.util.function.Supplier;

import org.apache.wicket.request.IExceptionMapper;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;
import com.novamens.spring.service.SpringServiceLocator;

import kbee.util.logging.Logger;
import kbee.web.application.ExceptionMapper;


public abstract class AbstractTest {
	
	private static kbee.util.logging.Logger logger = Logger.getLogger(AbstractTest.class.getName());

	private SessionFactory sessionFactory;
	private ContentDao dao;
	private Domain domain = null;
	
	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		
		/**
		
		String[] args = {
						"-XX:HotswapAgent=core",
						"-Xbootclasspath/a:C:\\Users\\atolo\\eclipse-workspace\\idoc-config\\src\\main\\resources",
						"-Dlog4j.configurationFile=C:\\Users\\atolo\\eclipse-workspace\\idoc-config\\src\\main\\resources\\log4j2-dev.xml", 
						"-Dcom.novamens.kbee.jettyProfilesDir=C:\\Users\\atolo\\eclipse-workspace\\idoc-config\\src\\main\\resources\\jprofiles\\", 
						"-Dcom.novamens.kbee.jettyProfiles=jetty-threadpool.xml;jetty.xml;jetty-http.xml",
						"-Dhttps.protocols=TLSv1,TLSv1.1,TLSv1.2",
						"-Djetty.port=8080",
						"-Xms6G",
						"-Xmx20G", 
						"-DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector"
		};
		
		logger.debug("Setup");
		com.novamens.content.web.Main.main( args );
		logger.debug("after Setup");
		
		**/
		
		logger.debug("setUp() ------------------------------------------------------- ");
		
        if (ServiceLocator.getInstance() == null)
            ServiceLocator.setInstance(new SpringServiceLocator("kbee"));

		
		BeansService beans = ServiceLocator.getService(BeansService.class);
		sessionFactory = (SessionFactory)beans.getBean("sessionFactory");
		TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(sessionFactory.openSession()));

		AuthenticationManager authenticationManager = (AuthenticationManager)beans.getBean("authenticationManager");
		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken("root", "root");
		Authentication authentication = authenticationManager.authenticate(authRequest);
		SecurityContext securityContext = SecurityContextHolder.getContext();
		securityContext.setAuthentication(authentication);
	}
	
	/**
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
		
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
		SessionFactoryUtils.closeSession(sessionHolder.getSession());
	}
	

	
	/**
	 * 
	 * 
	 * 
	 * @throws Exception
	 */
	@Test
	public void test() {
		
	    BeansService beans = ServiceLocator.getService(BeansService.class);
		dao =  (ContentDao) beans.getBean("contentDao");
		run();
	}
	
	
	public abstract void run();
	
	
	
	
	
    

    public Supplier<IExceptionMapper> getExceptionMapperProvider() {
        return new Supplier<IExceptionMapper>() {
            @Override
            public IExceptionMapper get() {
                return new ExceptionMapper();
            }
        };
    }

	
	protected Domain getDomain() {
		return domain;
	}
	
	protected void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	protected ContentDao getContentDao() { 
		return dao; 
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}



/**
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
**/



/**

**/


