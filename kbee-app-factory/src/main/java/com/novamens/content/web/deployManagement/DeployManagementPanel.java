package com.novamens.content.web.deployManagement;


import com.novamens.content.web.admin.markup.AbstractSystemInfoPanel;
import com.novamens.kbee.wicket.markup.html.areainfo.AreaInfoPanel;
import com.novamens.service.ServiceLocator;

import kbee.web.error.ErrorPanel;

import org.apache.wicket.model.Model;


public class DeployManagementPanel extends AbstractSystemInfoPanel {
			
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DeployManagementPanel.class.getName());

    private static final long serialVersionUID = 1L;

    public DeployManagementPanel() {
        this("info-panel");
    }

    public DeployManagementPanel(String id) {
        super(id);
    }

    public void onInitialize() {
        super.onInitialize();
        
        

        AreaInfoPanel area = new AreaInfoPanel("info");
        add(area);
        area.setSections(AreaInfoPanel.ONE_SECTION);
        area.setCss("col-lg-12");

        if (hasPermissions()) 
        	area.addPanel(new DeployManagementFormPanel("element"));
        else
        	area.addPanel(new ErrorPanel("element", new Model<String>("Not authorized")));
    }

	final boolean root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	
	protected boolean hasPermissions() {
		return root && isKbeeDomain();
	}

}
