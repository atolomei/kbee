package kbee.web.searcher.panel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElement;
import com.novamens.content.query.AttributeFilter;
import com.novamens.content.query.ClassifierFilter;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.DataAccessService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.PhoneticFilter;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.query.Suggestion;
import com.novamens.indexer.service.Index;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;
import com.novamens.solr.indexer.query.SolrQuery;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.repeater.util.Searcher;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.form.AutoCompleteFieldV5;

@SuppressWarnings("serial")

public abstract class SearcherAdvancedPanel extends Panel  {
	private static final long serialVersionUID = 1L;
																											
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SearcherAdvancedPanel.class.getName());
	
	private String title, code;
	
	private Searcher searcher = new Searcher();
	private Query query = null;
	
	private List<ElementModel> models;
	private Map<String, Object> filters;
	
	
	private interface ElementModel extends Serializable {
		public ModelElement getElement();
	}
	
	private class StringModel implements IModel<String>, ElementModel {
		private IModel<ModelElement> model;
		private String value;
		public StringModel(ModelElement element) {
			model = new ObjectModel<ModelElement>(element);
		}
		public ModelElement getElement() {
			return model.getObject();
		}
		public String getObject() {
			return value;
		}
		public void setObject(String value) {
			this.value = value;
		}
		public void detach() {
			model.detach();
		}
	}
	
	private class MemberModel implements IModel<DataSetMember>, ElementModel {
		private IModel<ModelElement> model;
		private IModel<DataSetMember> valuemodel;
		public MemberModel(ModelElement element) {
			model = new ObjectModel<ModelElement>(element);
		}
		public ModelElement getElement() {
			return model.getObject();
		}
		public DataSetMember getObject() {
			return valuemodel!=null ? valuemodel.getObject() : null;
		}
		public void setObject(DataSetMember value) {
			this.valuemodel = new ObjectModel<DataSetMember>(value);
		}
		public void detach() {
			model.detach();
			if (valuemodel!=null)
				valuemodel.detach();
		}
	}

	public SearcherAdvancedPanel(String id) {
		super(id);
		
		
	}
	

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		setVisible(false);
		
		Form<?> form = new Form<Void>("form");
		
		form.add(new TextField<String>("title", new PropertyModel<String>(this, "title")));
		((TextField<?>)form.get("title")).listenEnter(true);

		setElements();
		
		form.add(new ListView<ElementModel>("element", getElements()) {
			@SuppressWarnings("unchecked")
			public void populateItem(final ListItem<ElementModel> item) {
				ModelElement element = item.getModelObject().getElement(); 
				if (element instanceof Attribute) {
					item.add(new TextField<String>("field", (IModel<String>)item.getModelObject()) {
						public IModel<String> getLabel() {
							return new Model<String>(item.getModelObject().getElement().getDisplayName());
						}
					});
				}	
				if (element instanceof Classifier) {
					item.add(new AutoCompleteFieldV5<DataSetMember>("field", (IModel<DataSetMember>)item.getModelObject(), false) {
						@Override
						public void onUpdate(AjaxRequestTarget target) {
							target.focusComponent(getInput());
						}
						public int getMaxHistory() {
							return 3;
						}
						@Override
						public List<Suggestion> getSuggestions(String pattern) {
							return item.getModelObject().getElement().getService(DataAccessService.class).getSuggestions(pattern);
						}
						@Override 
						public String getHistoryKey() {
							return "advanced-search-"+item.getModelObject().getElement().getId();
						}
						@Override 
						public IModel<String> getLabel() {
							return new Model<String>(item.getModelObject().getElement().getDisplayName());
						}
					});					
				}
			}
		});
		
		form.add(new AjaxButton("submit") {
			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				((SolrQuery)getQuery()).setFilterParameters(getBaseFilters());
				getQuery().setParameters(getFilters());
				searcher.detach();
				target.add(SearcherAdvancedPanel.this);
			}
		});

		form.add(new AjaxLink<Void>("close") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				searcher.detach();
				target.add(SearcherAdvancedPanel.this);
				SearcherAdvancedPanel.this.onClose(target);
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
							(content.getService(ContentService.class).getPortalSubtitle() + ". v" + String.valueOf(content.getVersion())) 
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
	
	protected void setElements() {
		models = new ArrayList<ElementModel>();
		for (Classifier classifier : getContentDao().getClassifiers(getDomain())) {
			if (classifier.isSearchable()) {
				models.add(new MemberModel(classifier));
			}
		}
		for (Attribute attribute : getContentDao().getAttributes(getDomain())) {
			if (attribute.isSearchable()) {
				models.add(new StringModel(attribute));
			}
		}
	}
	
	protected List<ElementModel> getElements() {
		return models;
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
	
	
	public void onDetach() {
		super.onDetach();
		this.filters = null;
		
	}
	private Map<String, Object> getFilters() {
		
		
		if (filters!=null)
			return filters;
		
		this.filters = new HashMap<String, Object>();

		try {
			if (getTitle()!=null && !"".equals(getTitle())) 
				filters.put("title", new PhoneticFilter("title", getTitle()));
			for (ElementModel model : models) {
				if (model.getElement() instanceof Attribute) {
					String value = ((StringModel)model).getObject();
					if (value!=null && !"".equals(value)) {
						filters.put(model.getElement().getAlias(), new AttributeFilter((Attribute)model.getElement(), ((StringModel)model).getObject()));
					}	
				}
				if (model.getElement() instanceof Classifier) {
					if (((MemberModel)model).getObject()!=null) {
						filters.put(model.getElement().getAlias(), new ClassifierFilter((Classifier)model.getElement(), ((MemberModel)model).getObject()));
					}	
				}
			}
		} 
		catch (Exception e) {
			filters = null;
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
