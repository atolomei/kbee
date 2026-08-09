package kbee.web.nav;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


public class AlertManagementDropDownBC extends DropDownMenuBC<Void> {
			
	private static final long serialVersionUID = 1L;
	
	final boolean is_root 				= ServiceLocator.getService(SecurityService.class).isRoot();
	final boolean role_admin 			= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_support				= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	
	
	public AlertManagementDropDownBC() {
	
		addElement(new AlertManagementBC());
		addElement(new BillboardsBC());
		addElement(new WorkflowAlertSettingsBC());
		addElement(new TimeBasedAlertsBC());
		
		

		
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

}
