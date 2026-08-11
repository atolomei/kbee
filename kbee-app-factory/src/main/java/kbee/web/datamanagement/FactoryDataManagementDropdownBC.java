package kbee.web.datamanagement;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserService;
import com.novamens.content.web.admin.markup.datamanagement.SystemDataManagementGeneralPage;
import com.novamens.content.web.admin.markup.datamanagement.SystemSchedulerMonitorPage;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;

import kbee.web.nav.DataManagementBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.ReindexBC;
import kbee.web.nav.SchedulerBC;
import kbee.web.objectstorage.ObjectStoragePage;
import kbee.web.scheduler.SchedulerCronJobsPage;

public class FactoryDataManagementDropdownBC extends DropDownMenuBC<Void> {

	/**
	 * 
	 *         this.mountPage("/datamanagement/tagtool", TagManagementPage.class);
        this.mountPage("/datamanagement/${id}", SystemDataManagementGeneralPage.class);
        this.mountPage("/datamanagement/reindex", ReindexPage.class);
        this.mountPage("/datamanagement/scheduler", SystemSchedulerMonitorPage.class);
        this.mountPage("/datamanagement/scheduler/request", SchedulerRequestPage.class);
        this.mountPage("/datamanagement/scheduler/cronjobs", SchedulerCronJobsPage.class);
		this.mountPage("/datamanagement/objectstorage", ObjectStoragePage.class);
        this.mountPage("/datamanagement/cache", ThumbnailServicePage.class);

	 */
	private static final long serialVersionUID = 1L;

	public  FactoryDataManagementDropdownBC() {
		
		/**
		 Scheduler
		Database Gateway
		Reindex
		Object Storage
		Cache
		Data Management
		File Explorer
		Deploy Manager
		*/
		
		addElement(new DataManagementBC(), true);
		addElement(new ObjectStorageBC());
        addElement(new SchedulerBC());  
        addElement(new ReindexBC());    
        addElement(new CacheBC());
        
		
	     //addElement(new DatabaseGatewayBC());
    
	 
		
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
