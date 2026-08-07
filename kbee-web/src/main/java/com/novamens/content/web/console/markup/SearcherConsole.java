package com.novamens.content.web.console.markup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.model.Classifier;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.wicket.markup.html.repeater.util.NavigationOrder;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.grid.ClassifierColumn;
import kbee.web.query.SearcherQuery;

@SuppressWarnings("serial")
public abstract class SearcherConsole extends AbstractFacetedConsole<Content> {
	private static final long serialVersionUID = 1L;

	static private Logger logger = LogManager.getLogger(SearcherConsole.class.getName());

	private List<NavigationOrder> orders = null;
	private List<GridColumn<SearchResult,String>> columns = null;

	public SearcherConsole(String id, Query query) {
		super(id, "searcher", query);
	}
	
	public SearcherConsole(Query query) {
		super("searcher", query);
	}
	
	@Override
	protected String getIcon(IModel<Content> model) {
		return null;
	}

	
	@Override
	public List<NavigationOrder> getOrders() {
		if (this.orders!=null) 
			return this.orders;
		this.orders = super.getOrders();
		Collections.sort(orders, new Comparator<NavigationOrder>() {
			public int compare(NavigationOrder order1, NavigationOrder order2) {
				try {
					return order1.getLabel().compareToIgnoreCase(order2.getLabel());
				} catch (Exception e) {
					logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
					return 0;
				}
			}
		});	
		return this.orders;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		for (GridColumn<?,?> column: getColumns()) 
			column.detach();
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		 add(new SearcherForm("form", "search") {
				public void onSearch(AjaxRequestTarget target, String text) {
					getQuery().getParameters().put("text", text);
					getQuery().getParameters().put("sort", "relevance");
					refresh(target);
				}
		 });
	}
	
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null) 
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();
	
		this.columns.add(new GridColumn<SearchResult, String>("title", getLabel("titlecolumn"), "title_sort") {
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				Object object = resultmodel.getObject().getObject();
				IModel<Content> objectmodel = getModel((Content)object);
				cellItem.add(new SelectionTitleColumnPanel<Content>(componentId, objectmodel) {
					@Override
					protected void onClick(AjaxRequestTarget target, IModel<Content> model) {
						SearcherConsole.this.onClick(target, model);
					}
					@Override
					protected String getCss() {
						return "btn-link";
					}
				});
			}
			@Override
			public String getCssClass() {
				return "col title col-xs-1 col-md-1 col-lg-1";
			}
			@Override
			protected String getContextKey() {
				return SearcherConsole.this.getName() + super.getContextKey();
			}
		});
		
		for (Classifier classifier : getClassifiers()) {
			if (classifier.isMetadataSubtitle() && classifier.getState()==ObjectState.ENABLED) 
				columns.add(new ClassifierColumn(new ObjectModel<Classifier>(classifier), this.getName()));
		}
		
		this.columns.add(new LastModifiedColumn<Content>("date", getLabel("datecolumn"), "modified") {
			private static final long serialVersionUID = 1L;
			@Override
			protected String getContextKey() {
				return SearcherConsole.this.getName() + super.getContextKey();
			}
		});
		
		if (getDomain().getDomainType()!=DomainType.EXPRESS) { 
			this.columns.add(new GridColumn<SearchResult, String>("contentclass", getLabel("contentclasscolumn")) {
				@Override
				protected IModel<String> getLabelModel(SearchResult object) {
					try {
						return new Model<String>(((Content)object.getObject()).getContentTemplate().getDisplayName());
					} 
					catch (Exception e) {
						logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
						return new Model<String>( e.getClass().getSimpleName());
					}
				}
				@Override
				protected String getContextKey() {
					return SearcherConsole.this.getName() + super.getContextKey();
				}
				
			});
		}
	
		return this.columns;
	}
	
	protected void onClick(AjaxRequestTarget target, IModel<Content> model) {
		
	}
	
	protected List<Classifier> getClassifiers() {
		return getContentDao().getClassifiers(getDomain());
	}
		
	@Override
	public Query newQuery() {
		return setUserPreference(new SearcherQuery(getQueryIndex()));
	}

	protected Panel getMenu(IModel<Content> model) {
		return null;
	}
	
	protected boolean isSelectionEnabled() {
		return false;
	}
	
	protected boolean isMenuEnabled() {
		return false;
	}
}
