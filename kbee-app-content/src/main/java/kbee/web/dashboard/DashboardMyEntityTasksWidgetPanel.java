package kbee.web.dashboard;

import org.apache.wicket.model.IModel;

import com.novamens.content.model.EntityMember;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.portal6.model.PortalViewRender;

import kbee.web.content.console.WorkspacePage;
import kbee.web.query.WorkspaceQuery;

public class DashboardMyEntityTasksWidgetPanel extends DashboardMyTasksWidgetPanel implements PortalViewRender {
	private static final long serialVersionUID = 1L;
	
	IModel<EntityMember> entitymodel;
	
	public DashboardMyEntityTasksWidgetPanel(String id, IModel<EntityMember> entitymodel, String preferences_key) {
		super(id, preferences_key);
		this.entitymodel = entitymodel;
		setTitle(getLabel("mytasks-title", getEntity().getDisplayName()));
	}
	
	public EntityMember getEntity() {
		return entitymodel.getObject();
	}

	@Override
	protected ResultSet getTasks() {
		KbeeUser us = (KbeeUser) getSessionUser();
		return us.getService(UserDashboardService.class).getMyTasks(getEntity());
	}
	
	@Override
	protected void onClickAll() {
		WorkspaceQuery query = new WorkspaceQuery(getQueryIndex());
		query.setAsParameter(getEntity());
		setResponsePage(new WorkspacePage(query));
	}
}