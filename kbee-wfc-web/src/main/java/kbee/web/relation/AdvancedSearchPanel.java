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
	
//	private List<ElementModel> models;
//	private Map<String, Object> filters;
//	
//	
//	private interface ElementModel extends Serializable {
//		public ModelElement getElement();
//	}
//	
//	private class StringModel implements IModel<String>, ElementModel {
//		private IModel<ModelElement> model;
//		private String value;
//		public StringModel(ModelElement element) {
//			model = new ObjectModel<ModelElement>(element);
//		}
//		public ModelElement getElement() {
//			return model.getObject();
//		}
//		public String getObject() {
//			return value;
//		}
//		public void setObject(String value) {
//			this.value = value;
//		}
//		public void detach() {
//			model.detach();
//		}
//	}
//	
//	private class MemberModel implements IModel<DataSetMember>, ElementModel {
//		private IModel<ModelElement> model;
//		private IModel<DataSetMember> valuemodel;
//		public MemberModel(ModelElement element) {
//			model = new ObjectModel<ModelElement>(element);
//		}
//		public ModelElement getElement() {
//			return model.getObject();
//		}
//		public DataSetMember getObject() {
//			return valuemodel!=null ? valuemodel.getObject() : null;
//		}
//		public void setObject(DataSetMember value) {
//			this.valuemodel = new ObjectModel<DataSetMember>(value);
//		}
//		public void detach() {
//			model.detach();
//			if (valuemodel!=null)
//				valuemodel.detach();
//		}
//	}

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
//		Form<?> form = new Form<Void>("form");
//		
//		form.add(new TextField<String>("title", new PropertyModel<String>(this, "title")));
//		((TextField<?>)form.get("title")).listenEnter(true);
//		
//		setElements();
//		
//		form.add(new ListView<ElementModel>("element", getElements()) {
//			@SuppressWarnings("unchecked")
//			public void populateItem(final ListItem<ElementModel> item) {
//				ModelElement element = item.getModelObject().getElement(); 
//				if (element instanceof Attribute) {
//					item.add(new TextField<String>("field", (IModel<String>)item.getModelObject()) {
//						public IModel<String> getLabel() {
//							return new Model<String>(item.getModelObject().getElement().getDisplayName());
//						}
//					});
//				}	
//				if (element instanceof Classifier) {
//					item.add(new AutoCompleteFieldV5<DataSetMember>("field", (IModel<DataSetMember>)item.getModelObject(), false) {
//						@Override
//						public void onUpdate(AjaxRequestTarget target) {
//							target.focusComponent(getInput());
//						}
//						public int getMaxHistory() {
//							return 3;
//						}
//						@Override
//						public List<Suggestion> getSuggestions(String pattern) {
//							return item.getModelObject().getElement().getService(DataAccessService.class).getSuggestions(pattern);
//						}
//						@Override 
//						public String getHistoryKey() {
//							return "advanced-search-"+item.getModelObject().getElement().getId();
//						}
//						@Override 
//						public IModel<String> getLabel() {
//							return new Model<String>(item.getModelObject().getElement().getDisplayName());
//						}
//					});					
//				}
//			}
//		});
//		
//		form.add(new AjaxButton("submit") {
//			@Override
//			protected void onSubmit(AjaxRequestTarget target) {

//			}
//		});
//
//		form.add(new AjaxLink<Void>("close") {
//			@Override
//			public void onClick(AjaxRequestTarget target) {
//				searcher.detach();
//				target.add(AdvancedSearchPanel.this);
//				AdvancedSearchPanel.this.onClose(target);
//			}
//		});
//		
//		form.setDefaultButton((AjaxButton)form.get("submit"));
//				
//		add(form);
		
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
	
//	protected List<Classifier> getClassifiers() {
//		return getContentDao().getClassifiers(getDomain());
//	}
//	
//	protected List<Attribute> getAttributes() {
//		return getContentDao().getAttributes(getDomain());
//	}

//	protected Attribute getAttribute(String name) {
//		for (Attribute attribute : getContentDao().getAttributes(getDomain())) {
//			if (attribute.getAlias().toLowerCase().equals(name.toLowerCase())) {
//				return attribute;
//			}
//		}
//		return null;
//	}
//	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
//	protected ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
	
	protected Map<String, Object> getBaseFilters() {
		Map<String, Object> filters = new HashMap<String, Object>();
		filters.put("domain", String.valueOf(getDomain().getId()));
		filters.put("type", "[idoc]");
		filters.put("head", "true");
		return filters;
	}	
	
	private Map<String, Object> getFilters() {
		return ((AdvancedSearchForm)get("form")).getFilters();
	}
}
