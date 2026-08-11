package kbee.web.datamanagement;

import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.HREFBCElement;

import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.SchedulerBC;

public class FactoryInfoDropdownBCOBSOLETE extends DropDownMenuBC<Void> {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public  FactoryInfoDropdownBCOBSOLETE() {
		
		/**
	Dashboard
	Database
	Search
	Hardware
	API
	Properties
	System Parameters
	Logs 
	Config 
	Version 
	JVM Threads

	*/
		
		
	}

	public void onDetach() {
		super.onDetach();
	}
	
	
	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
//	@SuppressWarnings("unused")
//	private Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getDomain();
//	}
}
