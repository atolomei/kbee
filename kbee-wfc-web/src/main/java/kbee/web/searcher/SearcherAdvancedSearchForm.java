package kbee.web.searcher;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElement;
import com.novamens.content.query.AttributeFilter;
import com.novamens.content.query.ClassifierFilter;
import com.novamens.content.service.DataAccessService;
import com.novamens.content.service.domain.DomainPreferencesService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.PhoneticFilter;
import com.novamens.indexer.query.Suggestion;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrDateRangeFilter;
import com.novamens.wicket.markup.html.form.DateTimeField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.console.AdvancedSearchSelectorEditor;
import kbee.web.form.AutoCompleteFieldV5;
import kbee.web.search.AdvancedSearchForm;
import kbee.web.search.service.AdvancedSearchService;
import kbee.web.searcher.searchform.AdvancedSearchClickEvent;
import kbee.web.searcher.searchform.SearcherOnChangeEvent;


@SuppressWarnings("serial")
public class SearcherAdvancedSearchForm extends KBPanel {
//public class SearcherAdvancedSearchForm extends AdvancedSearchSelectorEditor<Void> {
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SearcherAdvancedSearchForm.class.getName());

	private String key;
	String title;

//	private List<ElementModel> models;
//	private Map<String, Object> filters;
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
// 	}
//					
//	private class OffsetDateTimeModel implements IModel<OffsetDateTime>, ElementModel {
//				
//		private IModel<ModelElement> model;
//		private OffsetDateTime value;
//		
//		public OffsetDateTimeModel (ModelElement element) {
//			model = new ObjectModel<ModelElement>(element);
//		}
//		public ModelElement getElement() {
//			return model.getObject();
//		}
//		public OffsetDateTime getObject() {
//			return value;
//		}
//		public void setObject(OffsetDateTime value) {
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
	
	public SearcherAdvancedSearchForm(String id, IModel<Site> sitemodel) {
		super(id);
		setOutputMarkupId(true);
		this.key = sitemodel.getObject().getName();
	}

//	@Override
//	protected void clearAll() {
//		setTitle(null);
//	}

	public String getKey() {
		return this.key;
	}
	
//	protected List<ElementModel> getElements() {
//		return models;
//	}
//	
//	protected void setElements() {
//		
//		models = new ArrayList<>();
//		
//		for (ModelElement element : getDomain()
//				.getService(AdvancedSearchService.class)
//				.getElements()) {
//			if (element instanceof Classifier) {
//				models.add(new MemberModel((Classifier)element));
//			}
//			else {
//				if (element instanceof Attribute) {
//					if (((Attribute)element).isDate())
//						models.add(new OffsetDateTimeModel((Attribute)element));
//					else
//						models.add(new StringModel((Attribute)element));
//				}
//			}
//		}
//	}
	
	static final String css_st[] = {"col-lg-12", "col-lg-12", "col-lg-6", "col-lg-4", "col-lg-3", "col-lg-2", "col-lg-2", "col-lg-6"};
	
	@Override
	public void onInitialize() {
		super.onInitialize();

//		this.setElements();

//		setEditionEnabled(true);
		
		add(new AdvancedSearchForm("form") {
			public void onSubmit(AjaxRequestTarget target) {
				update(target);
			}
			public void onClose(AjaxRequestTarget target) {
				fire(new AdvancedSearchClickEvent(target));
			}
		});	
		
//		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
//		
//		TextField<String> t1 = new TextField<String>("title", new PropertyModel<String>(this, "title"));	t1.setPlaceholderLabel(false); form.add(t1);
//		t1.setVisible(getDefaultDomainPreference("title", "yes").equals("yes"));
//		
//		final String css_1= css_st[getElements().size() % css_st.length];
//		
//		form.add(new ListView<ElementModel>("element", getElements()) {
//			@SuppressWarnings("unchecked")
//			public void populateItem(final ListItem<ElementModel> item) {
//				
//				ModelElement element = item.getModelObject().getElement(); 
//				
//				if (getElements().size()==5 && item.getIndex()<2) 
//					item.add( new AttributeModifier("class", "col-lg-3" + " col-md-6 col-xs-12 "));
//				else
//					item.add( new AttributeModifier("class", css_1 + " col-md-6 col-xs-12 ")); 
//				
//				if (element instanceof Attribute) {
//					if ( ((Attribute) element).isDate()) {
// 						DateTimeField df = new DateTimeField("field",	getSessionUser().getZoneId(), (IModel<OffsetDateTime>) item.getModelObject(), false) {
//							public IModel<String> getLabel() {
//								return new Model<String>(item.getModelObject().getElement().getDisplayName());
//							}
//						};
//						item.add(df);
//					}
//					else {
//						item.add(new TextField<String>("field", (IModel<String>)item.getModelObject()) {
//							public IModel<String> getLabel() {
//								return new Model<String>(item.getModelObject().getElement().getDisplayName());
//							}
//						});
//					}
//				}	
//				
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
//							return SearcherAdvancedSearchForm.this.getKey()+"-advanced-search-"+item.getModelObject().getElement().getId();
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
//		AjaxSubmitLink sl =new AjaxSubmitLink("submit") {
//			@Override
//			protected void onSubmit(AjaxRequestTarget target) {
//				 update(target);
//			}
//			@Override
//			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
//				super.updateAjaxAttributes(attributes);
//				IAjaxCallListener listener = new IAjaxCallListener() {
//					@Override
//					public CharSequence getSuccessHandler(Component component) {
//						return null;
//					}
//					@Override
//					public CharSequence getPrecondition(Component component) {
//						return null;
//					}
//					@Override
//					public CharSequence getFailureHandler(Component component) {
//						return null;
//					}
//					@Override
//					public CharSequence getCompleteHandler(Component component) {
//						return null;
//					}
//					@Override
//					public CharSequence getBeforeSendHandler(Component component) {
//						return null;
//					}
//					@Override
//					public CharSequence getBeforeHandler(Component component) {
//						return "document.getElementById('"+component.getMarkupId()+"').innerHTML = '<span class=\"far fa-sync fa-spin fa-fw spinning\"></span> "+getLabel("working").getObject() +"'";
//					}
//					@Override
//					public CharSequence getAfterHandler(Component component) {
//						return null;
//					}
//					@Override
//					public CharSequence getDoneHandler(Component component) {
//						return null;
//					}
//					@Override
//					public CharSequence getInitHandler(Component component) {
//						return null;
//					}
//				};
//				attributes.getAjaxCallListeners().add(listener);
//			}
//		};
//
//		String bu="float:left; width:100%; line-height: 30px; border-radius: 6px;  height: 40px; background:#385377; border-color:#385377;";
//				
//		if (getElements().size()>2)
//			sl.add( new AttributeModifier("style", bu+ " width:100%;"));
//		else
//			sl.add( new AttributeModifier("style", bu));
//		
//		form.add(sl);
//		
//		form.add(new AjaxLink<Void>("simple") {
//			@Override
//			public void onClick(AjaxRequestTarget target) {
//				fire(new AdvancedSearchClickEvent(target));
//			}
//			public boolean isVisible() {
//				return true;
//			}
//		});
//		
//		add(form);
	}
	
	//@Override
	public  void update(AjaxRequestTarget target) {
		//setEditionEnabled(true);
		fire(new SearcherOnChangeEvent(target, getFilters()));
		target.add(this);
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
//	
//	public void onDetach() {
//		super.onDetach();
//		this.filters = null;
//	}
//	
//	private String getDefaultDomainPreference(String key, String defaultValue) {
//		Domain domain = getDomain();
//		if (domain==null || key==null)
//			return defaultValue;
//		return domain.getService(DomainPreferencesService.class).getValue(getKey()+"/"+"advancedsearch", key, defaultValue);
//	}

	private Map<String, Object> getFilters() {
		return ((AdvancedSearchForm)get("form")).getFilters();
//		if (filters!=null)
//			return filters;
//		this.filters = new HashMap<String, Object>();
//		try {
//			if (getTitle()!=null && !"".equals(getTitle())) 
//				filters.put("title", new PhoneticFilter("title", getTitle()));
//			for (ElementModel model : models) {
//				
//				if (model.getElement() instanceof Attribute) {
//					Attribute attribute = (Attribute) model.getElement();
//					if (AttributeType.VALIDITY_TO.equals(attribute.getType())) {
//						OffsetDateTime value = ((OffsetDateTimeModel) model).getObject();
//						if (value!=null) {
//							filters.put(attribute.getAlias(), new SolrDateRangeFilter(attribute, null, value));
//						}	
//					}
//					else
//					if (AttributeType.VALIDITY_FROM.equals(attribute.getType())) {
//						OffsetDateTime value = ((OffsetDateTimeModel) model).getObject();
//						if (value!=null) {
//							filters.put(attribute.getAlias(), new SolrDateRangeFilter(attribute, value, null));
//						}	
//					}
//					else {
//						if (attribute.isDate()) {
//							OffsetDateTime value = ((OffsetDateTimeModel) model).getObject();
//							if (value!=null) {
//								OffsetDateTime dt =((OffsetDateTimeModel)model).getObject();
//								String str =ServiceLocator.getService(DateTimeService.class).getSolrFieldValue(dt);
//								filters.put(model.getElement().getAlias(), new AttributeFilter(attribute, str));
//							}
//						}
//						else  {
//							String value = ((StringModel)model).getObject();
//							if (value!=null && !"".equals(value)) {
//								filters.put(model.getElement().getAlias(), new AttributeFilter(attribute, ((StringModel)model).getObject()));
//							}	
//						}	
//					}
//				}
//				if (model.getElement() instanceof Classifier) {
//					if (((MemberModel)model).getObject()!=null) {
//						filters.put(model.getElement().getAlias(), new ClassifierFilter((Classifier)model.getElement(), ((MemberModel)model).getObject()));
//					}	
//				}
//			}
//		} 
//		catch (Exception e) {
//			filters = null;
//			logger.error(e);
//		}
//		return filters;
	} 
}