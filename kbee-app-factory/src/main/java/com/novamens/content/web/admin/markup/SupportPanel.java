package com.novamens.content.web.admin.markup;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;

import org.apache.wicket.model.StringResourceModel;

import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.util.InvisiblePanel;


/**
 *  ----------------------------------------
 *  
 *  Default Info 
 *  
 *  email 1
 *  password 1
 *  
 *  email 2
 *  password 2
 *  
 *  
 *  ----------------------------------------
 *  Reset All
 *  
 *  New Password
 *  
 *  
 *  ----------------------------------------
 *  Users
 *  
 *  
 *  
 *  
 *  [ _______________________ ]
 *  
 *  [ _______________________ ]
 *  
 *  ----------------------------------------
 *    
 *   Default Support Password
 *   [ ______________________ ] 
 *
 *
 */
public class SupportPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public SupportPanel(String id) {
		super(id);
		
		List<ITab> tabs = new ArrayList<ITab>();

		// --------------------------------------------------------------------------------------------------
		// Summary
		//
		tabs.add(new AbstractTab(new StringResourceModel("info", this, null)) {
			private static final long serialVersionUID = 1L;
			@Override
			public Panel getPanel(String panelId) {
				return new InvisiblePanel(panelId);
			}
		});
		
		// --------------------------------------------------------------------------------------------------
		//
		tabs.add(new AbstractTab(new StringResourceModel("reset", this, null)) {
			private static final long serialVersionUID = 1L;
			@Override
			public Panel getPanel(String panelId) {
				return new InvisiblePanel(panelId);
			}
		});

		// --------------------------------------------------------------------------------------------------
		//
		/*tabs.add(new AbstractTab(new StringResourceModel("users", this, null)) {
			private static final long serialVersionUID = 1L;
			@Override
			public Panel getPanel(String panelId) {
				return new InvisiblePanel(panelId);
			}
		});
		*/
		
		VerticalLayout<ITab> info = new VerticalLayout<ITab>("tabs", "factory", tabs);
		add(info);
	}

}
