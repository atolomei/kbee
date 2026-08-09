package kbee.web.console;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElement;
import com.novamens.content.query.AttributeFilter;
import com.novamens.content.query.ClassifierFilter;
import com.novamens.content.service.DataAccessService;
import com.novamens.content.service.domain.DomainPreferencesService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.PhoneticFilter;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.wicket.markup.html.event.FilterSelectorEvent;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.OffsetDateTimeField;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.form.AutoCompleteFieldV5;
import kbee.web.form.EditButtonsV5;

/**
 * 
 *
 */
@SuppressWarnings("serial")
public class AdvancedSearchContentSelectorPanel extends AdvancedSearchSelectorEditor<Void> {
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AdvancedSearchContentSelectorPanel.class.getName());

	private String key;
	private String title;
	private String oid;
	private String externalid;

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
					
	private class OffsetDateTimeModel implements IModel<OffsetDateTime>, ElementModel {
				
		private IModel<ModelElement> model;
		private OffsetDateTime value;
		
		public OffsetDateTimeModel (ModelElement element) {
			model = new ObjectModel<ModelElement>(element);
		}
		public ModelElement getElement() {
			return model.getObject();
		}
		public OffsetDateTime getObject() {
			return value;
		}
		public void setObject(OffsetDateTime value) {
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
	
	public AdvancedSearchContentSelectorPanel(String id, String key) {
		super(id);
		this.key=key;
	}

	@Override
	protected void clearAll() {
		setTitle(null);
		setOid(null);
		setExternalid(null);
	}
	

	public String getKey() {
		return this.key;
	}
	
	protected List<ElementModel> getElements() {
		return models;
	}
	
	protected void setElements() {
		models = new ArrayList<ElementModel>();
		
		for (Classifier classifier : getContentDao().getClassifiers(getDomain().getId(), ObjectState.ENABLED)) {
			if (classifier.isSearchable()) {
				models.add(new MemberModel(classifier));
			}
		}
		
		for (Attribute attribute : getContentDao().getAttributes(getDomain())) {
			if (attribute.isSearchable() && attribute.getState()==ObjectState.ENABLED) {
				if (attribute.isDate())
					models.add(new OffsetDateTimeModel(attribute));
				else
					models.add(new StringModel(attribute));
			}
		}
	}
	
	static final String css_st[] = {"col-lg-12", "col-lg-12", "col-lg-4", "col-lg-4", "col-lg-3", "col-lg-2", "col-lg-2", "col-lg-6"};
	
	@Override
	@SuppressWarnings("unchecked")
	public void onInitialize() {
		super.onInitialize();

		this.setElements();

		setOutputMarkupId(true);
		setEditionEnabled(true);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		TextField<String> t1 = new TextField<String>("title", new PropertyModel<String>(this, "title"));	t1.setPlaceholderLabel(false); form.add(t1);
		t1.setVisible(getDefaultDomainPreference("title", "yes").equals("yes"));
		
						
		TextField<String> t2 = new TextField<String>("oid", new PropertyModel<String>(this, "oid"));	t2.setPlaceholderLabel(false); form.add(t2);
		t2.setVisible(getDefaultDomainPreference("oid", "yes").equals("yes"));

		// TextField<String> t3 = new TextField<String>("externalid", new PropertyModel<String>(this, "externalid"));	
		// t2.setPlaceholderLabel(false); 
		// form.add(t3);
		// t3.setVisible(getDefaultDomainPreference("externalid", "yes").equals("yes"));
		
		
		// final String css_1= css_st[getElements().size() % css_st.length];
		final String css_1= "col-lg-4";
		
		
		
		form.add(new ListView<ElementModel>("element", getElements()) {
			public void populateItem(final ListItem<ElementModel> item) {
				
				ModelElement element = item.getModelObject().getElement(); 
				
				item.add( new AttributeModifier("class", css_1 + " col-md-6 col-xs-12 ")); 
				
				if (element instanceof Attribute) {
					if ( ((Attribute) element).isDate()) {
						OffsetDateTimeField df = new OffsetDateTimeField("field",	getSessionUser().getZoneId(), (IModel<OffsetDateTime>) item.getModelObject(), false) {
							public IModel<String> getLabel() {
								return new Model<String>(item.getModelObject().getElement().getDisplayName());
							}
						};
						item.add(df);
					}
					else {
						item.add(new TextField<String>("field", (IModel<String>)item.getModelObject()) {
							public IModel<String> getLabel() {
								return new Model<String>(item.getModelObject().getElement().getDisplayName());
							}
						});
					}
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
							return AdvancedSearchContentSelectorPanel.this.getKey()+"-advanced-search-"+item.getModelObject().getElement().getId();
						}
						@Override 
						public IModel<String> getLabel() {
							return new Model<String>(item.getModelObject().getElement().getDisplayName());
						}
					});					
				}
			}
		});
				
		form.add(new EditButtonsV5<Void>(this, true) {
			@Override
			public boolean getDisableAfterSubmit() {
				return false;
			}
			@Override
			protected IModel<String> getSubmitLabel() {
				return new StringResourceModel("apply", AdvancedSearchContentSelectorPanel.this, null);
			}
			@Override
			protected String getSubmitClass() {
				return "btn btn-default btn-sm";
			}
		});
		
		add(form);
	}
	
	@Override
	public  void update(AjaxRequestTarget target) {

	 
		setEditionEnabled(true);
		
		fire(new FilterSelectorEvent(target, getFilters()));
		target.add(this);
	}

	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public String getOid() {
		return oid;
	}
	
	public void setOid(String o) {
		this.oid = o;
	}

	public String getExternalid() {
		return externalid;
	}
	
	public void setExternalid(String o) {
		this.externalid = o;
	}
	
	
	
	public void onDetach() {
		super.onDetach();
		this.filters = null;
		
	}
	
	protected String getDefaultDomainPreference(String key, String defaultValue) {
		Domain domain = getDomain();
		if (domain==null || key==null)
			return defaultValue;
		return domain.getService(DomainPreferencesService.class).getValue(getKey()+"/"+"advancedsearch", key, defaultValue);
	}

	private Map<String, Object> getFilters() {
		
		
		if (filters!=null)
			return filters;
		
		this.filters = new HashMap<String, Object>();

		try {
			
			if (getTitle()!=null && !"".equals(getTitle())) 
				filters.put("title", new PhoneticFilter("title", getTitle().trim()));
			
			
			if (getOid()!=null && !"".equals(getOid())) 
				filters.put("objectid", getOid().trim());
			

			if (getExternalid()!=null && !"".equals(getExternalid())) 
				filters.put("externalid", getExternalid().trim());

			
			for (ElementModel model : models) {
				
				if (model.getElement() instanceof Attribute) {
					if ( ((Attribute) model.getElement()).isDate()) {
						OffsetDateTime value = ((OffsetDateTimeModel) model).getObject();
						if (value!=null && !"".equals(value.toString())) {
							OffsetDateTime dt =((OffsetDateTimeModel)model).getObject();
							//String str=ServiceLocator.getService(DateTimeService.class).format(dt,  getSessionUser().getZoneId().getId(), getSessionUser().getLocale(), DateTimeService.Year_Month_Day);
							// 2019 05 01			
							String str =ServiceLocator.getService(DateTimeService.class).getSolrFieldValue(dt);
							
							
							filters.put(model.getElement().getAlias(), new AttributeFilter((Attribute) model.getElement(), str));
						}
							
					}
					else {
						String value = ((StringModel)model).getObject();
						if (value!=null && !"".equals(value)) {
							filters.put(model.getElement().getAlias(), new AttributeFilter((Attribute)model.getElement(), ((StringModel)model).getObject()));
					}	
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
}
