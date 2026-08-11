package com.novamens.content.web.deployManagement;

import com.novamens.content.web.admin.markup.SystemInfoGeneralPage;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


public class DeployManagementPage extends SystemInfoGeneralPage {

    
	private static final long serialVersionUID = 1L;
	
	boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
    boolean role_support = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());

    public DeployManagementPage() {
        super("backups");
    }

    @Override
    public void onInitialize() {
        super.onInitialize();
    }

    @Override
    public boolean hasPermissions() {

        if (this.role_support || this.role_admin)
            return true;

        boolean role_reports = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.REPORTS.getId());

        return role_reports;
    }

}
