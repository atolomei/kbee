package kbee.web.dashboard;

import org.apache.wicket.model.IModel;

import com.novamens.content.model.EntityMember;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.security.KbeeUser;

import kbee.web.content.console.MonitorPage;
import kbee.web.query.PendingTasksQuery;

public class DashboardPendingEntityTasksWidgetPanel extends DashboardPendingTasksWidgetPanel  {
 	private static final long serialVersionUID = 1L;
 	
	IModel<EntityMember> entitymodel;
	
	public DashboardPendingEntityTasksWidgetPanel(String id, IModel<EntityMember> entitymodel, String preferences_key) {
		super(id, preferences_key);
		this.entitymodel = entitymodel;
		
		setTitle(getLabel("pending-title", getEntity().getDisplayName()));
	}
	
	public EntityMember getEntity() {
		return entitymodel.getObject();
	}

	@Override
	protected ResultSet getTasks() {
		KbeeUser us = (KbeeUser) getSessionUser();
		return us.getService(UserDashboardService.class).getPendingTasks(getEntity());
	}
	
	@Override
	protected void onClickAll() {
		PendingTasksQuery query = new PendingTasksQuery(getQueryIndex());
		query.setAsParameter(getEntity());
		setResponsePage(new MonitorPage(query));
	}
}