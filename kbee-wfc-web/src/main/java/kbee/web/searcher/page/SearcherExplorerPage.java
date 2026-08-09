package kbee.web.searcher.page;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.base.Content;
import com.novamens.content.userlist.UserList;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ValueFilter;
import com.novamens.kbee.portal.model.SearcherSiteQuery;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsApplyUserListEvent;
import com.novamens.kbee.wicket.markup.html.event.EditableListEvent;
import com.novamens.kbee.wicket.markup.html.event.ExplorerOpenEvent;
import com.novamens.kbee.wicket.markup.html.event.FilterSelectorClearAllEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.portal6.model.Site;
import com.novamens.wicket.markup.html.repeater.util.Searcher;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;
import kbee.web.console.SolrSearcherNavigator;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.searcher.panel.SearcherExplorerBrowser;
import kbee.web.searcher.searchform.SearcherOnChangeEvent;

/**
 * Explorer
 */
@SuppressWarnings("serial")
public class SearcherExplorerPage extends AbstractSearcherPage<Void> {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(AbstractSearcherPage.class.getName());

	private Query query;
	private Map<String, Object> parameters;
	
	
	public SearcherExplorerPage(PageParameters parameters) {
		Site site = getSite(parameters);
		setOutputMarkupId(true);
		if (site!=null) { 
			setSiteModel(new ObjectModel<Site>(site));
		}
	}

	public SearcherExplorerPage(IModel<Site> model, Map<String, Object> parameters) {
		setOutputMarkupId(true);
		setSiteModel(model);
		this.parameters = parameters;
		getPageParameters().set("siteurl", getSiteModel().getObject().getUrl().toString());
	}
	
	public SearcherExplorerPage(IModel<Site> model) {
		setOutputMarkupId(true);
		setSiteModel(model);
		getPageParameters().set("siteurl", getSiteModel().getObject().getUrl().toString());
	}
	
	@Override
	protected void addListeners() {
		super.addListeners();
	
		
		add(new WicketEventListener<FilterSelectorClearAllEvent>() {
			@Override
			public void onEvent(FilterSelectorClearAllEvent event) {
				try {
					SearcherExplorerPage s = new SearcherExplorerPage(getSiteModel(), null);
					setResponsePage(s);
					// FeedbackHelper.showInfoToast( "FilterSelectorClearAllEvent" ,   "FilterSelectorClearAllEvent" );
				}
				catch (Exception e) {
					setResponsePage(new ApplicationErrorPage<>(e));
					logger.error(e);
				}		
				
				 
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof FilterSelectorClearAllEvent;
			}
		});
		
		
		
		
		add(new WicketEventListener<SearcherOnChangeEvent>() {
			public void onEvent(SearcherOnChangeEvent event) {
				try {
					Map<String, Object> parameters = event.getParameters();
					parameters.put("writeables", isEditableOn() ? "true" : "false");
					SearcherSiteQuery sq = new SearcherSiteQuery(getSiteModel().getObject(), getIndex(), parameters);
					SearcherResultsPage s= new SearcherResultsPage(getSiteModel(), sq);
					setResponsePage(s);
				} 
				catch (Exception e) {
					logger.error(e);
				}		
			}
		});
		
		add(new WicketEventListener<EditableListEvent<Site>>() {
			public void onEvent(EditableListEvent<Site> event) {
				try {
					Map<String, Object> parameters = new HashMap<>();
					parameters.put("writeables", isEditableOn() ? "true" : "false");
					SearcherExplorerPage s = new SearcherExplorerPage(event.getModel(), parameters);
					setResponsePage(s);
				} 
				catch (Exception e) {
					setResponsePage(new ApplicationErrorPage<>(e));
					logger.error(e);
				}		
			}
		});
		
		add(new WicketEventListener<ClickEvent<?>>() {
			@Override
			public void onEvent(ClickEvent<?> event) {
				Object object = event.getModel().getObject();
				if (object instanceof Content) {
					@SuppressWarnings("unchecked")
					SearcherDetailDocumentPage<Content> page = new SearcherDetailDocumentPage<Content>((IModel<Content>) event.getModel(), getSiteModel());
					if (getQuery()!=null) {
						SolrSearcherNavigator<Content> na = new SolrSearcherNavigator<Content>( new Searcher(getQuery()), event.getIndex() );
						page.setNavigator(na);	
					}
					setResponsePage(page);
				}
			}
		});
		
		add(new WicketEventListener<ExplorerOpenEvent<Site>>() {
			public void onEvent(ExplorerOpenEvent<Site> event) {
				try {
					Map<String, Object> parameters = new HashMap<>();
					SearcherSiteQuery sq = new SearcherSiteQuery(getSiteModel().getObject(), getIndex(), parameters);
					setResponsePage(new SearcherResultsPage(getSiteModel(), sq));					
				} 
				catch (Exception e) {
					setResponsePage(new ApplicationErrorPage<>(e));
					logger.error(e);
				}		
			}
		});
		
		add(new WicketEventListener<MyListsApplyUserListEvent>() {
			@Override
			public void onEvent(MyListsApplyUserListEvent event) {
				IModel<UserList> list = event.getUserList();
				ValueFilter filter = new ValueFilter("userlist", String.valueOf(list.getObject().getId()), list.getObject().getDisplayName());
				Map<String, Object> parameters = new HashMap<>();
				String usereditable = getUserPreference("user-editable", "no");
				if (usereditable.equals("yes") || usereditable.equals("true")) {
					parameters.put("writeables", "true");
				}
				else {
					parameters.put("writeables", "false");
				}
				parameters.put("userlist", filter);
				SearcherSiteQuery sq = new SearcherSiteQuery(getSiteModel().getObject(), getIndex(), parameters);
				setResponsePage(new SearcherResultsPage(getSiteModel(), sq));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof MyListsApplyUserListEvent;
			}
		});
		
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		setPageTitle(new StringResourceModel("results", SearcherExplorerPage.this, null));
		if (hasPermissions()) {
			add(new SearcherExplorerBrowser("searcher", getSiteModel(), parameters));
		}	
		else {
			addOrReplace(new ErrorNotAuthorizedPanel<>("searcher"));
		}	
	}
	
	protected boolean hasLateralMenu() {
		return false;
	}
	
	@Override
	protected boolean isSearchForm() {
		return true;
	}
	
	@Override
	protected boolean isExplorerOn() {
		return true;
	}
	
	protected Query getQuery() {
		return query;
	}
	
	/** 
	 * Reports
	 *  
	 **/							
	protected String getPageType() {
		return "search-results";
	} // con | det
	
	protected String getContentTitle() {
		return null;
	} // content title or user title,
	
	protected String getStatsPageTitle() {
		return "search results";
	} // for console page, it is the name of the console
	
	protected Long getStatsPageId() {
		return Long.valueOf(0);
	}
	
	protected String getObjectId()  {
		return null;
	}
	// for user, domain, ...
	protected String getContentId() {
		return null;
	} //for content
}
