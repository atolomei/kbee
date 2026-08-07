package com.novamens.content.web.console.markup.searchselector;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.model.ObjectId;
import com.novamens.content.web.suggestion.service.UserSuggestionService;
import com.novamens.dom.Domain;
import com.novamens.dom.KBFSStorageType;
import com.novamens.indexer.query.PhoneticFilter;
import com.novamens.indexer.query.Suggestion;
import com.novamens.indexer.query.ValueFilter;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.kbee.wicket.markup.html.event.FilterSelectorClearAllEvent;
import com.novamens.kbee.wicket.markup.html.event.FilterSelectorEvent;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrDateRangeFilter;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.OffsetDateTimeField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.console.AdvancedSearchSelectorEditor;
import kbee.web.form.AutoCompleteFieldV5;
import kbee.web.form.EditButtonsV5;

/**
 * [KBFS] 
 * [Shard]
 */
public class AdvancedSearchResourcesSelectorPanel extends AdvancedSearchSelectorEditor<Void> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AdvancedSearchResourcesSelectorPanel.class.getName());

	private static final long serialVersionUID = 1L;

	private String title;
	private String oid;
	private IModel<User> uploadedBy;

	private IModel<Domain> domain_model;
	private List<IModel<Domain>> domains;

	private OffsetDateTime from;
	private OffsetDateTime to;
 	
	
	/**
	 * 
	 */
	public class KBFSStorageTypeProxy implements Serializable {
	
 		private static final long serialVersionUID = 1L;

 		private KBFSStorageType kbfs;
		private String name;
		private int id;
		
		public KBFSStorageTypeProxy (String name) {
			this.name=name;
			this.id=0;
		}
		
		public KBFSStorageTypeProxy (KBFSStorageType kbfs) {
			this.kbfs=kbfs;
			this.id=kbfs.getId();
			this.name=kbfs.getDisplayName();
		}
		
		public KBFSStorageType getKBFSStorageType() {
			return kbfs;
		}

		public int getId() {
			return id;
		}

		public String getDisplayName() {
			return (id==0?name:kbfs.getDisplayName());
		}
		
		public String getName() {
			return name;
		}

		public boolean isAll() {
			return id==0;
		}
	}

	
	/**
	 * 
	 */
 	public class KBFSStorageModel implements IModel<KBFSStorageTypeProxy> {

 		private static final long serialVersionUID = 1L;
		
 		private int id;
		private KBFSStorageTypeProxy object;
				
		public KBFSStorageModel(KBFSStorageTypeProxy object) {
			this.object=object;
			
			if (this.object.isAll())
				this.id = 0;	
			else
				this.id = object.getKBFSStorageType().getId();
		}
		public KBFSStorageTypeProxy getObject() {
			if (object==null) {
				if (id==0)
					object= new KBFSStorageTypeProxy("All");					
				else
					object= new KBFSStorageTypeProxy(KBFSStorageType.getById(this.id));
			}
			return object;
		}
		public void setObject(KBFSStorageTypeProxy object) {
			this.id=object.getId();
			this.object=object;
		}
		
		public void detach() {
			this.object=null;
		}
	}

	/**
	 * 
	 * 
	 * @param id
	 */
	public AdvancedSearchResourcesSelectorPanel(String id, OffsetDateTime from, OffsetDateTime to) {
		super(id);
		
		this.from = from;
		this.to   = to;
		
	}
	
	
	public IModel<Domain> getDomainModel() {
		return this.domain_model;
	}
	
	public void setDomainModel(IModel<Domain> d) {
		this.domain_model=d;
	}

	/**
	 * title
	 * domain
	 * kbfs
	 * daterange
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		setEditionEnabled(true);
		

		
		// ZonedDateTime zoned_from = ZonedDateTime.ofInstant(from.toInstant(), ZoneId.of(getDomain().getTimeZone())).truncatedTo(ChronoUnit.DAYS);
		// ZonedDateTime zoned_to  = ZonedDateTime.ofInstant(to.toInstant(), ZoneId.of(getDomain().getTimeZone())).truncatedTo(ChronoUnit.DAYS);
		// from = zoned_from.withZoneSameInstant(ZoneId.of(getDomain().getTimeZone())).toOffsetDateTime();
		// to	 = zoned_to.withZoneSameInstant( ZoneId.of(getDomain().getTimeZone())).toOffsetDateTime();
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new ChoiceField<IModel<Domain>>("domain", new PropertyModel<IModel<Domain>>(this, "domainModel"), new PropertyModel<List<IModel<Domain>>>(this, "domains")) {
		
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return isDomainKbee();
			}
			@Override
			public String getIdValue(IModel<Domain> value) {
				return String.valueOf(value.getObject().getId().toString());
			}

			@Override
			public String getDisplayValue(IModel<Domain> value) {
				return value.getObject().getOrganization(); //+ " ( "+ (value.getObject().getName())+")";
			}

			@Override
			public void onUpdate(AjaxRequestTarget target) {
				AdvancedSearchResourcesSelectorPanel.this.setDomainModel(getValue());
				setDomainModel(getValue());
				//getParameters().put("domain", getValue().getObject().getId().toString());
				//onChange(target, getParameters());
			}
		});
		
		form.add(new OffsetDateTimeField("from",  ZoneId.of(getDomain().getTimeZone()), new PropertyModel<OffsetDateTime>(this, "from"), false) {
			private static final long serialVersionUID = 1L;
			public void onUpdate(AjaxRequestTarget target) {
				logger.debug(getValue());
				logger.debug(getInputValue());
				setFrom(getValue());
			}
		});
		

		form.add(new  OffsetDateTimeField("to",  ZoneId.of(getDomain().getTimeZone()), new PropertyModel<OffsetDateTime>(this, "to"), false) {
			private static final long serialVersionUID = 1L;
			public void onUpdate(AjaxRequestTarget target) {
				logger.debug(getValue());
				logger.debug(getInputValue());
				setTo(getValue());
			}
		});

		form.add(new TextField<String>("title", new PropertyModel<String>(this, "title")));
		form.add(new TextField<String>("oid", new PropertyModel<String>(this, "oid")));
		
		WorkingIndicatorAjaxLinkV5<Void> clearall = new WorkingIndicatorAjaxLinkV5<Void>("clear-all", new StringResourceModel("clear-all", this, null).getString()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				((Form<?>) AdvancedSearchResourcesSelectorPanel.this.get("form")).clearInput();
				target.add(AdvancedSearchResourcesSelectorPanel.this);
				fire(new FilterSelectorClearAllEvent(target));
			}
			@Override
			public String getWorkingLabel() {
				return new StringResourceModel("working", this, null).getString();
			}
		};
		
		form.add(clearall);

		
		AutoCompleteFieldV5<User> usel = new AutoCompleteFieldV5<User>("uploadedBy", new PropertyModel<User>(AdvancedSearchResourcesSelectorPanel.this, "uploadedBy"), false) {
			private static final long serialVersionUID = 1L;

			@Override
			protected IModel<String> getHelpText() {
				//StringResourceModel model = new StringResourceModel("same-as.help", ResourcesAdvancedSearchSelectorPanel.this);
				//model.setParameters(NewUserEditor.this.getDomain().getName());
				//return model;
				return null;
			}
			
			@Override
			public IModel<String> getLabel() {
				return new Model<String>("Uploaded by");
				// return new StringResourceModel("same-as", ResourcesAdvancedSearchSelectorPanel.this);
				// IModel<String> m= getUserSelectorLabel();
				// if (m==null)
				//	return super.getLabel();
				// return m;
			}
			
			@Override
			public int getMaxHistory() {
				return 3;
			}
			
			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				return ServiceLocator.getService(UserSuggestionService.class).getSuggestions(pattern);
			}

			@Override
			public String getHistoryKey() {
				return "audit-uploaded-by";
			}

			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
					if (getValue() != null) {
						setUploadedBy( new ObjectModel<User>(getValue()));
					}
				}
			};
		
		form.add(usel);
		
		form.add(new EditButtonsV5<Void>(this, true) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean getDisableAfterSubmit() {
				return false;
			}
			@Override
			protected IModel<String> getSubmitLabel() {
				return new StringResourceModel("apply", AdvancedSearchResourcesSelectorPanel.this, null);
			}
			@Override
			protected String getSubmitClass() {
				return "btn btn-default btn-sm";
			}
		});
		
		add(form);
	}
	
	public IModel<User> getUploadedBy() {
		return uploadedBy;
	}


	public void setUploadedBy(IModel<User> uploadedBy) {
		this.uploadedBy = uploadedBy;
	}


	public OffsetDateTime getFrom() {
		return from;
	}


	public void setFrom(OffsetDateTime from) {
		this.from = from;
	}


	public OffsetDateTime getTo() {
		return to;
	}


	public void setTo(OffsetDateTime to) {
		this.to = to;
	}


	

	
	public List<String> getSortRanges() {
		List<String> list = new ArrayList<String>();
		list.add("Date (desc)");
		list.add("Date (asc)");
		list.add("Relevance");
		return list;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		
		if (this.domain_model!=null)
			this.domain_model.detach();
		
		if (this.domains!=null)
			this.domains.forEach(item -> item.detach());
 	}
	
	/**			
	public List<KBFSStorageTypeProxy> getKbfsList() {
		List<KBFSStorageTypeProxy> list = new ArrayList<KBFSStorageTypeProxy>();
		list.add(new KBFSStorageTypeProxy("All"));
		list.add(new KBFSStorageTypeProxy(KBFSStorageType.KBFS1));
		list.add(new KBFSStorageTypeProxy(KBFSStorageType.KBFS2));
		list.add(new KBFSStorageTypeProxy(KBFSStorageType.KBFS2Archive));
		list.add(new KBFSStorageTypeProxy(KBFSStorageType.External));
		list.add(new KBFSStorageTypeProxy(KBFSStorageType.KBFSAmazonS3));
		list.add(new KBFSStorageTypeProxy(KBFSStorageType.KBFSAmazonGlacier));
		return list;
	}

	
	public List<Filter> getDateRanges() {
		List<Filter> list = new ArrayList<Filter>();
		list.add(new SolrDateRangeFilter("date", getLabel("1minute"), 1, ChronoUnit.MINUTES));
		list.add(new SolrDateRangeFilter("date", getLabel("5minutes"), 5, ChronoUnit.MINUTES));
		list.add(new SolrDateRangeFilter("date", getLabel("15minutes"), 15, ChronoUnit.MINUTES));
		list.add(new SolrDateRangeFilter("date", getLabel("1hour"), 1, ChronoUnit.HOURS));
		list.add(new SolrDateRangeFilter("date", getLabel("4hour"), 4, ChronoUnit.HOURS));
		list.add(new SolrDateRangeFilter("date", getLabel("1day"), 1, ChronoUnit.DAYS));
		list.add(new SolrDateRangeFilter("date", getLabel("2day"), 2, ChronoUnit.DAYS));
		list.add(new SolrDateRangeFilter("date", getLabel("3day"), 3, ChronoUnit.DAYS));
		list.add(new SolrDateRangeFilter("date", getLabel("1week"), 1, ChronoUnit.WEEKS));
		list.add(new SolrDateRangeFilter("date", getLabel("1month"), 1, ChronoUnit.MONTHS));
		list.add(new SolrDateRangeFilter("date", getLabel("2month"), 2, ChronoUnit.MONTHS));
		list.add(new SolrDateRangeFilter("date", getLabel("3months"), 3, ChronoUnit.MONTHS));
		list.add(new SolrDateRangeFilter("date", getLabel("1year"), 1, ChronoUnit.YEARS));
		list.add(new SolrDateRangeFilter("date", getLabel("2year"), 2, ChronoUnit.YEARS));
		list.add(new SolrDateRangeFilter("date", getLabel("all"), 100, ChronoUnit.YEARS));
		return list;
	}
	*/

	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

 
	private String getStringLabel(String key) {
		return (new StringResourceModel(key, this, null)).getObject();
	}
	
	@Override
	public void update(AjaxRequestTarget target) {
		logger.debug(getFilters().toString());
		setEditionEnabled(true);
		fire(new FilterSelectorEvent(target, getFilters()));
		target.add(this);
	}

	
	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	public List<IModel<Domain>> getDomains() {

		if (this.domains != null)
			return this.domains;

		this.domains = new ArrayList<IModel<Domain>>();

		for (Domain domain : getContentDao().getDomains()) {
				this.domains.add(new ObjectModel<Domain>(domain));
			}
		
		Collections.sort(this.domains, new Comparator<IModel<Domain>>() {
			@Override
			public int compare(IModel<Domain> c1, IModel<Domain> c2) {
				try {
					if (c1.getObject().getOrganization() != null && c2.getObject().getOrganization() != null) {
						return c1.getObject().getOrganization().compareToIgnoreCase(c2.getObject().getOrganization());
					}
					return (c1.getObject().getOrganization() != null ? -1 : 0);
				} catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
		});
		return this.domains;
	}


	/**
	 * 
	 */
	@Override
	protected void clearAll() {
		uploadedBy=null;
		title=null;
		oid=null;
		setFrom(null);
		setTo(null);
		this.domain_model=null;
	}
	

	/**
	 * 
	 * Map<String, Object> filters = new HashMap<String, Object>();
	 * filters.put("modified", new SolrDateRangeFilter("modified",	d_from, d_to));
	 * 
	 */
	private Map<String, Object> getFilters() {
		
		Map<String, Object> filters = new HashMap<String, Object>();
									
		if (getDomainModel()!=null && getDomainModel().getObject()!=null)
		{
			filters.put("domain", new ValueFilter("domain", getDomainModel().getObject().getId().toString()));
		}
			
		if (getTitle()!=null && !"".equals(getTitle())) 
			filters.put("title", new PhoneticFilter("title", getTitle()));

		if (getOid()!=null && !"".equals(getOid())) {
			String o= (new ObjectId(KBFileImpl.class.getSimpleName().toLowerCase(), getOid())).toString();
			filters.put("id", new ValueFilter("id",  o));
		}
		else
			filters.remove("id");
	 
		try {
			if (getTo()!=null || getFrom()!=null) {
				
				logger.debug("from -> " + getFrom().toString());
				logger.debug("to -> " + getTo().toString());
				
				ZonedDateTime zoned_from = ZonedDateTime.ofInstant(from.toInstant(), ZoneId.of(getDomain().getTimeZone())).truncatedTo(ChronoUnit.DAYS);
				ZonedDateTime zoned_to  = ZonedDateTime.ofInstant(to.plusDays(1).toInstant(), ZoneId.of(getDomain().getTimeZone())).truncatedTo(ChronoUnit.DAYS);
				
				OffsetDateTime d_from  	 = zoned_from.withZoneSameInstant(ZoneId.of(getDomain().getTimeZone())).toOffsetDateTime();
				OffsetDateTime d_to		 = zoned_to.withZoneSameInstant( ZoneId.of(getDomain().getTimeZone())).toOffsetDateTime();

				logger.debug("from -> " + d_from.toString());
				logger.debug("to -> " + d_to.toString());
				
				filters.put("modified", new SolrDateRangeFilter("modified",	d_from, d_to));
			}
			else
				filters.remove("modified");
			
				
			if (getUploadedBy()!=null) {
				filters.put("lastmodifieduser", new ValueFilter("lastmodifieduser", getUploadedBy().getObject().getId().toString()));
			}
			else
				filters.remove("lastmodifieduser");
			
		} catch (Exception e) {
			logger.error(e);
		}
 		
		filters.put("sort", "modified");
		filters.put("ascending", "false");

		return filters;
	}
	

}
