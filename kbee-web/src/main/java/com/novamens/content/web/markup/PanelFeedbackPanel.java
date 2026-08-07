package com.novamens.content.web.markup;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
 
public class PanelFeedbackPanel extends Panel {

	private static final long serialVersionUID = 8198303508028782783L;
	
	private String pfStr = " <result></result>";
	private boolean panelfeedbackvisible=false;
	
	public PanelFeedbackPanel(String id) {
		super(id);
		setOutputMarkupId(true);
		WebMarkupContainer panelfeedback = new WebMarkupContainer("panel-feedback") {
			private static final long serialVersionUID = 3185836064564565L;
			@Override
			public boolean isVisible() {
				return isVisiblePanelFeedback();
			}
		};
	
		panelfeedback.add( (new Label("msg", new PropertyModel<String>(this, "pfStr"))).setEscapeModelStrings(false));
		AjaxLink<Void> link = new AjaxLink<Void>("close") {
			private static final long serialVersionUID = -6907224034512175993L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				setVisiblePanelFeedback(false);
				onClose(target);
			}
		};
		panelfeedback.add(link);
		add(panelfeedback);
	}
	
	public void onClose(AjaxRequestTarget target) {}
	
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
	
	/*	public void onTimedFeedback(AjaxRequestTarget target) {
		target.add(this);
		target.appendJavaScript("setTimeout(\"hidefeedback('"+this.getMarkupId()+"')\",12000);");
	}
	*/
}
