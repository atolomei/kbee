package kbee.web.security.user;


import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.datetime.DateTimeService;
import com.novamens.event.LogEvent;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.console.grid.DatePropertyColumn;


/**
 * 
 * 
 * THis is for Principal
 * 
 *  * THIS IS NOT USED



 * Ver si esta en uso ????
 *
 * @param <T>
 */

@Deprecated
public class AuditTrailPanel<T extends Principal> extends Panel {
				
	private static final long serialVersionUID = 1L;
	
	static Logger logger = LogManager.getLogger(AuditTrailPanel.class.getName());

	private IModel<T> model;
	private List<LogEvent> auditTrail;
	
	public class EventsProvider extends SortableDataProvider<LogEvent, String> {
		private static final long serialVersionUID = 1L;
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

	public AuditTrailPanel(String id, IModel<T> model) {
		this(id, model,0);
	}
	
	public AuditTrailPanel(String id, IModel<T> model, int windowHeight) {
		super(id);
		
		
		
		this.model = model;

		DataTable<LogEvent, String> table = new AjaxFallbackDefaultDataTable<LogEvent, String>("events", getColumns(), new EventsProvider(), 16);

		WebMarkupContainer container = new WebMarkupContainer("audittrail-container");
		add(container);
		
		container.add(table);
		
		AjaxLink<Void> close= new AjaxLink<Void>("close") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				AuditTrailPanel.this.onClose(target);
			}
		};
		
		add(close);
		
		throw new RuntimeException("deprecated");
		
		
	}
	
	public void onClose(AjaxRequestTarget target) {
	}
	
	public List<IColumn<LogEvent, String>> getColumns() {
		
		List<IColumn<LogEvent, String>> columns = new ArrayList<IColumn<LogEvent, String>>();
		
		// 3
		//
		columns.add(new IColumn<LogEvent, String>() {
			private static final long serialVersionUID = 1L;
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
				String str;
				try {
					str = rowModel.getObject().getEventUser().getFirstLastName();
				} catch (Exception e) {
					str = "err";
				}
				cellItem.add((new Label(componentId, str)).setEscapeModelStrings(false));
				cellItem.add( new AttributeModifier("class", "col-xs-2"));
			}

			@Override
			public Component getHeader(String componentId) {
				Label label = new Label(componentId, "User"); 
				label.add( new AttributeModifier("class", "col-xs-2"));
				return label;
			}
		});


		// 2
		//
		columns.add(new IColumn<LogEvent, String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

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
				String str;
				try {
					str = rowModel.getObject().getEventType();
				} catch (Exception e) {
					str = "err";
				}
				cellItem.add((new Label(componentId, str)).setEscapeModelStrings(false));
				cellItem.add( new AttributeModifier("class", "col-xs-2"));
			}

			@Override
			public Component getHeader(String componentId) {
				Label label = new Label(componentId, "Event"); 
				label.add( new AttributeModifier("class", "col-xs-2"));
				return label;
			}
		});

		
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		User user = getUser();
		String zid = service.getMapZoneIds().get(user.getTimeZone());
		if (zid==null)
				zid=ZoneId.systemDefault().getId();

		// 2
		//
		columns.add(new DatePropertyColumn<LogEvent, String>(new Model<String>("Date"), "Time", ZoneId.of(zid), user.getLocale()));
		
		
		// 5
		//
		columns.add(new IColumn<LogEvent, String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

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
				String str =rowModel.getObject().getDescription();
				cellItem.add((new Label(componentId, str)).setEscapeModelStrings(false));
				cellItem.add( new AttributeModifier("class", "col-xs-4"));
			}

			@Override
			public Component getHeader(String componentId) {
				Label label = new Label(componentId, "Description"); 
				label.add( new AttributeModifier("class", "col-xs-4"));
				return label;
			}
		});
		return columns;
	}
	
	public IModel<T> getModel() {
		return model;
	}
	
	public void onDetach() {
		auditTrail = null;
		if (model!=null)
			model.detach();
		super.onDetach();
	}

	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	
	@SuppressWarnings("unchecked")
	private List<LogEvent> getAuditTrail() {
		if (auditTrail==null) {
			try {
				auditTrail = (List<LogEvent>)getContentDao().getAuditTrail((User)model.getObject());
			} catch (RuntimeException e) {
				logger.error(e);
			}
 			
			if (auditTrail==null)
 				auditTrail = new ArrayList<LogEvent>();
		}
		return auditTrail;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
