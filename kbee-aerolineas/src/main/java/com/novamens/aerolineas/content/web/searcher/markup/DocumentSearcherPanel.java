package com.novamens.aerolineas.content.web.searcher.markup;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Attribute;
import com.novamens.content.query.AttributeFilter;
import com.novamens.content.service.ContentService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.PhoneticFilter;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.service.Index;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;
import com.novamens.solr.indexer.query.SolrQuery;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.repeater.util.Searcher;

@SuppressWarnings("serial")


/**
 * 
 * Title
 * Tipo de Documento
 * 
 *
 */
public abstract class DocumentSearcherPanel extends Panel  {
	private static final long serialVersionUID = 1L;
																											
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DocumentSearcherPanel.class.getName());
	
	private String title, code;
	private Searcher searcher = new Searcher();
	private Query query = null;

	public DocumentSearcherPanel(String id) {
		super(id);
		
		setOutputMarkupId(true);
		setVisible(false);
		
		Form<?> form = new Form<Void>("form");
		
		form.add(new TextField<String>("title", new PropertyModel<String>(this, "title")));
		form.add(new TextField<String>("code", new PropertyModel<String>(this, "code")));
		
		form.add(new AjaxButton("submit") {
			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				((SolrQuery)getQuery()).setFilterParameters(getBaseFilters());
				getQuery().setParameters(getFilters());
				searcher.detach();
				target.add(DocumentSearcherPanel.this);
			}
		});

		form.add(new AjaxLink<Void>("close") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				searcher.detach();
				target.add(DocumentSearcherPanel.this);
				DocumentSearcherPanel.this.onClose(target);
			}
		});
		
		form.setDefaultButton((AjaxButton)form.get("submit"));
				
		add(form);
		
		WebMarkupContainer resultcontainer = new WebMarkupContainer("result-container") {
			public boolean isVisible() {
				return !getFilters().isEmpty();
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
							(content.getService(ContentService.class).getPortalSubtitle() + ". Version " + String.valueOf(content.getVersion())) 
							: "-";
					item.add(new Label("metadata", subt ));
					
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
	
	protected abstract void onClose(AjaxRequestTarget target);

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setCode(String part) {
		this.code = part;
	}	
	
	public String getCode() {
		return code;
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
	
	private Map<String, Object> getBaseFilters() {
		Map<String, Object> filters = new HashMap<String, Object>();
		filters.put("domain", String.valueOf(getDomain().getId()));
		filters.put("type", "[idoc]");
		filters.put("head", "true");
		return filters;
	}	
	
	private Map<String, Object> getFilters() {
		Map<String, Object> filters = new HashMap<String, Object>();
		try {
			if (getTitle()!=null && !"".equals(getTitle())) 
				filters.put("title", new PhoneticFilter("title", getTitle()));
			
			if (getCode()!=null && !"".equals(getCode()))
				filters.put("code", new AttributeFilter(getAttribute("codigo"), getCode()));
		} catch (Exception e) {
			logger.error(e);
		}
		
		return filters;
	} 
	
	protected Attribute getAttribute(String name) {
		for (Attribute attribute : getContentDao().getAttributes(getDomain())) {
			if (attribute.getAlias().toLowerCase().equals(name.toLowerCase())) {
				return attribute;
			}
		}
		return null;
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

}
