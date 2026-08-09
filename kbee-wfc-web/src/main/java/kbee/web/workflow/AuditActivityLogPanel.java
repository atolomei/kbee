package kbee.web.workflow;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.datetime.DateTimeService;
import com.novamens.event.LogEvent;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Activity;

//import kbee.util.logging.Logger;
import kbee.web.console.grid.DatePropertyColumn;

@SuppressWarnings("serial")
public class AuditActivityLogPanel extends ModelPanel<EFormData> {
	private static final long serialVersionUID = 1L;

//	static private Logger logger = Logger.getLogger(AuditActivityLogPanel.class.getName());
	
	private List<LogEvent> events;
	private IModel<Activity> activitymodel;
	
	public class EventsProvider extends SortableDataProvider<LogEvent, String> {
		public Iterator<LogEvent> iterator(long first, long count) {
			ArrayList<LogEvent> iteration = new ArrayList<LogEvent>();
			Iterator<LogEvent> iterator = getAuditTrail().listIterator((int)first);
			int i = 0;
			while (i++<count) {
				iteration.add(iterator.next());
			}
			return iteration.iterator();
		}	
		public IModel<LogEvent> model(LogEvent object) {
			return new ObjectModel<LogEvent>(object);
		}
		public long size() {
			return getAuditTrail().size();
		}
	}

	public AuditActivityLogPanel(String id, IModel<Activity> activitymodel, IModel<EFormData> datamodel) {
		super(id, datamodel);
		setOutputMarkupId(true);
		this.activitymodel = activitymodel;
	}
	
	public Activity getActivity() {
		return activitymodel.getObject();
	}
	
	public EForm getForm() {
		return getModelObject().getForm();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addTable();
	}
	
	@Override	
	public void onDetach() {
		super.onDetach();
		this.events = null;
	}
	
	private void addTable() {
		DataTable<LogEvent, String> table = new DataTable<LogEvent, String>("event", getColumns(), new EventsProvider(), 40);
		table.addTopToolbar(new AjaxFallbackHeadersToolbar<String>(table, (EventsProvider)table.getDataProvider()));
		WebMarkupContainer tablecontainer = new WebMarkupContainer("table-container");
		tablecontainer.add(table);
		add(tablecontainer);
	}
	
	@SuppressWarnings("unchecked")
	protected List<LogEvent> getAuditTrail() {
		if (events==null) {
			events = (List<LogEvent>)getContentDao().getAuditTrail(getActivity(), getForm());
			if (events==null) {
 				events = new ArrayList<LogEvent>();
			}	
		}
		return events;
	}
	
	protected List<IColumn<LogEvent, String>> getColumns() {
		
		List<IColumn<LogEvent, String>> columns = new ArrayList<IColumn<LogEvent, String>>();
		
		columns.add(new DatePropertyColumn<LogEvent, String>(getLabel("time"), "Time", getZoneId(), getSessionUser().getLocale(), false) {
			@Override
			public String getCssClass() {
				return "col-xs-4 col-lg-3 col-md-4";
			}
		});
		
		columns.add(new PropertyColumn<LogEvent, String>(getLabel("description"), "description") {
			@Override
			public String getCssClass() {
				return "col-xs-8 col-lg-9 col-md-8";
			}
			@Override
			public void populateItem(Item<ICellPopulator<LogEvent>> cellItem, String componentId, IModel<LogEvent> rowModel) {
				Label description = new Label(componentId, rowModel.getObject().getDescription());
				description.setEscapeModelStrings(false);
				cellItem.add(description);
			}
		});

		return columns;
	}
	
//	private ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
	
	private ZoneId getZoneId() {
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		User user = getSessionUser();
		String zid = service.getMapZoneIds().get(user.getTimeZone());
		if (zid==null)	zid=ZoneId.systemDefault().getId();
		return ZoneId.of(zid);
	}
	
	
}