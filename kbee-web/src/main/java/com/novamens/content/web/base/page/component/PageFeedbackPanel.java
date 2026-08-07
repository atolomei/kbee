package com.novamens.content.web.base.page.component;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class PageFeedbackPanel extends Panel {

	private static final long serialVersionUID = 8198303508028782783L;
	
	private String pfStr;
	private boolean panelfeedbackvisible=false;
	
	public PageFeedbackPanel(String id) {
		super(id);
		
		setOutputMarkupId(true);
		
		WebMarkupContainer panelfeedback = new WebMarkupContainer("page-feedback") {
			private static final long serialVersionUID = 318583606570185085L;
			@Override
			public boolean isVisible() {
				return isVisiblePanelFeedback();
			}
		};
	
		panelfeedback.add(new Label("msg", new PropertyModel<String>(this, "pfStr")));
		add(panelfeedback);
	}
	
	public void setPanelFeedbackMsg(String msg) {
		pfStr=msg;
	}
	
	public String getPanelFeedbackMsg() {
		return pfStr;
	}
	
	private void setVisiblePanelFeedback(boolean b) {
		panelfeedbackvisible=b;
	}
	
	private boolean isVisiblePanelFeedback() {
		 	return panelfeedbackvisible;
	}

	public void showFeedbackMsg(String msg) {
		setPanelFeedbackMsg(msg);
		setVisiblePanelFeedback(true);
	}
}
