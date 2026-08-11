package com.novamens.portal.model.diagrammablesite;

import org.apache.wicket.markup.html.panel.Panel;

public interface ProxyArea {

	public DiagrammableArea getArea();
	public DiagrammableSite getCallerSite();

	// public Panel getPanel(String string, boolean b);
	
}
