package com.novamens.content.web.report.markup;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;

import kbee.web.report.ReportPanel;

public class ReportWindowPanel extends ReportPanel {
	private static final long serialVersionUID = 1L;
	ReportPanel reportPanel;
	
	@SuppressWarnings("serial")
	public ReportWindowPanel(String id, ReportPanel reportPanel) {
		super(id);
		
		this.reportPanel = reportPanel;
		
		add(reportPanel);
		
		AjaxLink<Void> close = new AjaxLink<Void>("close") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				ReportWindowPanel.this.onClose(target);
			}
		};
		
		add(close);
	}
	
	public String getTitle() {
		return reportPanel.getTitle();
	}
	
	public void onClose(AjaxRequestTarget target) {
	}
}
