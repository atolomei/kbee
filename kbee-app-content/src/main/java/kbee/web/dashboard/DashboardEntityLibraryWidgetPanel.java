package kbee.web.dashboard;

import org.apache.wicket.model.IModel;

import com.novamens.content.model.EntityMember;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.security.KbeeUser;

import kbee.web.content.console.ContentBasePage;
import kbee.web.query.LibraryQuery;

public class DashboardEntityLibraryWidgetPanel extends DashboardLibraryWidgetPanel {
	private static final long serialVersionUID = 1L;
	
	IModel<EntityMember> entitymodel;
	
	public DashboardEntityLibraryWidgetPanel(String id, IModel<EntityMember> entitymodel, String preferences_key) {
		super(id, preferences_key);
		this.entitymodel = entitymodel;
		setTitle(getLabel("mytasks", getEntity().getDisplayName()));
	}
	
	public EntityMember getEntity() {
		return entitymodel.getObject();
	}

	@Override
	protected ResultSet getContents() {
		KbeeUser us = (KbeeUser) getSessionUser();
		return us.getService(UserDashboardService.class).getLibraryContents(getLibrary(), getEntity());
	}
	
	@Override
	protected void onClickAll() {
	   	LibraryQuery query = new LibraryQuery(getQueryIndex(), getLibrary());
		query.setAsParameter(getEntity());
		setResponsePage(new ContentBasePage(getLibraryModel(), query));			
	}
}