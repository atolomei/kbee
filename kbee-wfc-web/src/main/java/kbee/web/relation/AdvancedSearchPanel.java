package kbee.web.relation;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;

import com.novamens.content.base.Content;
import com.novamens.content.service.ContentService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.service.Index;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;
import com.novamens.solr.indexer.query.SolrQuery;
import com.novamens.wicket.markup.html.repeater.util.Searcher;

import kbee.util.logging.Logger;
import kbee.web.search.AdvancedSearchForm;

@SuppressWarnings("serial")

public abstract class AdvancedSearchPanel extends Panel  {
	private static final long serialVersionUID = 1L;
																											
	private static Logger logger = Logger.getLogger(AdvancedSearchPanel.class.getName());
	
	private Searcher searcher = new Searcher();
	private Query query = null;
	

	public AdvancedSearchPanel(String id) {
		super(id);
		
		
	}
	

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		setVisible(false);
		
		
		add(new AdvancedSearchForm("form") {
			public void onSubmit(AjaxRequestTarget target) {
				((SolrQuery)getQuery()).setFilterParameters(getBaseFilters());
				getQuery().setParameters(getFilters());
				searcher.detach();
				target.add(AdvancedSearchPanel.this);
			}
			public void onClose(AjaxRequestTarget target) {
				searcher.detach();
				target.add(AdvancedSearchPanel.this);
				AdvancedSearchPanel.this.onClose(target);
			}
			@Override
			protected boolean listenEnter() {
				return false;
			}
		});	

		WebMarkupContainer resultcontainer = new WebMarkupContainer("result-container") {
			public boolean isVisible() {
				return true;
				//return !getFilters().isEmpty();
			}
		};
		
		DataView<SearchResult> results = new DataView<SearchResult>("result", getSearcher(), 20) {
			@Override
			protected void populateItem(final Item<SearchResult> item) {
				try {
					SearchResult result = item.getModelObject();
					Content content = (Content)result.getObject();
					AjaxLink<?> selector = new AjaxLink<Void>("selector") {
						public void onClick(AjaxRequestTarget target) {
							onSelect(target, (Content)item.getModelObject().getObject());
						}
					};
					selector.add(new Label ("title", content!=null ? content.getTitle() : "-"));
					item.add(selector);
					String subt = content!=null? 
							(content.getService(ContentService.class).getPortalSubtitle() + ". v" + String.valueOf(content.getVersion())) 
							: "-";
					Label metadata =  new Label("metadata", subt );
					metadata.setEscapeModelStrings(false);
					item.add(metadata);
				} 
				catch (Exception e) {
					logger.error(e);
					AjaxLink<?> selector = new AjaxLink<Void>("selector") {
						@Override
						public void onClick(AjaxRequestTarget target) {
						}
					};
					selector.add(new Label ("title", e.getClass().getSimpleName()));
					item.add(selector);
					item.add(new Label("metadata", e.getMessage()));
				}
			}
		};
		
		resultcontainer.add(results);
		add(resultcontainer);

	}
	
	protected void onClose(AjaxRequestTarget target) {
		
	}
	
	protected void onSelect(AjaxRequestTarget target, Content content) {
		
	}
	
	protected Searcher getSearcher() {
		searcher.setQuery(getQuery());
		return searcher;
	}
	
	protected void search(AjaxRequestTarget target) {
	}
	
	protected Query getQuery() {
		if (query ==null) {
			query = new SolrParametersQuery(getQueryIndex());
		}
		return query;
	}
	

	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	
	protected Map<String, Object> getBaseFilters() {
		Map<String, Object> filters = new HashMap<String, Object>();
		filters.put("domain", String.valueOf(getDomain().getId()));
		filters.put("type", "[idoc]");
		filters.put("head", "true");
		return filters;
	}	
	
//	private Map<String, Object> getFilters() {
//		return ((AdvancedSearchForm)get("form")).getFilters();
//	}
}
