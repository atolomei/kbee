package kbee.web.dashboard;

import java.time.OffsetDateTime;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.model.EntityMember;
import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.ServiceLocator;

import kbee.web.content.console.MonitorPage;
import kbee.web.query.MonitorQuery;

@SuppressWarnings("serial")
public class DashboardEntityIndicatorsWidgetPanel extends DashboardWidgetBasePanel {
	private static final long serialVersionUID = 1L;

	private IModel<EntityMember> entitymodel;
	
	
	public DashboardEntityIndicatorsWidgetPanel(String id, IModel<EntityMember> entitymodel) {
		super(id, "roles");
		this.entitymodel = entitymodel;
		setTitle(new Model<String>("Indicadores"));
	}
	
	
	public EntityMember getEntity() {
		return entitymodel.getObject();
	}
	
	
	/** TODO */
	@Override
	protected void onClickCollapse(AjaxRequestTarget target) {
		//main_container.setVisible(!main_container.isVisible());
		refresh(target);
	}
	@Override
	protected void onHelp(AjaxRequestTarget target) {
		refresh(target);
	}

	
	@Override
	protected void onTitleClick() {
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		Link<Void> taskslink = new Link<Void>("tasks-link") {
			public void onClick() {
				MonitorQuery query = new MonitorQuery(getQueryIndex());
				query.setAsParameter(getEntity());
				setResponsePage(new MonitorPage(query));
			}
		};
		taskslink.add(new Label("tasks-total", new Model<String>() {
			public String getObject() {
				return String.valueOf(getTotalTasks());
			}
		}));
		add(taskslink);
		
		Label lastActivityLabel = new Label("last-activity-time", new Model<String>() {
			public String getObject() {
				return getLastActivityTime()!=null
					? ServiceLocator.getService(DateTimeService.class).timeElapsed(getLastActivityTime())
					: "-";
			}
		});
		lastActivityLabel.setEscapeModelStrings(false);
		add(lastActivityLabel);
		
		add(new Label("published-total", new Model<String>() {
			public String getObject() {
				return String.valueOf(getTotalPublished());
			}
		}));

		
//		add(new ListView<IModel<Library>>("library", () -> getLibraries()) {
//			public void populateItem(ListItem<IModel<Library>> item) {
//				Library library = item.getModelObject().getObject();
//				item.add(new Label("label", getLabel("library-label", library.getDisplayName())));
//				KbeeUser us = (KbeeUser) getSessionUser();
//				int total = us.getService(UserDashboardService.class).getLibraryContents(library, getEntity()).size();
//				AjaxLink<Void> link = new AjaxLink<Void>("link") {
//					public void onClick(AjaxRequestTarget target) {
//						
//					}
//				};
//				link.add(new Label("total", String.valueOf(total)));
//				item.add(link);
//			}
//		});
	}	
	
	protected int getTotalTasks() {
		KbeeUser us = (KbeeUser) getSessionUser();
		return us.getService(UserDashboardService.class).getMonitoredTasks(getEntity()).size();
	}
	
	protected OffsetDateTime getLastActivityTime() {
		KbeeUser us = (KbeeUser) getSessionUser();
		Content content =  us.getService(UserDashboardService.class).getLastTask(getEntity());
		return content!=null ? content.getLastModifiedOffsetDateTime() : null;
	}
	
	protected int getTotalPublished() {
		KbeeUser us = (KbeeUser) getSessionUser();
		return us.getService(UserDashboardService.class).getLibraryContents(getEntity()).size();
	}
//	
//	protected List<IModel<Library>> getLibraries() {
//		List<IModel<Library>> libraries = new ArrayList<IModel<Library>>();
//		for (Library cabinet : getRepository(Library.class).findAll()) {
//			if (cabinet.isReadable()) 
//				libraries.add( new ObjectModel<Library>(cabinet));
//		};
//		return libraries;
//	}
//	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
}