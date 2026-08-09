package kbee.web.workflow;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.form.EFormData;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.wicket.markup.html.tabs.AjaxTabbedPanel;
import com.novamens.workflow.Activity;

@SuppressWarnings("serial")
public class AuditEFormSidePanel extends ModelPanel<EFormData> {
	private static final long serialVersionUID = 1L;
	
	private IModel<Activity> activitymodel;
	
	public AuditEFormSidePanel(String id, IModel<Activity> activitymodel, IModel<EFormData> datamodel) {
		super(id, datamodel);
		this.activitymodel=activitymodel;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		setOutputMarkupId(true);
		
		List<ITab> tabs = new ArrayList<ITab>();
		
		tabs.add(new AbstractTab(new StringResourceModel("info", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new AuditActivityInfoPanel(panelId, activitymodel);
			}
		});
		
		if (getModel()!=null) {
			tabs.add(new AbstractTab(new StringResourceModel("log", this, null)) {
				@Override
				public Panel getPanel(String panelId) {
					return new AuditActivityLogPanel(panelId, activitymodel, getModel());
				}
			});
		}

		AjaxTabbedPanel<ITab> tabbedpanel = new AjaxTabbedPanel<ITab>("tabs", tabs) {
			@Override
			protected String  getNavCss() {
				return "nav nav-buttons";
			}
		};
		
		add(tabbedpanel); 
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (this.activitymodel!=null)
			this.activitymodel.detach();
	}
}