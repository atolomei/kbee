package com.novamens.content.web;  

import java.util.Map;
import java.util.function.Supplier;

import com.novamens.beans.BeansService;
import com.novamens.content.web.test.Test1;
// import com.novamens.content.web.test.Test2;
// import com.novamens.content.web.test.Test3;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;

import kbee.web.application.BaseWebApplication;
import kbee.web.application.ExceptionMapper;
import kbee.web.page.HomeSimplePage;
import kbee.web.util.WebMapping;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.Page;
import org.apache.wicket.SystemMapper;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.IExceptionMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

 
//import com.novamens.content.web.security.login.HomeSimplePage;
//import com.novamens.content.web.util.ExceptionMapper;
import com.novamens.kbee.content.webapi.resource.ApiResourceReferenceMapper;

public class WebApplication extends  BaseWebApplication {
			
	private static org.apache.logging.log4j.Logger startlogger = LogManager.getLogger("StartupLogger");
	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(WebApplication.class.getName());
	
	private Class<? extends Page> class_home;

	/**
	 * IDOC or KBEE
	 */
	@Override
	public String[] getProductCursesName() {
		return ServiceLocator.getService(BrandingService.class).getProductCursesName();
	}
	
	@Override
	public Class<? extends Page> getHomePage() {
		
		if (class_home!=null)
			return class_home;
		
		WebPage page  = null;
		
		try {
			page = (WebPage) ServiceLocator.getService(BeansService.class).getBean("home-page");
		} 
		catch (Exception e) {
		}
		
		logger.debug("homepage -> " + (page!=null?page.getClass().getName() : " null"));
		
		if (page!=null) {
			class_home =  page.getClass();
			return page.getClass();
		}
		
		logger.debug("default  -> " + HomeSimplePage.class.getName());
		
		class_home = HomeSimplePage.class;
		return HomeSimplePage.class;
	}

	
	@Override
	public ApplicationContext getApplicationContext() {
		return WebApplicationContextUtils.getRequiredWebApplicationContext( ( javax.servlet.ServletContext) this.getServletContext());
	}

	@Override
	public Supplier<IExceptionMapper> getExceptionMapperProvider()	{
		return new Supplier<IExceptionMapper>() {
			@Override
			public IExceptionMapper get() {
				return new ExceptionMapper();
			}
		};
	}

	public void onStartup() {
		super.onStartup();
		startlogger.info("Startup in: " + String.format( "%8.2f", (System.currentTimeMillis()-getStartTime()) / 1000.0).trim() + " secs");
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override 
	protected void init() {
		super.init();
		
		this.mountPage("/test/${id}", Test1.class);
		//this.mountPage("/test2", Test2.class);
		//this.mountPage("/test3", Test3.class);

		logger.debug("Starting init -> "+ this.getClass().getName());
		
		BeansService beans = ServiceLocator.getService(BeansService.class);
		WebMapping sf = (WebMapping)beans.getBean("webMapping");

		for (Map.Entry<String, String> mapping : sf.getMapping().entrySet()) {
			try {
				this.mountPage(mapping.getKey(),  (Class) Class.forName(mapping.getValue()));
				logger.debug(mapping.getKey() +" -> " +	((Class) Class.forName(mapping.getValue())).getName());
			} 
			catch (ClassNotFoundException e) {
				startlogger.error(e);
				logger.error(e);
			}
		}

		((SystemMapper)this.getRootRequestMapper()).add(new ApiResourceReferenceMapper());
		onStartup();
	}
}
