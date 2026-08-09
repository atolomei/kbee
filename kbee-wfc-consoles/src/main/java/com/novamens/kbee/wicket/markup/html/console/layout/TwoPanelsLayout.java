package com.novamens.kbee.wicket.markup.html.console.layout;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;



/**
 * 
 * Includes 
 * 1 Top panel 
 * 1 Left main panel
 * 1..n Side panels on the Right (ConsoleSidePanel) 
 * 
 * 
 *
 */
public class TwoPanelsLayout extends AbstractLayout {

	private static final long serialVersionUID = 1L;
	
	
	public TwoPanelsLayout(String id, LayoutPanel... panels) {
		super(id);
		setOutputMarkupId(true);
		for (int i =0 ; i<panels.length; i++) {
			LayoutPanel layoutpanel = panels[i];
			if (layoutpanel.getDisposition() == MAIN_DISPOSITION)				layoutpanel.getPanel().setMarkupId("main");
			else if (layoutpanel.getDisposition() == TOP_DISPOSITION)			layoutpanel.getPanel().setMarkupId("top");
			else
				layoutpanel.getPanel().setMarkupId("side");
		}
		setPanels(panels);
	}
	
	@Override
	public void addPanel(Panel panel) {
		panel.setVisible(false);
		super.addPanel(panel, SIDE_DISPOSITION);
	}

	/**
	 */
	public <P extends WebMarkupContainer> void togglePanel(Class<P> panelclass) {
						
		LayoutPanel panel = getLayoutPanel(panelclass);
		
		if (panel==null)
			return;
		
		boolean visible = !panel.isVisible();
		
		
		LayoutPanel dispositionpanel = getLayoutPanel(panel.getDisposition());
		
		if (panelclass.isInstance(dispositionpanel.getPanel())) {
			dispositionpanel.setVisible(visible);
		}
		else {
			dispositionpanel.setVisible(false);
			panel.setVisible(visible);
		}
		
		// Top ???
		if ( (panel.getDisposition()==SIDE_DISPOSITION) && (!panelclass.isInstance(pc.get("side")))) {
			pc.addOrReplace(panel.getPanel());
		}
		
		
	}
	
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
	}
	
	WebMarkupContainer pc;
 
	@Override
	public void onInitialize() {
		super.onInitialize();
		if (get("panels-container")==null) {
			 pc = new WebMarkupContainer("panels-container");
			addOrReplace(pc);
			pc.add(getPanel(MAIN_DISPOSITION));
			pc.add(getPanel(SIDE_DISPOSITION));
			pc.add(getPanel(TOP_DISPOSITION));
			pc.setOutputMarkupId(true);
			
			pc.add(new AttributeModifier("class", new Model<String>() {
				private static final long serialVersionUID = 1L;
				public String getObject() {
					LayoutPanel tp = getLayoutPanel(TOP_DISPOSITION);
					if (tp!=null && tp.isVisible())
						return "two-panels-container row hastop";
					else
						return "two-panels-container row";
				}
			}));
		}
	}
	
}
