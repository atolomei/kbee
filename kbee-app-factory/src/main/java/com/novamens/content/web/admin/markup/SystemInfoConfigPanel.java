package com.novamens.content.web.admin.markup;

import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.areainfo.AreaInfoPanel;
import com.novamens.wicket.util.BCElement;

public class SystemInfoConfigPanel extends AbstractSystemInfoPanel {
			
	private static final long serialVersionUID = 1L;

	public SystemInfoConfigPanel() {
		this("info-panel");
	}
	
	public SystemInfoConfigPanel(String id) {
		super(id);
	}
	
	/**
	 * 
	 */
	public void onInitialize() {
		super.onInitialize();
		
		AreaInfoPanel area = new AreaInfoPanel("info");
		add(area);
		area.setSections(AreaInfoPanel.ONE_SECTION);
		area.setCss("col-lg-12");
		area.addPanel(new ConfigFilesInfoPanel("element"));
	
		
	}
	
	protected BCElement getPageBCElement() {
		return new BCElement(new Model<String>("Config"));
	}

}
