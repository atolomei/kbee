package kbee.web.nav;


import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;


public class TasksDropDownMenuBC extends DropDownMenuBC<Void> {

	final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	
	final boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_pending = role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.PENDING_TASKS.getId());
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public TasksDropDownMenuBC() {
		this("bc-menu-item");
	}
	
	public TasksDropDownMenuBC(String id) {
		super(id);
		addElement(new TasksSectionBC(), true);
		addElement(new WorkspaceBC());
		addElement(new MonitorBC());
		
		if (role_pending)
			addElement(new PendingTasksBC());
		
		if (role_admin)
			addElement(new DashboardBC());

	}
	
	
}
