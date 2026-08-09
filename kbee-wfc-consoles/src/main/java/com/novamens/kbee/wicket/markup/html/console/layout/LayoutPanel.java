package com.novamens.kbee.wicket.markup.html.console.layout;


import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.util.io.IClusterable;

public class LayoutPanel implements IClusterable {
	private static final long serialVersionUID = 1L;
	private WebMarkupContainer panel;
	private String markupId;
	private int disposition;
	
	public LayoutPanel(WebMarkupContainer panel, int disposition) {
		this.panel = panel;
		this.disposition = disposition;
	}
	
	public LayoutPanel(String markupId, int disposition) {
		this.markupId = markupId;
		this.disposition = disposition;
	}
	
	public WebMarkupContainer getPanel() {
		if (panel==null) {
			panel = getPanel(markupId);
			panel.setVisible(false);
		}
		return panel;
	}

	public int getDisposition() {
		return disposition;
	}
	
	public boolean isVisible() {
		return panel!=null && panel.isVisible();
	}
	
	public void setVisible(boolean value) {
		if (panel==null) {
			panel = getPanel(markupId);
		}
		panel.setVisible(value);
	}
	
	protected WebMarkupContainer getPanel(String id) {
		return null;
	}
};