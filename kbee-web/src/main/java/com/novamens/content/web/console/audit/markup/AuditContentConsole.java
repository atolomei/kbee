package com.novamens.content.web.console.audit.markup;

import java.util.ArrayList;
import java.util.List;

import com.novamens.kbee.wicket.markup.html.console.grid.*;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Source;
import com.novamens.content.model.ObjectId;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.UrlService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectID;
import com.novamens.dom.Proxy;
import com.novamens.event.LogEvent;
import com.novamens.indexer.java.LogIndexerService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.wicket.markup.html.console.data.DataViewPanel;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.logging.ContentEvent;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.AuditConsole;
import kbee.web.console.ExpandedPanel;
import kbee.web.console.NameColumnPanel;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.query.AuditFindContentQuery;
import kbee.web.workflow.ErrorPage;

import org.danekja.java.util.function.serializable.SerializableSupplier;


public abstract class AuditContentConsole extends AbstractFacetedConsole<LogEvent> implements AuditConsole {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AuditContentConsole.class.getName());


	private String console;
	List<GridColumn<SearchResult,String>> columns;
	
	
	public AuditContentConsole(Query query) {
		super("contentlog", query);
	}



	@Override
	protected String getIcon(IModel<LogEvent> model) {
		return null;
	}
	
	public void onDetach() {
		super.onDetach();
		columns=null;
	}
	
	/**
	 *
	 * DateTime
	 * User
	 * OId-version-ID (head)
	 * Title
	 * Event
	 * Action
	 * Description
	 *
	 * ------------------------
	 * isHead
	 * State of Version
	 * ------------------------
	 */
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {


		if (columns!=null)
			return columns;
		
		columns = new ArrayList<GridColumn<SearchResult,String>>();


		// Executed
		//

		SerializableSupplier<String> formatSupplier = () -> this.getBrowser().getPanel(GridPanel.class).getDateFormat();
		DateKbeeColumn<LogEvent> executedColumn = new DateKbeeColumn<LogEvent>("executed", getLabel("executedcolumn"), (obj)-> obj.getTime(), formatSupplier);
		columns.add(executedColumn);


		// Title
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
			public void populateItemExpanded(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
					try {
						Object object = resultmodel.getObject().getObject();
						if (object==null) {
							cellItem.add(new Label(componentId, "object is null"));
							return;
						}
						IModel<LogEvent> objectmodel = getModel((LogEvent)object);
						cellItem.add(new Label(componentId,objectmodel.getObject().getTitle()));
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
				return AuditContentConsole.this.getName() + super.getContextKey();
			}
		});

  		
		// User  
		//

		KbeePredicateGridColumn<LogEvent> userColumn = new KbeePredicateGridColumn<>("user", getLabel("usercolumn"), (obj) -> getUserColumnText(obj).getObject());
		userColumn.setContextKey(this.getName() + userColumn.getContextKey());
		columns.add(userColumn);


		// Action
		//

		KbeePredicateGridColumn<LogEvent> eventClassColumn = new KbeePredicateGridColumn<>("eventaction", getLabel("eventactioncolumn"), (obj) -> obj.getAction());
		eventClassColumn.setContextKey(this.getName() + eventClassColumn.getContextKey());
		columns.add(eventClassColumn);


		// Description  ----------------------------------------------------------
		//

		KbeePredicateGridColumn<LogEvent> descriptionColumn = new KbeePredicateGridColumn<>("description", getLabel("descriptioncolumn"), (obj) -> obj.getDescription());
		descriptionColumn.setContextKey(this.getName() + descriptionColumn.getContextKey());
		columns.add(descriptionColumn);


		// OId-Version-Id
		//
		KbeePredicateGridColumn<LogEvent> idColumn = new KbeePredicateGridColumn<>("id", new Model<String>("OID - version - ID"), (obj) -> getIdColumnText(obj).getObject());
		idColumn.setContextKey(this.getName() + idColumn.getContextKey());
		columns.add(idColumn);

		return columns;
	}

	
	public String getConsole() {
		return this.console;
	}

	public void setConsole(String console) {
		this.console=console;
	}



	protected void addListeners() {
		super.addListeners();
		add(new WicketEventListener<ClickEvent<LogEvent>>() {
			
			private static final long serialVersionUID = 1L;

			
			@Override
			public void onEvent(ClickEvent<LogEvent> ce) {
				
				try {
					
					LogEvent event = ce.getModelObject();
					
					if (event instanceof ContentEvent) {
						
						// String oid=String.valueOf(((ContentEvent)event).getContentOId());
						String id=((ContentEvent) event).getObjectId();
						
						if (id==null)
							throw new KbeeRuntimeException("Id is null");
						
						Content content = (Content) getContentDao().findObjectById(new ObjectId(id));
							
						if (content==null) 
							throw new KbeeRuntimeException("Content is null for id ->  " + (id!=null?id:"null"));
							
						String url = content.getService(UrlService.class).getUrl();
						setResponsePage(new RedirectPage(url));
						}
				} catch (Exception e) {
					logger.error(e);
					setResponsePage( new ApplicationErrorPage<LogEvent>(e));
				}
			}
		});
		
	}
	

	
	private IModel<String> getIdColumnText(LogEvent event) {
		try {

				if (event instanceof ContentEvent) {
					String oid=String.valueOf(((ContentEvent)event).getContentOId());
					String id=((ContentEvent) event).getObjectId();
					String vid;
					String arr[] = id.split("#");
					if (arr.length>1)
						vid=arr[1];
					else
						vid=id;
					String version=String.valueOf(((ContentEvent)event).getVersion());
					return new Model<String>(oid+" - v"+version+" - "+vid);
				}
				else
					return (new Model<String>(event.getClass().getSimpleName()));

		} catch (Exception e) {
			logger.error(e);
				return (new Model<String>(e.getClass().getSimpleName()));
		}
	}


	private IModel<String> getUserColumnText(LogEvent event) {
		if (event==null || event==null) {
				return new Model<String>("err");
		}

		if (event.getEventUser()==null) {
			return new Model<String>("user null");
		}

		if (event.getEventUser().getFirstLastName()==null) {
			return new Model<String>("user name is null");
		}
		try {
			return new Model<String>(event.getEventUser().getFirstLastName());
		} catch (Throwable e) {
			logger.error(e);
				return new Model<String>(e.getClass().getSimpleName());
		}
	}

	@Override
	protected Index getQueryIndex() {
		return getDomain().getService(LogIndexerService.class).getIndex();
	}
	
	@Override
	protected Panel getTopPanel() {
		return new  AuditContentAdvancedSearchPanel("top");
	}
	
	@Override
	public Query newQuery() {
		return new AuditFindContentQuery();
	}
	
	@Override
	protected boolean isSelectionEnabled() {
		return false;
	}

	@Override
	protected boolean hasExpander() {
		return true;
	}

	@Override
	protected Panel getMenu(IModel<LogEvent> model) {
		return null;
	}
	
	
	
	@Override
	protected boolean hasTopPanel() {
		return true;
	}
	
	@Override
	protected Panel getPanel(IModel<LogEvent> model) {
		return getPanel(model, null);
	}
	
	
	@Override
	protected Panel getPanel(IModel<LogEvent> model, List<String> snippets) {
						
		if (model!=null && model.getObject() instanceof ContentEvent) {

			try {
				String coid=((ContentEvent) model.getObject()).getObjectId();
				String arr[]= coid.split("#");
	
				if (arr[1]!=null) {
					Content content = getContentDao().findContentById(Long.valueOf(arr[1]));	
					if (content!=null) {
						IModel<Content> mo=new ObjectModel<Content>(content);
						String bean = getContentClass(content) + "-panel";

						@SuppressWarnings("unchecked")
						ViewMode view_mode = ((DataViewPanel<LogEvent>) getBrowser().getPanel(DataViewPanel.class)).getViewMode();
						try {
							
							/**
							 * 
							 * IDocHitExpandedPanel
							 * TextHitExpandedPanel
							 * TreeIDocHitExpandedPanel
							 * 
							 */
							String query = (String)getQuery().getParameters().get("text");
							return ((Panel) ServiceLocator.getService(BeansService.class).getBean(bean, mo, view_mode, false, query, snippets));
						} 
						catch (Exception e) {
							logger.error("Can not resolve Spring bean " + bean);
							logger.error(e);
							return new InvisiblePanel("editor");
						}
					}
					else {
						return new ExpandedPanel<LogEvent>("editor", this, model, snippets);
					}
				}
			} catch (Exception e) {
				logger.error(e);				
			}
		}
		return new ExpandedPanel<LogEvent>("editor", this, model, snippets);
	}
	
	protected String getContentClass(Content content) {
		return Proxy.getClassName(content).toLowerCase();
	}
	
	@Override
	protected boolean isMyListsEnabled() {
		return false;
	}
	
	@Override
	protected boolean isFiltersEnabled() {
		return false;
	}
	
	protected boolean isDefaultTopPanelVisible() {
		return true;
	}
	

	@Override
	 protected  IModel<LogEvent> getModel(LogEvent object) {
			return new ObjectModel<LogEvent>(object, true);
	}
	


}


		