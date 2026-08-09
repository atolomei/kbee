package kbee.web.support;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.service.AppMonitoringService;
import com.novamens.dom.KBFSStorageType;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.util.PropertiesFactory;

public class SupportFooterPanel extends KBPanel {

	
	private static final long serialVersionUID = 1L;





	
	public SupportFooterPanel(String id) {
		super(id);
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		Link<Void> link = new Link<Void>("support-link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				setResponsePage( new ReportIssuePage());
			}
		};
		 
		add(link);
		link.add(new Label("support-label", new StringResourceModel("report-issue", this, null)));
		
		link.setVisible(ServiceLocator.getService(AppMonitoringService.class).isSupportEnabled());
	}



}
