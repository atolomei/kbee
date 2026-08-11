package kbee.web.systeminfo;

import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.HREFBCElement;

import kbee.web.datamanagement.CacheBC;
import kbee.web.datamanagement.DatabaseGatewayBC;
import kbee.web.datamanagement.ObjectStorageBC;
import kbee.web.nav.DataManagementBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.ReindexBC;
import kbee.web.nav.SchedulerBC;
import kbee.web.nav.SystemInfoBC;



/**
 * 
 * Dashboard
Database
Search
Hardware
Scheduler
API
Properties
System Parameters
Logs 
Config 
Version 
JVM Threads

 *
 */
public class FactorySystemInfoDropdownBC extends DropDownMenuBC<Void> {

	private static final long serialVersionUID = 1L;

	public  FactorySystemInfoDropdownBC() {
		
		addElement(new SystemInfoBC(), true);
		
		addElement(new SystemInfoBC());
		addElement(new HREFBCElement("/systeminfo/keymetrics", getLabel("dashboard")));
		addElement(new HREFBCElement("/systeminfo/hardware", new Model<String>("Hardware")));
		addElement(new HREFBCElement("/systeminfo/api-dashboard", getLabel("mainmenu.api.dashboard")));
		addElement(new HREFBCElement("/systeminfo/properties", getLabel("properties")));
		addElement(new HREFBCElement("/systeminfo/parameters", getLabel("system.parameters")));
		addElement(new HREFBCElement("/systeminfo/logs", getLabel("mainmenu.systemlogs")));
		addElement(new HREFBCElement("/systeminfo/config", getLabel("configuration")));
		addElement(new HREFBCElement("/systeminfo/version", getLabel("version")));
		addElement(new HREFBCElement("/systeminfo/jvm-threads", new Model<String>("JVM Threads")));
		//addElement(new HREFBCElement("/ping", new Model<String>("Ping")));

		
		
	}

	public void onDetach() {
		super.onDetach();
	}
	
	
	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
//	
//	@SuppressWarnings("unused")
//	private Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getDomain();
//	}
}
