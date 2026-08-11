package com.novamens.content.web.admin.markup;

import org.apache.wicket.model.Model;

import com.novamens.wicket.util.BCElement;

@Deprecated
public class SystemInfoSchedulerPanel extends AbstractSystemInfoPanel {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public SystemInfoSchedulerPanel() {
		this("info-panel");
	}

	
	public SystemInfoSchedulerPanel(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	
	}

	protected BCElement getPageBCElement() {
		return new BCElement(new Model<String>("Schedule"));
	}

}
