package kbee.web.security.user;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.datetime.DateTimeService;
import com.novamens.event.LogEvent;
import com.novamens.security.Identifiable;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.console.grid.DatePropertyColumn;

@SuppressWarnings("serial")
public class UserActivityPanel extends Panel {
				
	private static final long serialVersionUID = 1L;
	
	//static DateConverter converter = new PatternDateConverter("dd MMM yyyy hh:mm:ss z",false);
	
	static private Logger logger = LogManager.getLogger(UserActivityPanel.class.getName());

	
	private IModel<Person> model;
	private List<LogEvent> activity;
	private List<IColumn<LogEvent, String>> columns;
	
	public class EventsProvider extends SortableDataProvider<LogEvent, String> {
		public Iterator<LogEvent> iterator(long first, long count) {
			ArrayList<LogEvent> iteration = new ArrayList<LogEvent>();
			Iterator<LogEvent> iterator = getActivity().listIterator((int)first);
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
			return getActivity().size();
		}
	}

	public UserActivityPanel(String id, IModel<Person> model) {
		super(id);
		
		setModel(model);
		
	}
	
	 
	/**
	 * Lazy Initialization
	 */
	public void onBeforeRender() {
		super.onBeforeRender();

		if (get("activity")!=null)
			return;
		
		DataTable<LogEvent, String> table = new DataTable<LogEvent, String>("activity", getColumns(), new EventsProvider(), 30);
		table.addTopToolbar(new AjaxFallbackHeadersToolbar<String>(table, (EventsProvider)table.getDataProvider()));
		add(table);
		add(new com.novamens.wicket.markup.html.repeater.util.NavigationToolbar("navigation", table, true) {
			
			protected String getDownloadFilename() {
				return getDownloadFileName();
			}
			
			@Override
			protected File getFile() {
				try {
					return getDownloadFile();
				} catch (Exception e) {
					logger.error(e.getClass().getName(), e);
					return null;
				}
			};
			
		});
	}

	public void setModel(IModel<Person> model) {
		this.model = model;
	}
	
	public IModel<Person> getModel() {
		return this.model;
	}
	
	public User getUser() {
		return getModel().getObject().getProfile(UserProfile.class).getUser();
	}
	
	@Override
	public void onDetach() {
		this.model.detach();
		this.activity = null;
		this.columns=null;
		super.onDetach();
	}
	
	
	private List<IColumn<LogEvent, String>> getColumns() {
		
		if (columns!=null)
			return columns;
		
		this.columns = new ArrayList<IColumn<LogEvent, String>>();
		
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		User user = getUser();
		
		String zid = null;
		
		if (user.getTimeZone()!=null)
			zid = service.getMapZoneIds().get(user.getTimeZone());
		if (zid==null)
				zid=ZoneId.systemDefault().getId();

		
		this.columns.add(new DatePropertyColumn<LogEvent, String>(new StringResourceModel("date",this, null), "Time", ZoneId.of(zid), user.getLocale(), false) {
			@Override
			public String getCssClass() {
				return "col-xs-2 col-lg-1 col-md-1";
			}
		});
		
		this.columns.add(new PropertyColumn<LogEvent, String>(new StringResourceModel("action",this, null), "action") {
			@Override
			public String getCssClass() {
				return "col-xs-3 col-lg-2 col-md-2";
			}
		});
		
		
		this.columns.add(new PropertyColumn<LogEvent, String>(new StringResourceModel("object",this, null), "target") {
			@Override
			public String getCssClass() {
				return "col-xs-2 col-lg-2 col-md-2";
			}
		});
		
		
		this.columns.add(new PropertyColumn<LogEvent, String>(new StringResourceModel("title",this, null), "title") {
			
			@Override
			public void populateItem(Item<ICellPopulator<LogEvent>> cellItem, String componentId, IModel<LogEvent> rowModel) {
				try {
					String title = "";
					if (rowModel.getObject() instanceof LogEvent) {
						title=((LogEvent) rowModel.getObject()).getTitle();
					}
					cellItem.add((new Label(componentId, title)).setEscapeModelStrings(false));
					
				} catch (Exception e) {
					cellItem.add((new Label(componentId, e.getClass().getName())).setEscapeModelStrings(false));
				}
			}
			
			@Override
			public String getCssClass() {
				return "col-xs-3 col-lg-2 col-md-2";
			}
		});
		
		
		this.columns.add(new PropertyColumn<LogEvent, String>(new StringResourceModel("description",this, null), "description") {
			@Override
			public String getCssClass() {
				return "col-xs-3 col-lg-4 col-md-4";
			}
		});
		return this.columns;
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
		String month = String.valueOf(now.getMonth().getValue());
		String day = String.valueOf(now.getDayOfMonth());
		
		return name +"-activity-" + year + "-" + month + "-" + day +".csv";
	}
	
	
	static private final String SEPARATOR = ",";

	/**
	 * 
	 * 
	 */
	protected InputStream getDownloadStream() {
		StringBuffer filebuffer = new StringBuffer();
		int c = 0;
		for (IColumn<LogEvent, String> column : getColumns()) {
			if (c++>0) 
				filebuffer.append(SEPARATOR);
			if (column instanceof PropertyColumn)
						filebuffer.append(((PropertyColumn<LogEvent, String>) column).getDisplayModel().getObject()  );
		}
		
		List<LogEvent> list = getActivity(); 
		filebuffer.append("\r\n");
		
		for (LogEvent event: list) {
			c=0;
			for (IColumn<LogEvent, String> column : getColumns()) {
				if (c++>0) 
					filebuffer.append(SEPARATOR);
				if (column instanceof  DatePropertyColumn)
					filebuffer.append(((kbee.web.console.grid.DatePropertyColumn<LogEvent, String>) column).getCellAsString( new ObjectModel<LogEvent>(event)).getObject());
				else if (column instanceof  PropertyColumn) { 
			
					String str = null;
					IModel<LogEvent> mod=new ObjectModel<LogEvent>(event);

					if(((PropertyColumn<LogEvent, String>) column).getDataModel(mod)!=null) {
						Object o=((PropertyColumn<LogEvent, String>) column).getDataModel(mod).getObject();
						if(o!=null)
							str = o.toString();
					}
					
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
				}
			}
			filebuffer.append("\r\n");
		}
		InputStream stream = new ByteArrayInputStream(filebuffer.toString().getBytes());
		return stream;
	}

	protected File getDownloadFile() throws IOException {
		File file = File.createTempFile(getDisplayName().getObject() , ".csv");
		org.apache.wicket.util.file.Files.writeTo(file, getDownloadStream());
		return file;
	}
	
	
	
	@SuppressWarnings("unchecked")
	private List<LogEvent> getActivity() {
		if (this.activity==null) {
			try {
				if (getUser()!=null) {
						this.activity = (List<LogEvent>)getContentDao().getActivity(getUser());
				}
			} 
			catch (RuntimeException e) {
				logger.error(e.getClass().getName(), e);
			}
 			
			if (this.activity==null)
				this.activity = new ArrayList<LogEvent>();
		}
		return this.activity;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
