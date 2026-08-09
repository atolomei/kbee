package kbee.web.model.object;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
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
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;

// import com.novamens.content.web.workflow.markup.ResolutionModal;
// import com.novamens.content.web.workflow.markup.ResolutionPage;


import com.novamens.datetime.DateTimeService;
import com.novamens.event.LogEvent;
import com.novamens.kbee.security.KbeeUser;

import com.novamens.kbee.wicket.markup.html.behaviour.AjustableHeightBehavior;

import com.novamens.logging.TaskEndEvent;

import com.novamens.security.Identifiable;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.console.grid.DatePropertyColumn;
import kbee.web.workflow.ResolutionModal;
import kbee.web.workflow.ResolutionPage;

/**
 * <ul>
 *{@link User}
 *{@link Group}
 *{@link WorkflowRule}
 * -------------------------------------------------------
 *{@link DataSet}
 *{@link Classifier}
 *{@link Content Class}
 * -------------------------------------------------------
 *{@link Label}
 *{@link DataSetMember}
 * </ul>
 * -------------------------------------------------------
 *  
 *  {@link Role}
 *  
 * @param <T>
 */
@SuppressWarnings("serial")
public class AuditTrailObjectPanel<T> extends Panel {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AuditTrailObjectPanel.class.getName());

	static private final String SEPARATOR = ",";
	
	private IModel<T> model;
	private List<LogEvent> events;
	private boolean adjust_height = false;

	public class DescriptionFragment extends Fragment {
		IModel<LogEvent> model;
		public DescriptionFragment(String id, LogEvent event) {
			super(id, "description-fragment", AuditTrailObjectPanel.this);
			this.model = new ObjectModel<LogEvent>(event);
			String note = event.getDescription();
			final String resolution = event instanceof TaskEndEvent ? ((TaskEndEvent)event).getResolution() : null;
			add((new Label("note", note)).setEscapeModelStrings(false));
			add(new Link<Void>("resolution") {
				public void onClick() {
					setResponsePage(new ResolutionPage(model));
				}
				public boolean isVisible() {
					return resolution!=null && !"".equals(resolution.trim());
				}
			});
		};
		public void onDetach() {
			super.onDetach();
			this.model.detach();
		}
	};	

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
	
	public AuditTrailObjectPanel(String id) {
		this(id, null, false);
	}
	
	public AuditTrailObjectPanel(String id, IModel<T> model) {
			this(id, model, false);
	}

	public AuditTrailObjectPanel(String id, IModel<T> model, boolean badjusth) {
		super(id);
		setOutputMarkupId(true);
		setModel(model);
		this.adjust_height= badjusth;
	}
	
	public void setAdjustHeight(boolean adjusth) {
		this.adjust_height=adjusth;
	}
	
	public void setModel(IModel<T> model) {
		this.model = model;
	}

	public IModel<T> getModel() {
		return this.model;
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("audittrail-container")==null) {
			add(new ResolutionModal());
			addTable();
		}
	}
	
	@Override
	public void onDetach() {
		if (this.model!=null)
			this.model.detach();
		this.events = null;
		super.onDetach();

	}
	
	protected List<IColumn<LogEvent, String>> getColumns() {
	
		List<IColumn<LogEvent, String>> columns = new ArrayList<IColumn<LogEvent, String>>();
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		User user = getUser();
		String zid = service.getMapZoneIds().get(user.getTimeZone());
		if (zid==null)	zid=ZoneId.systemDefault().getId();

		columns.add(new DatePropertyColumn<LogEvent, String>(new StringResourceModel("time",this, null), "Time", ZoneId.of(zid), user.getLocale(), false) {
			@Override
			public String getCssClass() {
				return "col-xs-2";
			}
		});

		columns.add(new PropertyColumn<LogEvent, String>(new StringResourceModel("user",this, null), "user.firstLastName") {
			@Override
			public String getCssClass() {
				return "col-xs-2";
			}
		});
		
		columns.add(new PropertyColumn<LogEvent, String>(new StringResourceModel("action",this, null), "action") {
			@Override
			public String getCssClass() {
				return "col-xs-2";
			}
		});

		columns.add(new PropertyColumn<LogEvent, String>(new StringResourceModel("description", this, null), "description") {
			@Override
			public String getCssClass() {
				return "col-xs-6";
			}
			@Override
			public void detach() {
			}
			@Override
			public String getSortProperty() {
				return null;
			}
			@Override
			public boolean isSortable() {
				return false;
			}
			@Override
			public void populateItem(Item<ICellPopulator<LogEvent>> cellItem, String componentId, IModel<LogEvent> rowModel) {
				cellItem.add(new DescriptionFragment(componentId,  rowModel.getObject()));
			}
		});

		return columns;
	}
	
	protected List<LogEvent> getEvents() {
		return this.events;
	}
	
	protected void setEvents(List<LogEvent> list) {
		this.events=list;
	}

	@SuppressWarnings("unchecked")
	protected List<LogEvent> getAuditTrail() {
		if (events==null) {
			try {
				if (getModel().getObject()!=null) {
					/** 
					 *  if it is a content, it returns the audit of all previous versions.
					 	Normally Contents should not be here. 
					 **/
					if (getModel().getObject() instanceof Content)
						events = (List<LogEvent>)getContentDao().getAuditTrail((Content) getModel().getObject());
					else
						events = (List<LogEvent>)getContentDao().getAuditTrail(getModel().getObject());
				}
			} 
			catch (RuntimeException e) {
				logger.error(e);
			}
			if (events==null)
 				events = new ArrayList<LogEvent>();
		}
		return events;
	}
	
	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected File getDownloadFile() throws IOException {
		File file = File.createTempFile(getDisplayName().getObject() , ".csv");
		org.apache.wicket.util.file.Files.writeTo(file, getDownloadStream());
		return file;
	}

	protected IModel<String>  getDisplayName() {
		if (getModel().getObject() instanceof Identifiable) {
			if (((Identifiable) getModel().getObject()).getDisplayName()!=null)
			return new Model<String>(((Identifiable) getModel().getObject()).getDisplayName());
		}
		return new Model<String>(this.getClass().getSimpleName());
	}

	protected String getDownloadFileName() {
		String name;
		if (getModel().getObject() instanceof Identifiable) {
			name = ((Identifiable) getModel().getObject()).getDisplayName();
			
			if (name!=null)
				name=name.trim().replace(" ", "_").toLowerCase();
			else
				name=getDisplayName().getObject().toLowerCase().replace(" ", "-");
		}
		else
			name=getDisplayName().getObject().toLowerCase().replace(" ", "-");
		
		OffsetDateTime now = OffsetDateTime.now();
		
		String year = String.valueOf(now.getYear());
		String month = String.valueOf(now.getMonth());
		String day = String.valueOf(now.getDayOfMonth());
		
		return name + "-" + year + "-" + month + "-" + day +"-audit.csv";
	}
	
	protected InputStream getDownloadStream() {
		StringBuffer filebuffer = new StringBuffer();
		int c = 0;
		for (IColumn<LogEvent, String> column : getColumns()) {
			if (c++>0) 
				filebuffer.append(SEPARATOR);
			if (column instanceof PropertyColumn)
						filebuffer.append(((PropertyColumn<LogEvent, String>) column).getDisplayModel().getObject()  );
		}
		
		List<LogEvent> list = getAuditTrail(); 
		filebuffer.append("\r\n");
		
		for (LogEvent event: list) {
			c=0;
			for (IColumn<LogEvent, String> column : getColumns()) {
				if (c++>0) 
					filebuffer.append(SEPARATOR);
				
				if (column instanceof  DatePropertyColumn) {
				
					try {
						filebuffer.append((( DatePropertyColumn<LogEvent, String>) column).getCellAsString( new ObjectModel<LogEvent>(event)).getObject());
					} catch (Exception e) {
						filebuffer.append(e.getClass().getSimpleName() + " | " + e.getMessage());
					}
				}
				else if (column instanceof  PropertyColumn) { 
					try {
						PropertyColumn<LogEvent, String> col = (PropertyColumn<LogEvent, String>) column;
						IModel<?> m= col.getDataModel(new ObjectModel<LogEvent>(event));
						String str = (m!=null & m.getObject()!=null) ? m.getObject().toString() : "";

						if (str!=null) {
							str=str.replace(SEPARATOR,"");
							str=str.replace("\r\n"," \\ ");
							str=str.replace("\n"," - ");
							str=str.replace("\\s"," - ");
							str=str.replace("\r"," - ");
						}
							else
								str="";
						filebuffer.append(str);
					} catch (Exception e) {
						filebuffer.append(e.getClass().getSimpleName() + " | " + e.getMessage());
					}
				}
			}
			filebuffer.append("\r\n");
		}
		InputStream stream = new ByteArrayInputStream(filebuffer.toString().getBytes());
		return stream;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private void addTable() {
																							
		DataTable<LogEvent, String> table = new DataTable<LogEvent, String>("activity", getColumns(), new EventsProvider(), 40);
		
		table.addTopToolbar(new AjaxFallbackHeadersToolbar<String>(table, (EventsProvider)table.getDataProvider()));
		WebMarkupContainer container = new WebMarkupContainer("audittrail-container");
		
		WebMarkupContainer tablecontainer = new WebMarkupContainer("table-container");
		if (this.adjust_height) {
			container.add(new AjustableHeightBehavior(180));
			tablecontainer.add(new AjustableHeightBehavior(170));
		}
		tablecontainer.add(table);
		container.add(tablecontainer);
		container.add(new com.novamens.wicket.markup.html.repeater.util.NavigationToolbar("navigation", table, true) {
			protected String getDownloadFilename() {
				return getDownloadFileName();
			}
			@Override
			protected File getFile() {
				try {
					return getDownloadFile();
				} catch (Exception e) {
					logger.error(e);
					return null;
				}
			};
		});
		add(container);
	}
}
