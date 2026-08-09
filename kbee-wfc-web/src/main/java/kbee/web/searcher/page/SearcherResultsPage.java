package kbee.web.searcher.page;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.base.Content;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.portal.model.SearcherSiteQuery;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
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
import kbee.web.searcher.panel.SearcherResultsBrowser;
import kbee.web.searcher.searchform.SearcherOnChangeEvent;

@SuppressWarnings("serial")
public class SearcherResultsPage extends AbstractSearcherPage<Void> {
	
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(SearcherResultsPage.class.getName());

	private Query query;

	public SearcherResultsPage(PageParameters parameters) {
		Site site = getSite(parameters);
		setOutputMarkupId(true);
		if (site != null) {
			setSiteModel(new ObjectModel<Site>(site));
		}
	}

	public SearcherResultsPage(IModel<Site> site_model, Query query) {
		this.query = query;
		setOutputMarkupId(true);
		setSiteModel(site_model);
		getPageParameters().set("siteurl", getSiteModel().getObject().getUrl().toString());
	}

	@Override
	protected void addListeners() {
		super.addListeners();

		add(new WicketEventListener<FilterSelectorClearAllEvent>() {
			@Override
			public void onEvent(FilterSelectorClearAllEvent event) {
				try {
					SearcherSiteQuery sq = new SearcherSiteQuery(getSiteModel().getObject(), getIndex(), null);
					SearcherResultsPage s = new SearcherResultsPage(getSiteModel(), sq);
					setResponsePage(s);
				} catch (Exception e) {
					setResponsePage(new ApplicationErrorPage<>(e));
					logger.error(e);
				}
			}

			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof FilterSelectorClearAllEvent;
			}
		});

		add(new WicketEventListener<EditableListEvent<Site>>() {
			public void onEvent(EditableListEvent<Site> event) {
				try {
					Map<String, Object> parameters = getQuery().getParameters();
					parameters.put("writeables", isEditableOn() ? "true" : "false");
					SearcherSiteQuery sq = new SearcherSiteQuery(getSiteModel().getObject(), getIndex(), parameters);
					SearcherResultsPage s = new SearcherResultsPage(getSiteModel(), sq);
					setResponsePage(s);
				} catch (Exception e) {
					setResponsePage(new ApplicationErrorPage<>(e));
					logger.error(e);
				}
			}
		});

		add(new WicketEventListener<SearcherOnChangeEvent>() {
			public void onEvent(SearcherOnChangeEvent event) {
				try {
					Map<String, Object> parameters = event.getParameters();
					parameters.put("writeables", isEditableOn() ? "true" : "false");
					SearcherSiteQuery sq = new SearcherSiteQuery(getSiteModel().getObject(), getIndex(), parameters);
					setResponsePage(new SearcherResultsPage(getSiteModel(), sq));
				} catch (Exception e) {
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
					if (getQuery() != null) {
						SolrSearcherNavigator<Content> na = new SolrSearcherNavigator<Content>(new Searcher(getQuery()), event.getIndex());
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
					parameters.put("writeables", isEditableOn() ? "true" : "false");
					setResponsePage(new SearcherExplorerPage(event.getModel(), parameters));
				} catch (Exception e) {
					setResponsePage(new ApplicationErrorPage<>(e));
					logger.error(e);
				}
			}
		});
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		setPageTitle(getLabel("results"));
		if (hasPermissions()) {
			add(new SearcherResultsBrowser("searcher", (query != null) ? query : new SearcherSiteQuery(getSiteModel().getObject(), getIndex()), getSiteModel()));
		} else {
			addOrReplace(new ErrorNotAuthorizedPanel<>("searcher"));
		}
	}

	protected Query getQuery() {
		return query;
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
		return false;
	}

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

	protected String getObjectId() {
		return null;
	} // for user, domain, ...

	protected String getContentId() {
		return null;
	} // for content

}
