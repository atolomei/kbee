package kbee.web.nav;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.value.IValueMap;

import com.novamens.indexer.query.Cursor;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingAjaxLink;
import com.novamens.kbee.wicket.markup.html.console.browser.AbstractListBrowser;
import com.novamens.kbee.wicket.markup.html.event.ClickBackEvent;
import com.novamens.kbee.wicket.markup.html.event.GeneralWicketEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.Identifiable;
import com.novamens.solr.indexer.query.SolrCursor;
import com.novamens.wicket.markup.html.actions.LinkMenuItemPanel;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.markup.html.repeater.util.NavigationOrder;
import com.novamens.wicket.markup.html.repeater.util.Searcher;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.model.SerializableModel;

import kbee.util.logging.Logger;
import kbee.web.console.SolrSearcherNavigator;
import kbee.web.cursor.CursorListModel;
import kbee.web.panel.ClickItemEvent;
import kbee.web.query.ListModelQuery;

/**
 *  T must be Serializable or sublcass of AbstractObject because we are using ObjectModel
 */
public class NavigatorPanelV6<T> extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(NavigatorPanelV6.class.getName());

	static private final int MAX_LENGTH = 80;
	
	private Cursor cursor;
	private Navigator<T> navigator;
    private Class<T> type;
	private WebMarkupContainer resultSetPanel = null;
	private WebMarkupContainer cursorContainer = null;
	
	private boolean isResultsPanel = false;
	
	
	public NavigatorPanelV6(String id, Navigator<T> navigator, Class<T> type) {
		this(id, navigator);
		this.type = type;
	}
	
	public NavigatorPanelV6(String id, Navigator<T> navigator) {
		super(id);
		if (navigator!=null) {
			setNavigator(navigator);
			setCursor(navigator.getCursor());
			if (getCursor()!=null)
				getCursor().setIndex(navigator.getIndex());
		}
	}

	public NavigatorPanelV6(String id, Searcher searcher, long index) {
		super(id);
		if (searcher!=null) {
			setCursor(searcher.getResultSet().getCursor());
			if (getCursor()!=null)
				getCursor().setIndex(index);
		}
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		setOutputMarkupId(true);
		
		add(newBackLink());
		add(newPreviousLink());
		add(newNextLink());
		cursorContainer = new WebMarkupContainer("cursorContainer");
		cursorContainer.setOutputMarkupId(true);
		cursorContainer.add(new InvisiblePanel("resultSetPanel"));
		add(cursorContainer);
		cursorContainer.setVisible(getCursor()!=null);
		if (getCursor()!=null) {
			Label current = new Label("current", String.valueOf(getIndex()+1));
			cursorContainer.add(current);
			Label total = new Label("total", String.valueOf(getCursor().size()));
			cursorContainer.add(total);
			AjaxLink<Void> rsp= new AjaxLink<Void>("resultset-link") {
				private static final long serialVersionUID = 1L;
				@Override
				public boolean isVisible() {
					return isResultsPanel;
				}
				@Override
				public void onClick(AjaxRequestTarget target) {
					WebMarkupContainer panel=getResultSetPanel();
					if (panel instanceof InvisiblePanel)
						return;
					panel.setVisible( !panel.isVisible());
					cursorContainer.addOrReplace(panel);	
					target.add(cursorContainer);
				}
			};
			cursorContainer.add(rsp);
		}
	}

	public boolean isResultsPanel() {
		return isResultsPanel;
	}
	
	public void setResultsPanel( boolean b) {
		isResultsPanel=b;
	}

	
	public void setResultSetPanel(WebMarkupContainer panel) {
		if (!panel.getId().equals("resultSetPanel"))
			throw new IllegalArgumentException("panel id must be = 'resultSetPanel'");
		this.resultSetPanel=panel;
	}
	
	public WebMarkupContainer getResultSetPanel() {
		
		if (resultSetPanel!=null)
			return resultSetPanel;
		
		WebMarkupContainer c = newBrowser();
		
		if (c==null) {
			c=new InvisiblePanel("resultSetPanel");
		}
		else
			c.setVisible(false);
		
		resultSetPanel = c;
		return resultSetPanel;
	}

	public Cursor getCursor() {
		return cursor;
	}
	
	public void setCursor(Cursor cursor) {
		this.cursor = cursor; 
	}
	
	public Navigator<T> getNavigator() {
		return navigator;
	}
	
	public void setNavigator(Navigator<T> navigator) {
		this.navigator = navigator; 
	}
	
	public long getIndex() {
		return  getCursor().getIndex();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (navigator!=null)
			navigator.detach();
	}
	
	protected IModel<T> getModel(T object) {
		if (object instanceof Serializable) {
			return new SerializableModel<T>(object);
		}
		if (! (object instanceof AbstractObject)) 
			logger.error("REQUIERES TO BE MAPPED BY HIBERNATE -> " + object.getClass().getName());
		// ------------
		// ObjectModel must beBE MAPPED BY HIBERNATE
		
		return new ObjectModel<T>(object);
	}
	
	
	protected Link<Void> newBackLink() {
		
		return new Link<Void>("back-link") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onClick() {
				fire ( new ClickBackEvent<T>() );
			}
			
			@Override
			public boolean isEnabled() {
				return true;
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
		};
	}
	
	protected AjaxLink<?> newPreviousLink() {
		return new WorkingAjaxLink<T>("previous-link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				navigatePrevious(target);
			}
			@Override
			public boolean isEnabled() {
				return getCursor()!=null && getCursor().getIndex() > 0;
			}
			
			@Override
			public boolean isVisible() {
				return getCursor()!=null;
			}
			
			@Override
			public String getBeforeClick() {
				return "if (typeof submit === \"function\") { submit(); }";
			}
			@Override
			protected void onComponentTag(final ComponentTag tag) {
				super.onComponentTag(tag);
				IValueMap attributes = tag.getAttributes();
				if (isVisible() && isEnabled()) {
					String label = getLabel(getCursor().get(getCursor().getIndex()-1));
					String s=String.valueOf(1+getCursor().getIndex()-1) + "/" + 	String.valueOf(getCursor().size());
					attributes.put("title", s + " - " + label);
				}
				else {
					attributes.put("title", "[no more items]");
				}
			}
		};
	}
	
	protected AjaxLink<?> newNextLink() {
		return new WorkingAjaxLink<T>("next-link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				navigateNext(target);
			}
			
			@Override
			public boolean isEnabled() {
				return getCursor()!=null && getCursor().hasMoreElements();
			}

			@Override
			public boolean isVisible() {
				return getCursor()!=null;
			}
			@Override
			public String getBeforeClick() {
				return "if (typeof submit === \"function\") { submit(); }";
			}
			@Override
			protected void onComponentTag(final ComponentTag tag) {
				super.onComponentTag(tag);
				IValueMap attributes = tag.getAttributes();
				if (isEnabled()) {
					String label = getLabel(getCursor().get(getCursor().getIndex()+1));
					String s=String.valueOf(1+getCursor().getIndex()+1) + "/" + 	String.valueOf(getCursor().size());
					attributes.put("title", s + " - " + label);
				}
				else {
					attributes.put("title", "[no more items]");
				}
			}
		};
	}
	
	
	@Override
	public void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<GeneralWicketEvent>() {
			private static final long serialVersionUID = 1L;
			
			public boolean handles(Class<GeneralWicketEvent> claz) {
				if (claz.getName()==null)
					return false;
				return (claz.getName().equals("navigate-next") || 
						claz.getName().equals("navigate-previous"));
			}
			
			@Override
			public void onEvent(GeneralWicketEvent event) {
					if (event.getName().equals("navigate-next")) {
						navigateNext();
					}
					else if (event.getName().equals("navigate-previous")) {
						navigatePrevious();
					}
			}});
		}
	
	public void navigateNext() {
		navigateNext(null);
	}
	
	@SuppressWarnings("unchecked")
	public void navigateNext(AjaxRequestTarget target) {
		
		if (getCursor()==null)
			return;
		
		SearchResult result = getCursor().hasMoreElements() ? getCursor().next() : null;
		
		try {
			if (result!=null && result.getObject()!=null) {
				if (type!=null && !type.isInstance(result.getObject())) {
					navigateNext(target);
				}
				else {
					//fire (new HomeNavigationEvent<T>(target));
					fire (new CursorNavigationEvent<T>(target, NavigatorPanelV6.this.getModel( (T) result.getObject() ) ));
				}	
			}
		}
		catch (Exception e) {
			logger.error(e);
			navigateNext(target);
		}
	}
	
	public void navigatePrevious() {
		navigatePrevious(null);
	}
	
	public void navigateBack() {
	}
	
	@SuppressWarnings("unchecked")
	public void navigatePrevious(AjaxRequestTarget target) {
		if (getCursor()==null)
			return;
		SearchResult result = getCursor().previous();
		if (result!=null && result.getObject()!=null) {
			fire (new CursorNavigationEvent<T>(target, NavigatorPanelV6.this.getModel( (T) result.getObject() ) ));
		}
	}
	
	protected void onNavigate(T object) {
	}
	
	protected String getLabel(SearchResult result) {
		if (result==null) return "-";
		String label = DisplayNameExtractor.get(result.getObject());
		if (label!=null && label.length()>MAX_LENGTH) 
			label = label.substring(0, MAX_LENGTH-3)+"...";
		return label;
	}

	
	@SuppressWarnings("unchecked")
	protected WebMarkupContainer newBrowser() {

		Query query = null;
		Searcher seacher = null;
		
		if (getPage() instanceof NavigablePage<?>) {
			NavigablePage<T> page = ((NavigablePage<T>)getPage());
			Navigator<T> navigator = page.getNavigator();
			if (navigator!=null) {
				Cursor cursor=navigator.getCursor();
				
				if (cursor instanceof SolrCursor) {
					query = ((SolrCursor) cursor).getQuery();
					
					if ( navigator instanceof SolrSearcherNavigator)  
						seacher = ((SolrSearcherNavigator<T>) navigator).getSearcher();
					else
						seacher = new Searcher( query);
				}
				else {
					if (cursor instanceof CursorListModel) {
						query  =  new ListModelQuery<T>(((CursorListModel<T>) cursor).getList());
						seacher = new Searcher(query);
					}
				}
			}
		}
		
		if (query==null)
			return null;

		AbstractListBrowser<T> panel = new AbstractListBrowser<T>("resultSetPanel", "resultSetPanel", query) {
			private static final long serialVersionUID = 1L;
			@Override
			protected boolean isFiltersEnabled() {
				return false;
			}
			@Override
			public List<NavigationOrder> getOrders() {
				return NavigatorPanelV6.this.getOrders();
			}
			@Override
			protected IModel<T> getModel(T content) {
				return new ObjectModel<T>(content);
			}
			@Override
			protected Panel getTopPanel() {
				return new InvisiblePanel("top");
			}
			@Override
			protected boolean hasExpander() {
				return false;
			}
			@Override
			public boolean isMyListsEnabled() {
				return false;
			}
			@Override
			protected boolean isSelectionEnabled() {
				return false;
			}
			@Override
			protected boolean isMenuEnabled() {
				return false;
			}
			
			@Override
			protected boolean isVisible(Facet facet) {
				return false;
			}
			 
			@Override
			protected Panel getItemListPanel(IModel<T> model, int index) {
				
					LinkMenuItemPanel<T> link = new LinkMenuItemPanel<T>("item", model) {
						private static final long serialVersionUID = 1L;
						@Override
						public void onClick() throws Exception {
							 fire( new ClickItemEvent<T>( getModel(), getIndex()) );
						}
						@Override
						public String getLabel() {
							if ((model.getObject() instanceof Identifiable)) {
								return ((Identifiable) model.getObject()).getDisplayName();	
							}
							return model.getObject().toString();
						}
					};
					
					link.setIndex(index);
					return link;
			}
			@Override
			protected boolean isSettingsEnabled() {
				return false;
			}

			@Override
			protected Panel getPanel(IModel<T> model, List<String> snippets) {
				return null;
			}

			@Override
			protected Panel getPanel(IModel<T> model) {
				return null;
			}

			@Override
			protected Panel getMenu(IModel<T> model) {
				return null;
			}

			@Override
			protected String getDefaultUserPreference(String key) {
				return null;
			}
			@Override
			public boolean isRememberQuery() {
				return false;
			}
			@Override
			protected void onUpdateQuery(AjaxRequestTarget target) {
			}
			@Override
			protected String getIcon(IModel<T> model) {
				return NavigatorPanelV6.this.getIcon(model);
			}
			@Override
			protected boolean hasIcon(IModel<T> model) {
				return NavigatorPanelV6.this.hasIcon(model);
			}
		};
		
		panel.setSearcher(seacher);
		return panel;
		
	}

	protected boolean hasIcon(IModel<T> model) {
		// TODO AT
		return false;
	}

	protected String getIcon(IModel<T> model) {
		// TODO AT
		return "";
	}	

	protected List<NavigationOrder> getOrders() {
		List<NavigationOrder> orders = new ArrayList<NavigationOrder>();
		return orders;
	}
	/*
	 * private Class<T> getTypeOfT() {
	 * 
	 * Type mySuperclass = getClass().getGenericSuperclass();
	 * 
	 * boolean p = mySuperclass instanceof ParameterizedType;
	 * 
	 * mySuperclass = ((Class<?>) mySuperclass).getGenericSuperclass();
	 * 
	 * return ((Class) ((ParameterizedType) getClass()
	 * .getGenericSuperclass()).getActualTypeArguments()[0]);
	 * 
	 * }
	 */
}
