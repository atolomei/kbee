package com.novamens.content.web.console.markup;

import java.util.ArrayList;
import java.util.List;

import com.novamens.kbee.wicket.markup.html.console.grid.*;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Source;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ObjectId;
import com.novamens.content.web.console.markup.searchselector.AuditAdvancedSearchSelectorPanel;
import com.novamens.event.LogEvent;
import com.novamens.indexer.java.LogIndexerService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.service.Index;

import com.novamens.logging.ContentEvent;
import com.novamens.logging.DataSetValueEvent;
import com.novamens.logging.EmptyRecycleBinEvent;
import com.novamens.logging.ModelEvent;
import com.novamens.logging.SecurityEvent;
import com.novamens.logging.WorkNoteEvent;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.AuditConsole;
import kbee.web.console.ExpandedPanel;
import kbee.web.console.NameColumnPanel;
import kbee.web.query.SystemLogQuery;

import org.danekja.java.util.function.serializable.SerializableSupplier;


public abstract class AuditActivityConsole extends AbstractFacetedConsole<LogEvent>  implements AuditConsole {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AuditActivityConsole.class.getName());


	private String console;
	List<GridColumn<SearchResult,String>> columns;
	
	public AuditActivityConsole(Query query) {
		super("systemlog", query);
		setOutputMarkupId(true);
		setConsole(getName());
	}
	
	protected boolean isDefaultTopPanelVisible() {
		return true;
	}
	

	

	@Override
	protected String getIcon(IModel<LogEvent> model) {
		return null;
	}
	
	public void onDetach() {
		super.onDetach();
		if (columns!=null)
			columns=null;
	}
	
	@Override
	 protected  IModel<LogEvent > getModel(LogEvent object) {
			return new ObjectModel<LogEvent >(object, true);
	}

	
	public String getConsole() {
		return this.console;
	}
	
	public void setConsole(String console) {
		this.console=console;
	}
	
	
	
	
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		
		if (columns!=null)
			return columns;
		
		columns = new ArrayList<GridColumn<SearchResult,String>>();
		
		//  Title
		//

		columns.add(new GridColumn<SearchResult, String>("title", getLabel("titlecolumn"), "title_sort") {
 			private static final long serialVersionUID = 1L;
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				
					try {
						Object object = resultmodel.getObject().getObject();
						
						if (object==null) {
							cellItem.add(new Label(componentId, "object is null"));
							// this means the log should be reindexed or cleaned.
							return;
						}
						
						IModel<LogEvent> objectmodel = getModel((LogEvent)object);
						cellItem.add(new NameColumnPanel<LogEvent>(componentId,objectmodel) {
		 					private static final long serialVersionUID = 1L;
							@Override
							protected String getCss() {
								return "cell-label btn-link";
							}
							protected String getDisplayProperty() {
								return "title";
							}
						});
					} catch(Exception e) {
						logger.error(e);
						cellItem.add(new Label(componentId, e.getClass().getName())); 
					}
			}

			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				if (object.getObject() == null)
					return new Model<String>("err");
				try {
					String strValue = ((LogEvent) object.getObject()).getTitle();
					return new Model<>(strValue);
				} catch (Exception e) {
					return new Model<String>(e.getClass().getSimpleName());
				}
			}

			@Override
			public String getCssClass() {
				return "col title col-xs-1 col-md-1 col-lg-1";
			}
			@Override
			protected String getContextKey() {
				return AuditActivityConsole.this.getName() + super.getContextKey();
			}
		});

		
		//  Id
		//

		KbeePredicateGridColumn<LogEvent> idColumn = new KbeePredicateGridColumn<>("id", getLabel("idcolumn"), (obj) -> getIdColumnText(obj).getObject());
		idColumn.setContextKey(this.getName() + idColumn.getContextKey());
		columns.add(idColumn);



		//  Event
		//

		KbeePredicateGridColumn<LogEvent> eventColumn = new KbeePredicateGridColumn<>("event", getLabel("eventtypecolumn"), (obj) -> obj.getType());
		eventColumn.setContextKey(this.getName() + eventColumn.getContextKey());
		columns.add(eventColumn);


  		// 	ObjectClass
  		//

		KbeePredicateGridColumn<LogEvent> objectClassColumn = new KbeePredicateGridColumn<>("objectclass", getLabel("objectclasscolumn"), (obj) -> obj.getObjectClass());
		objectClassColumn.setContextKey(this.getName() + objectClassColumn.getContextKey());
		columns.add(objectClassColumn);


		//  Action
		//

		KbeePredicateGridColumn<LogEvent> eventClassColumn = new KbeePredicateGridColumn<>("eventaction", getLabel("eventactioncolumn"), (obj) -> obj.getAction());
		eventClassColumn.setContextKey(this.getName() + eventClassColumn.getContextKey());
		columns.add(eventClassColumn);
  		
		// Executed
		//


		SerializableSupplier<String> formatSupplier = () -> this.getBrowser().getPanel(GridPanel.class).getDateFormat();
		DateKbeeColumn<LogEvent> executedColumn = new DateKbeeColumn<LogEvent>("executed", getLabel("executedcolumn"), (obj)-> obj.getTime(), formatSupplier);
		columns.add(executedColumn);


		//  User
		//

		KbeePredicateGridColumn<LogEvent> userColumn = new KbeePredicateGridColumn<>("user", new Model<String>("Person name"), (obj) -> getUserColumnText(obj).getObject());
		userColumn.setContextKey(this.getName() + userColumn.getContextKey());
		columns.add(userColumn);

		//getLabel("usercolumn")

		// 	Description
		//

		KbeePredicateGridColumn<LogEvent> descriptionColumn = new KbeePredicateGridColumn<>("description", getLabel("descriptioncolumn"), (obj) -> obj.getDescription());
		descriptionColumn.setContextKey(this.getName() + descriptionColumn.getContextKey());
		columns.add(descriptionColumn);
		
		
		//
		// GERMAN AGREGAR COLUMNA PARA LINK AL RECURSO
		//
		// LogEvent ll;
		// Serializable ii=ll.getAuditResourceKBFileId();
		//
		// getContentDao().getResource()
		//
		//

		return columns;
	}
	
	


	private IModel<String> getUserColumnText(LogEvent logEvent) {
		if (logEvent.getEventUser()==null) {
			return new Model<String>("user null");
		}

		if (logEvent.getEventUser().getFirstLastName()==null) {
			return new Model<String>("user name is null");
		}

		try {
			return new Model<String>(logEvent.getEventUser().getFirstLastName());
		} catch (Throwable e) {
				logger.error(e);
				return new Model<String>(e.getClass().getSimpleName());
		}
	}

	@Override
	protected Panel getMenu(IModel<LogEvent> model) {
		return null;
	}
	
	@Override
	protected Panel getTopPanel() {
		return new  AuditAdvancedSearchSelectorPanel("top");
	}
	
	@Override
	protected Index getQueryIndex() {
		return getDomain().getService(LogIndexerService.class).getIndex();
	}
	
	@Override
	public Query newQuery() {
		return new SystemLogQuery();
	}

	protected Panel getPanel(IModel<LogEvent> model) {
		return new ExpandedPanel<LogEvent>("editor", this, model);
	}
	
	protected Panel getPanel(IModel<LogEvent> model, List<String> snippets) {
		return new ExpandedPanel<LogEvent>("editor", this, model, snippets);
	}
	
	@Override
	protected boolean isSelectionEnabled() {
		return false;
	}
	
	@Override
	protected boolean hasTopPanel() {
		return true;
	}

	@Override
	protected boolean hasExpander() {
		return true;
	}
	
	//protected abstract Page getConsolePage(Query query, long index);
	

	private IModel<String> getIdColumnText(LogEvent event) {
		if (event instanceof ContentEvent) {
			String clazz = ((ContentEvent) event).getKbeeClass();
			return new Model<String>( clazz+ " - "  + String.valueOf(((ContentEvent)event).getContentOId()) +"/"+String.valueOf(((ContentEvent)event).getVersion()));
		}

		// Security (User, Rule, Group)
		//
		else if (event instanceof SecurityEvent) {
				String clazz = ((SecurityEvent) event).getKbeeClass();
				try {
					ObjectId oid = (new ObjectId(((SecurityEvent)event).getObjectId()));
					String id = oid.getId();
					return new Model<String>(clazz + " - "  + id);

				} catch( Throwable e) {
					return (new Model<String>(event.getClass().getSimpleName()));
				}
		}

		// Empty Recycle Bin
		//
		else if (event instanceof EmptyRecycleBinEvent) {
			String clazz = ((EmptyRecycleBinEvent) event).getKbeeClass();
			return new Model<String>(clazz + " - "  + String.valueOf(((EmptyRecycleBinEvent)event).getEventUser().getId()));
		}

		// Model (DataSet, Classifier, Content Class)
		//
		else if (event instanceof ModelEvent) {
			String clazz = ((ModelEvent) event).getKbeeClass();
			ObjectId oid = (new ObjectId(((ModelEvent)event).getObjectId()));
			String id = oid.getId();
			return new Model<String>(clazz + " - "  + id);
		}

		// DataSetValue
		//
		else if (event instanceof DataSetValueEvent) {
			String clazz = (( DataSetValueEvent) event).getKbeeClass();
			ObjectId oid = (new ObjectId((( DataSetValueEvent)event).getObjectId()));
			String id = oid.getId();
			return new Model<String>(clazz + " - "  + id);
		}
		else if (event instanceof WorkNoteEvent) {
			String clazz = ((WorkNoteEvent) event).getKbeeClass();
			ObjectId oid = (new ObjectId(((WorkNoteEvent)event).getObjectId()));
			String id = oid.getId();
			return new Model<String>(clazz + " - "  + id);
		}
		else
			return (new Model<String>(event.getClass().getSimpleName()));
	}
	
	@Override
	protected boolean isMyListsEnabled() {
		return false;
	}
	
	@Override
	protected boolean isFiltersEnabled() {
		return false;
	}

	

	

}
