package com.novamens.kbee.wicket.markup.html.console.layout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

import kbee.web.console.Layout;

public abstract class AbstractLayout extends Layout {
	
	private static final long serialVersionUID = 1L;
	
	
	private List<LayoutPanel> panels;
	

	public AbstractLayout(String id, LayoutPanel... panels) {
		super(id);
		setPanels(panels);
	}
	
	@Override
	public void addPanel(Panel panel, int disposition) {
		this.panels.add(new LayoutPanel(panel, disposition));
	}
	
	@Override
	public void addPanel(Panel panel) {
		this.panels.add(new LayoutPanel(panel, MAIN_DISPOSITION));
	}
	
	
	public void setPanels(LayoutPanel... panels) {
		this.panels = new ArrayList<LayoutPanel>();
		for (int i =0 ; i<panels.length; i++) {
			this.panels.add(panels[i]);
		}
	}
	
	public void setPanels(List<LayoutPanel> panels) {
		this.panels = panels;
	}

	@Override
	public <P extends WebMarkupContainer> int getDisposition(Class<P> panelclass) {
		LayoutPanel panel = getLayoutPanel(panelclass);
		if (panel==null)
			return SIDE_DISPOSITION;
		return panel.getDisposition();
	}
	
	
	@Override
	@SuppressWarnings("unchecked")
	public WebMarkupContainer getPanel(int disposition) {
		for (LayoutPanel layoutpanel : panels) {
			if (layoutpanel.getDisposition()==disposition && layoutpanel.isVisible()) {
				return layoutpanel.getPanel();
			}
		}
		for (LayoutPanel layoutpanel : panels) {
			if (layoutpanel.getDisposition()==disposition) {
				WebMarkupContainer panel =  layoutpanel.getPanel();
				panel.setVisible(false);
				return panel;
			}
		}
		return null;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <P extends WebMarkupContainer> P getPanel(Class<P> panelclass) {
		for (LayoutPanel layoutpanel : panels) {
			if (panelclass.isInstance(layoutpanel.getPanel())) {
				return (P)layoutpanel.getPanel();
			}
			else {
				WebMarkupContainer child = getChild(layoutpanel.getPanel(), panelclass);
				if (child!=null) {
					return (P)child;
				}
			}
		}
		return null;
	}
	
	public <P extends WebMarkupContainer> void togglePanel(Class<P> panelclass) {
		
	}
	
	protected <P extends WebMarkupContainer> LayoutPanel getLayoutPanel(Class<P> panelclass) {
		for (LayoutPanel layoutpanel : panels) {
			if (panelclass.isInstance(layoutpanel.getPanel())) {
				return layoutpanel;
			}
			else {
				WebMarkupContainer child = getChild(layoutpanel.getPanel(), panelclass);
				if (child!=null) {
					return layoutpanel;
				}
			}
		}
		return null;
	}
	
	
	protected LayoutPanel getLayoutPanel(int disposition) {
		for (LayoutPanel layoutpanel : panels) {
			if (layoutpanel.getDisposition()==disposition && layoutpanel.isVisible()) {
				return layoutpanel;
			}
		}
		for (LayoutPanel layoutpanel : panels) {
			if (layoutpanel.getDisposition()==disposition) {
				return layoutpanel;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected <P extends WebMarkupContainer> P getChild(WebMarkupContainer panel, Class<P> panelclass) {
		Iterator<Component> childs = panel.iterator();
		while (childs.hasNext()) {
			Component child = childs.next();
			if (panelclass.isInstance(child)) {
				return (P)child;
			}
			else {
				if (child instanceof WebMarkupContainer) {
					child = getChild((WebMarkupContainer)child, panelclass);
					if (child!=null) {
						return (P)child;
					}
				}
			}
		}
		return null;
	}
}
