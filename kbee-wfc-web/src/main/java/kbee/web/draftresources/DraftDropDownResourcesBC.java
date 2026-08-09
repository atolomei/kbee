package kbee.web.draftresources;

import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;

import kbee.web.nav.DashboardBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.MonitorBC;
import kbee.web.nav.MyBoxBC;
import kbee.web.nav.PendingTasksBC;
import kbee.web.nav.TasksSectionBC;
import kbee.web.nav.WorkspaceBC;

public class DraftDropDownResourcesBC extends DropDownMenuBC<Void> {

	
final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	
	final boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	//final boolean bulk_create = role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.WORKSPACE_BULK_ACTIONS.getId());
	final boolean role_pending = role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.PENDING_TASKS.getId());
	// final boolean is_my_box  = role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.WORKSPACE_MY_RESOURCES.getId());;
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public DraftDropDownResourcesBC() {
		this("bc-menu-item");
	}
	
	public DraftDropDownResourcesBC(String id) {
		super(id);

		addElement(new DraftResourcesBC(), true);
		addElement(new  MyBoxBC());
		addElement(new  PublicBoxBC());

		  
	}
	

}
