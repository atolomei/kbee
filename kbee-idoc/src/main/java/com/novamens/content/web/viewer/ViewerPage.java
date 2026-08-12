package com.novamens.content.web.viewer;

import org.apache.wicket.markup.html.WebMarkupContainer;

import com.novamens.content.web.base.page.component.KbeeContentFooter;
// import com.novamens.kbee.wicket.markup.html.console.behavior.AjustableHeightBehavior;

				
public class ViewerPage extends com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage {
	
private static final long serialVersionUID = 1L;

public ViewerPage() {
	 
	setPageFonts(getFonts());
    WebMarkupContainer canvas = new WebMarkupContainer("canvas");
	// canvas.add(new AjustableHeightBehavior(50+92));
	add(canvas);
	
	add(new KbeeContentFooter("footer"));
	}

}
