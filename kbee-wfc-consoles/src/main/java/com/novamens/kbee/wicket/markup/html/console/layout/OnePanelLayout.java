package com.novamens.kbee.wicket.markup.html.console.layout;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;

public class OnePanelLayout extends AbstractLayout {
	
	private static final long serialVersionUID = 1L;
	
	
	public OnePanelLayout(String id) {
		super(id);
		setOutputMarkupId(true);
	}
	
	public OnePanelLayout(String id, LayoutPanel panel) {
		super(id);
		setOutputMarkupId(true);
		panel.getPanel().setMarkupId("main");
		setPanels(panel);
	}

	
	public OnePanelLayout(String id, LayoutPanel panel, LayoutPanel top) {
		super(id);
		setOutputMarkupId(true);
		panel.getPanel().setMarkupId("main");
		top.getPanel().setMarkupId("top");
		setPanels(panel, top);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (get("panels-container")==null) {
			WebMarkupContainer pc = new WebMarkupContainer("panels-container");
			add(pc);
			
			pc.add(getPanel(MAIN_DISPOSITION));
			pc.add(getPanel(TOP_DISPOSITION));
			
			pc.setOutputMarkupId(true);
			
			pc.add(new AttributeModifier("class", new Model<String>() {
				private static final long serialVersionUID = 1L;
				public String getObject() {
					LayoutPanel tp = getLayoutPanel(TOP_DISPOSITION);
					if (tp!=null && tp.isVisible())
						return "row hastop";
					else
						return "row";
				}
			}));
		}
	}
	
	public void onDetach() {
		super.onDetach();
	}
}
