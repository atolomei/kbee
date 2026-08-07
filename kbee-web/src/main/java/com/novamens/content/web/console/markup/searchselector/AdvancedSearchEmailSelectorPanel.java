package com.novamens.content.web.console.markup.searchselector;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.dom.Domain;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.kbee.wicket.markup.html.event.FilterSelectorClearAllEvent;
import com.novamens.kbee.wicket.markup.html.event.FilterSelectorEvent;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.DateTimeField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.OffsetDateTimeField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;


import kbee.web.console.AdvancedSearchSelectorEditor;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class AdvancedSearchEmailSelectorPanel extends AdvancedSearchSelectorEditor<Void>  {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AdvancedSearchEmailSelectorPanel.class.getName());

	
	private String to;
	private String from;
	private String text;
	
	private OffsetDateTime fromDate;
	private OffsetDateTime toDate;
	
	
	private String result = "All";	
	
		public AdvancedSearchEmailSelectorPanel(String id) {
		super(id);
		setFromDate(OffsetDateTime.now().minusDays(7));
		setToDate(OffsetDateTime.now().plusDays(1).truncatedTo(ChronoUnit.DAYS));
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		setEditionEnabled(true);

		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		/**
		WorkingIndicatorAjaxLinkV5<Void> clearall = new WorkingIndicatorAjaxLinkV5<Void>("clear-all", new StringResourceModel("clear-all", this, null).getString()) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				clearAll();
				target.add(AdvancedSearchEmailSelectorPanel.this);
				fire(new FilterSelectorClearAllEvent(target));
			}
			@Override
			public String getWorkingLabel() {
				return new StringResourceModel("working", this, null).getString();
			}
		};
		
		form.add(clearall);
		**/
		
		
		DateTimeField sp = new DateTimeField("from-date", ZoneId.of(getDomain().getTimeZone()), new PropertyModel<OffsetDateTime>(this, "fromDate"), true);
		DateTimeField ep = new DateTimeField("to-date", ZoneId.of(getDomain().getTimeZone()), new PropertyModel<OffsetDateTime>(this, "toDate"), true);
		
		form.add(sp);
		form.add(ep);
				
		form.add(new TextField<String>("from", new PropertyModel<String>(this, "from")));
		form.add(new TextField<String>("to", new PropertyModel<String>(this, "to")));
		form.add(new TextField<String>("text", new PropertyModel<String>(this, "text")));
		
		form.add(new ChoiceField<String> ("result", new PropertyModel<String>(this, "result"), new PropertyModel<List<String>>(this, "results"), true) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				setResult(getValue());
			}
		});

		form.add(new EditButtonsV5<Void>(this, true) {
			@Override
			public boolean getDisableAfterSubmit() {
				return false;
			}
			@Override
			protected IModel<String> getSubmitLabel() {
				return new StringResourceModel("apply", AdvancedSearchEmailSelectorPanel.this, null);
			}
			
			@Override
			protected String getSubmitClass() {
				return "btn btn-default btn-sm";
			}
		});
		
		add(form);
	}
	
	public String getResult() {
		return this.result;
	}
	
	public void setResult(String value) {
		this.result = value;
	}
	
	public String getText() {
		return this.text;
	}
	
	public void setText(String value) {
		this.text = value;
	}
	
	public String getTo() {
		return this.to;
	}
	
	public void setTo(String value) {
		this.to = value;
	}
	
	public String getFrom() {
		return this.from;
	}
	
	public void setFrom(String value) {
		this.from = value;
	}
	

	@Override
	public  void update(AjaxRequestTarget target) {
		setEditionEnabled(true);
		fire(new FilterSelectorEvent(target, getFilters()));
		target.add(this);
	}

	
	
	public List<String> getResults() {
		List<String> list = new ArrayList<String>();
		list.add("All");
		list.add("OK");
		list.add("Error");
		return list;
	}

	public List<String> getDomains() {
		List<String> list = new ArrayList<String>();
		list.add("All");
		for (Domain domain: getContentDao().getDomains()) {
			list.add(domain.getName());
		}
		return list;
	}
	
	private Map<String, Object> getFilters() {
		
		Map<String, Object> filters  = new HashMap<String, Object>();
		
		if (getFromDate()!=null)
			filters.put("fromdate", getFromDate());
		else
			filters.put("fromdate", "null");
			
		if (getToDate()!=null)
			filters.put("todate", getToDate());
		else
			filters.put("todate", "null");
		
		if (getFrom()!=null && !"".equals(getFrom()))
			filters.put("from", getFrom());
		else
			filters.put("from", "null");
		
		if (getTo()!=null && !"".equals(getTo()))
			filters.put("to", getTo());
		else
			filters.put("to", "null");


		if (getText()!=null && !"".equals(getText()))
			filters.put("text", getText());
		else
			filters.put("text", "null");

		
		if (getResult()!=null && !"".equals(getResult()) && !"All".equals(getResult())) 
			filters.put("result", getResult());

		return filters;
		
	}

	@Override
	protected void clearAll() {
		
		setFromDate(OffsetDateTime.now().minusDays(7));
		setToDate(OffsetDateTime.now().plusDays(1).truncatedTo(ChronoUnit.DAYS));
		this.setText(null);
		this.setFrom(null);
		this.setTo(null);
		
		((Form<?>) this.get("form")).clearInput();
		
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

	
	

 	
 
}
