package com.novamens.content.web.console.audit.markup;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.kbee.wicket.markup.html.event.FilterSelectorEvent;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.OffsetDateTimeField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.console.AdvancedSearchSelectorEditor;
import kbee.web.form.EditButtonsV5;

public class AuditContentAdvancedSearchPanel extends AdvancedSearchSelectorEditor<Void> {
				
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AuditContentAdvancedSearchPanel.class.getName());
	
	private String title;
	private String oid;
	private String cid;

	private OffsetDateTime fromDate;
	private OffsetDateTime toDate;
	
	private String	executedto;
	private String executedfrom;
	
	/** private Filter daterange = new SolrDateRangeFilter("date", "All", 50, ChronoUnit.YEARS);*/
	
	public AuditContentAdvancedSearchPanel (String id) {
		super(id);
		setFromDate(OffsetDateTime.now().minusDays(1));
		setToDate(OffsetDateTime.now());
	}
	
	
 
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		setEditionEnabled(true);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
							
		form.add(new TextField<String>("oid", new PropertyModel<String>(this, "oid")));
		form.add(new TextField<String>("cid", new PropertyModel<String>(this, "cid")));
		form.add(new TextField<String>("title", new PropertyModel<String>(this, "title")));
		
		OffsetDateTimeField sp = new OffsetDateTimeField("from-date", ZoneId.of(getDomain().getTimeZone()), new PropertyModel<OffsetDateTime>(this, "fromDate"), true);
		OffsetDateTimeField ep = new OffsetDateTimeField("to-date", ZoneId.of(getDomain().getTimeZone()), new PropertyModel<OffsetDateTime>(this, "toDate"), true);
		
		form.add(sp);
		form.add(ep);

	 
		
		form.add(new EditButtonsV5<Void>(this, true) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean getDisableAfterSubmit() {
				return false;
			}
			@Override
			protected IModel<String> getSubmitLabel() {
				return new StringResourceModel("apply", AuditContentAdvancedSearchPanel.this, null);
			}
			@Override
			protected String getSubmitClass() {
				return "btn btn-default btn-sm";
			}
		});
		
		
	 
		
		add(form);
	}
	
 
	
	public String getExecutedto() {
		return executedto;
	}

	public void setExecutedto(String executedto) {
		this.executedto = executedto;
	}

	public String getExecutedfrom() {
		return executedfrom;
	}

	public void setExecutedfrom(String executedfrom) {
		this.executedfrom = executedfrom;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	public String getCId() {
		return cid;
	}

	public void setCId(String cid) {
		this.cid = cid;
	}

	@Override
	public  void update(AjaxRequestTarget target) {
		if (logger.isDebugEnabled())
			logger.debug(getFilters().toString());
		setEditionEnabled(true);
		fire(new FilterSelectorEvent(target, getFilters()));
		target.add(this);
	}
	
	

	public String getTitle() {
		return title;
	}

			
	/**
	public List<String> getSortRanges() {
		List<String> list = new ArrayList<String>();
		list.add("Date (desc)");
		list.add("Date (asc)");
		list.add("Relevance");
		return list;
	}
	
		
	public List<Filter> getDateRanges() {
		List<Filter> list = new ArrayList<Filter>();
		list.add(new SolrDateRangeFilter("date", getLabel("1minute"), 1, ChronoUnit.MINUTES));
		list.add(new SolrDateRangeFilter("date", getLabel("5minutes"), 5, ChronoUnit.MINUTES));
		list.add(new SolrDateRangeFilter("date", getLabel("1hour"), 1, ChronoUnit.HOURS));
		list.add(new SolrDateRangeFilter("date", getLabel("1day"), 1, ChronoUnit.DAYS));
		list.add(new SolrDateRangeFilter("date", getLabel("1week"), 1, ChronoUnit.WEEKS));
		list.add(new SolrDateRangeFilter("date", getLabel("1month"), 1, ChronoUnit.MONTHS));
		list.add(new SolrDateRangeFilter("date", getLabel("3months"), 3, ChronoUnit.MONTHS));
		list.add(new SolrDateRangeFilter("date", getLabel("1year"), 1, ChronoUnit.YEARS));
		list.add(new SolrDateRangeFilter("date", getLabel("all"), 50, ChronoUnit.YEARS));
		return list;
	}**/
	
	
	
	
	
	private Map<String, Object> getFilters() {
		
		Map<String, Object> filters  = new HashMap<String, Object>();

		if (getTitle()!=null && !"".equals(getTitle())) {
			filters.put("title", getTitle());
		}
		else
			filters.put("title", "null");

		
		if (getOid()!=null && !"".equals(getOid()))
				filters.put("oid", getOid().trim());
		else
			filters.put("oid", "null");
							
		if (getCId()!=null && !"".equals(getCId()))
			filters.put("id", getCId().trim());
		else
			filters.put("id", "null");
		
		if (getFromDate()!=null)
			filters.put("fromdate", getFromDate());
		else
			filters.put("fromdate", "null");

			
		if (getToDate()!=null)
			filters.put("todate", getToDate());
		else
			filters.put("todate", "null");
		

		filters.put("sort", "executed");
		filters.put("ascending", "false");
	
		return filters;
	}
	
	public OffsetDateTime getFromDate() {
		return fromDate;
	}

	public void setFromDate(OffsetDateTime fromDate) {
		this.fromDate = fromDate;
	}

	public OffsetDateTime getToDate() {
		return toDate;
	}

	public void setToDate(OffsetDateTime toDate) {
		this.toDate = toDate;
	}


	@Override
	protected void clearAll() {
		// TODO Auto-generated method stub
		
	}

	
}
