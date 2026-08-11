package com.novamens.content.web.admin.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.sql.SqlPlatform;
import com.novamens.kbee.sql.SqlService;
import com.novamens.kbee.wicket.markup.html.event.FilterSelectorEvent;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.form.EditButtonsV5;


/**
 * FieldText
 * FieldText
 * DateRangeSelector
 * DomainSelector
 * BoooleanSelector
 * 
 */
@SuppressWarnings("serial")
public class APIReportSelectorPanel extends ObjectEditor<APIReportFilter> {
				
	private static final long serialVersionUID = 1L;
	
	static private Logger logger = LogManager.getLogger(APIReportSelectorPanel.class.getName());

	private String query;
	List<String> dom;
	
	public APIReportSelectorPanel(String id) {
		super(id);
		setModel(new Model<APIReportFilter>(new APIReportFilter()));
		setQuery(getModelQuery());
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		setEditionEnabled(true);
		
		APIReportFilter filter = getModelObject();
					
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new TextField<String>("externalId"));
		form.add(new TextField<String>("requestText"));
		form.add(new TextField<String>("responseText"));
		form.add(new BooleanField("json"));
																		
		form.add(new ChoiceField<String> ("domain", new Model<String>(filter.getDomain()), new PropertyModel<List<String>>(this,"domainList"), true) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				APIReportSelectorPanel.this.getModelObject().setDomain(getValue());
			}
			@Override
			public boolean isVisible() {
				return isDomainKbee();
			}
		});
		
		form.add(new ChoiceField<String> ("status", new Model<String>(filter.getStatus()), new PropertyModel<List<String>>(this,"st"), true) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				APIReportSelectorPanel.this.getModelObject().setStatus(getValue());
		}});
		
											
		form.add(new ChoiceField<String> ("method", new Model<String>(filter.getStatus()), new PropertyModel<List<String>>(this,"method"), true) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				APIReportSelectorPanel.this.getModelObject().setMethod(getValue());
		}});

		
		
		
		form.add(new ChoiceField<String> ("range", new Model<String>(filter.getRange()), new PropertyModel<List<String>>(this,"range"), true) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				APIReportSelectorPanel.this.getModelObject().setRange(getValue());
			}
		});
		
		form.add(new BooleanField("closed"));
		
		form.add(new EditButtonsV5<APIReportFilter>(this, true) {
			@Override
			public boolean getDisableAfterSubmit() {
				return false;
			}
			@Override
			public IModel<String> getSubmitLabel() {
				return new StringResourceModel("apply", APIReportSelectorPanel.this, null);
			}
			@Override
			protected String getSubmitClass() {
				return "btn btn-default btn-sm";
			}
		});
		
		add(form);
	}

	/**
	 * 
	 * @return
	 */
	private String getModelQuery() {
		
		APIReportFilter filter = getModelObject();
		
		String js = ((( filter.getJson()!=null) && (filter.getJson().booleanValue())) ? ", event_request  \"Request\" ": "");
		String w_s=null;
				
		String dmc = null;
			dmc =", event_domain \"Domain\"";

		if 			(filter.getStatus().equals("All"))							w_s=null;
		else if 	(filter.getStatus().equals("200 OK"))						w_s="(event_status=200)";
		else if 	(filter.getStatus().equals("208 Older than current"))		w_s="(event_status=208)";
		
		else if 	(filter.getStatus().equals("201 - 399"))					w_s="(event_status>200 and event_status<400)";
		else if 	(filter.getStatus().startsWith("300"))						w_s="(event_status>=300 and event_status<400)";
		
		
		
		// else if 	(filter.getStatus().equals("Relevant Errors (403 412 500)")) w_s=" (event_status=403 or event_status=412 or event_status=500)";
		
		else if 	(filter.getStatus().equals("400 (all)"))							w_s="(event_status>=400 and event_status<500)";
		else if 	(filter.getStatus().equals("403"))							w_s="(event_status=403)";
		else if 	(filter.getStatus().equals("404"))							w_s="(event_status=404)";
		else if 	(filter.getStatus().equals("412"))							w_s="(event_status=412)";
		else if 	(filter.getStatus().equals("429"))							w_s="(event_status=429)";
		else if 	(filter.getStatus().equals("500"))							w_s="(event_status>=500 and event_status<600)";
//		else if 	(filter.getStatus().equals("Reprocessed with Error"))		w_s="(event_retrynumber>0 and event_status!=200 and event_retry is null and not event_closed)";
		else if 	(filter.getStatus().equals("Reprocessed with Error"))		w_s="(event_retrynumber>0 and event_status!=200 and event_retry is null and event_closed="+getSqlPlatform().getFalseValue()+")";
		else if 	(filter.getStatus().equals("All Errors"))					w_s="(event_status!=200)";
		String w_r = null;
		
//		if			(filter.getRange().equals("1 minute"))					w_r="event_time >(now() - INTERVAL '1 minute')::timestamp";
//		else if 	(filter.getRange().equals("5 minutes"))					w_r="event_time >(now() - INTERVAL '5 minute')::timestamp";
//		else if 	(filter.getRange().equals("15 minutes"))				w_r="event_time >(now() - INTERVAL '15 minute')::timestamp";
//		else if		(filter.getRange().equals("1 hour"))					w_r="event_time >(now() - INTERVAL '1 hour')::timestamp";
//		else if		(filter.getRange().equals("4 hours"))					w_r="event_time >(now() - INTERVAL '4 hour')::timestamp";
//		else if		(filter.getRange().equals("8 hours"))					w_r="event_time >(now() - INTERVAL '8 hour')::timestamp";
//		else if		(filter.getRange().equals("12 hours"))					w_r="event_time >(now() - INTERVAL '12 hour')::timestamp";
//		else if		(filter.getRange().equals("1 day"))		 			    w_r="event_time >(now() - INTERVAL '1 day')::timestamp";															
//		else if		(filter.getRange().equals("1 week"))					w_r="event_time >(now() - INTERVAL '1 week')::timestamp";
//		else if		(filter.getRange().equals("2 weeks"))					w_r="event_time >(now() - INTERVAL '2 week')::timestamp";
//		else if		(filter.getRange().equals("1 month"))					w_r="event_time >(now() - INTERVAL '1 month')::timestamp";
//		else if		(filter.getRange().equals("2 months"))					w_r="event_time >(now() - INTERVAL '2 month')::timestamp";
//		else if		(filter.getRange().equals("3 months"))					w_r="event_time >(now() - INTERVAL '3 month')::timestamp";											
//		else if		(filter.getRange().equals("6 months"))					w_r="event_time >(now() - INTERVAL '6 months')::timestamp";
//		else if		(filter.getRange().equals("1 year"))					w_r="event_time >(now() - INTERVAL '1 year')::timestamp";											
//		else if		(filter.getRange().equals("All"))						w_r=null;
		
		if			(filter.getRange().equals("1 minute"))					w_r="event_time >("+ getSqlPlatform().getCurrentTimestamp() + " - INTERVAL '1' minute)";
		else if 	(filter.getRange().equals("5 minutes"))					w_r="event_time >("+ getSqlPlatform().getCurrentTimestamp() + " - INTERVAL '5' minute)";
		else if 	(filter.getRange().equals("15 minutes"))				w_r="event_time >("+ getSqlPlatform().getCurrentTimestamp() + " - INTERVAL '15' minute)";
		else if		(filter.getRange().equals("1 hour"))					w_r="event_time >("+ getSqlPlatform().getCurrentTimestamp() + " - INTERVAL '1' hour)";
		else if		(filter.getRange().equals("4 hours"))					w_r="event_time >("+ getSqlPlatform().getCurrentTimestamp() + " - INTERVAL '4' hour)";
		else if		(filter.getRange().equals("8 hours"))					w_r="event_time >("+ getSqlPlatform().getCurrentTimestamp() + " - INTERVAL '8' hour)";
		else if		(filter.getRange().equals("12 hours"))					w_r="event_time >("+ getSqlPlatform().getCurrentTimestamp() + " - INTERVAL '12' hour)";
		else if		(filter.getRange().equals("1 day"))		 			    w_r="event_time >("+ getSqlPlatform().getCurrentTimestamp() + " - INTERVAL '1' day)";															
											
		else if		(filter.getRange().equals("2 days"))		 			    w_r="event_time >("+ getSqlPlatform().getCurrentTimestamp() + " - INTERVAL '2' day)";
		else if		(filter.getRange().equals("3 days"))		 			    w_r="event_time >("+ getSqlPlatform().getCurrentTimestamp() + " - INTERVAL '3' day)";
		
		else if		(filter.getRange().equals("1 week"))					w_r="event_time >("+ getSqlPlatform().getCurrentTimestamp() + " - INTERVAL '7' day)";
		else if		(filter.getRange().equals("2 weeks"))					w_r="event_time >("+ getSqlPlatform().getCurrentTimestamp() + " - INTERVAL '14' day)";
		else if		(filter.getRange().equals("1 month"))					w_r="event_time >("+ getSqlPlatform().getCurrentTimestamp() + " - INTERVAL '1' month)";
		else if		(filter.getRange().equals("2 months"))					w_r="event_time >("+ getSqlPlatform().getCurrentTimestamp() + " - INTERVAL '2' month)";
		else if		(filter.getRange().equals("3 months"))					w_r="event_time >("+ getSqlPlatform().getCurrentTimestamp() + " - INTERVAL '3' month)";											
		else if		(filter.getRange().equals("6 months"))					w_r="event_time >("+ getSqlPlatform().getCurrentTimestamp() + " - INTERVAL '6' month)";
		else if		(filter.getRange().equals("1 year"))					w_r="event_time >("+ getSqlPlatform().getCurrentTimestamp() + " - INTERVAL '1' year)";											
		else if		(filter.getRange().equals("All"))						w_r=null;
		
		String w_d = null;
		if (filter.getDomain().equals("All"))						
			w_d=null;
		else
			w_d="event_domain='"+filter.getDomain()+"'";
		
		String wer = "";
		
		StringBuilder ttt = new StringBuilder();
		boolean requires_and = false;
		
		String qeid = filter.getExternalId();
		String w_eid = null;
		if (qeid!=null && qeid.length()>0) {
			w_eid = "event_file='"+qeid+"'";
		}
		
		String qreqtxt = filter.getRequestText();
		String w_reqtxt = null;
		if (qreqtxt!=null && qreqtxt.length()>0) {
			w_reqtxt = "event_request like'%"+qreqtxt.trim()+"%'";
		}
		
		String qrestxt = filter.getResponseText();
		String w_restxt = null;
		if (qrestxt!=null && qrestxt.length()>0) {
			w_restxt = "event_response like'%"+qrestxt.trim()+"%'";
		}
		
		boolean closed = filter.getClosed();
				
		if (w_s !=null || w_r !=null || w_d!=null || w_eid!=null || w_reqtxt!=null || w_restxt!=null) {
		
			ttt.append(" where ");
			
			if (w_s!=null) {
				ttt.append(w_s);
				requires_and=true;
			}
			
			if (w_r!=null) {
				if (requires_and)
					ttt.append(" and ");	
				ttt.append(w_r);
				requires_and=true;
			}
			
			if (w_d!=null) {
				if (requires_and)
					ttt.append(" and ");
				ttt.append(w_d);
				requires_and=true;
			}
			
			if (w_eid!=null) {
				if (requires_and)
					ttt.append(" and ");
				ttt.append(w_eid);
				requires_and=true;
			}
			
			if (w_reqtxt!=null) {
				if (requires_and)
					ttt.append(" and ");
				ttt.append(w_reqtxt.trim());
				requires_and=true;
			}
			
			if (w_restxt!=null) {
				if (requires_and)
					ttt.append(" and ");
				ttt.append(w_restxt.trim());
				requires_and=true;
			}
		}
		
		if (requires_and)
			ttt.append(" and ");
		else 
			if (ttt.length()<=0) {
				ttt.append(" where ");
			}
//		ttt.append(closed ? " event_closed " : " not event_closed ");
		ttt.append(" event_closed="+(closed?getSqlPlatform().getTrueValue():getSqlPlatform().getFalseValue()));
//		requires_and=true;
		
		if (ttt.length()>0)
			wer=ttt.toString();
		else
			wer="";
																				
		String sq = "select EVENT_TIME \"Date\" " +  dmc + ", EVENT_USER \"User\", EVENT_METHOD \"Method\",  EVENT_STATUS \"Status\", EVENT_RESPONSE \"Response\", event_file \"File\", EVENT_FILESOURCE \"File Source\", EVENT_URI \"URI\",  EVENT_TRANSACTION \"Transaction\",  EVENT_CONTENTCLASS \"Content Class\", EVENT_PROCESSING_TIME \"Time (ms)\", EVENT_RETRYNUMBER \"Retry\", EVENT_SOURCE \"Source Event\"   " + js  +" from api_logevent " + wer +  " order by event_time desc";
		
		logger.debug(sq);
		return sq;
	}
	
	@Override
	public  void update(AjaxRequestTarget target) {
		String sq = getModelQuery();
		
		setQuery(sq);
		logger.debug(sq);
		
		setEditionEnabled(true);
		target.add(this);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("query", sq);
		
		fire(new FilterSelectorEvent(target, map));
	}

	
	public List<String> getMethod() {
		List<String> list = new ArrayList<String>();
		list.add("ALL");
		list.add("DELETE");
		list.add("GET");
		list.add("PATCH");
		list.add("POST");
		list.add("PUT");
		return list;
	}

	
	public List<String> getSt() {
		List<String> list = new ArrayList<String>();
		list.add("All");
		list.add("200 OK");
		list.add("201 - 399");
		list.add("208 Older than current");
		list.add("300 (all)");
		list.add("400 (all)");
		list.add("400 (all but 404)");
		list.add("403");
		list.add("404");
		list.add("412");
		list.add("429");
		list.add("500");

		// list.add("Relevant Errors (403 412 500)");
		
		list.add("Reprocessed with Error");
		list.add("All Errors");
		return list;
	}
	
	public List<String> getRange() {
		List<String> list = new ArrayList<String>();
		list.add("1 minute");
		list.add("5 minutes");
		list.add("15 minutes");
		list.add("1 hour");
		list.add("4 hours");
		list.add("8 hours");
		list.add("12 hours");
		
		list.add("1 day");
		list.add("2 days");
		list.add("3 days");
		
		list.add("1 week");
		list.add("2 weeks");
		list.add("1 month");
		list.add("2 months");
		list.add("3 months");
		list.add("6 months");
		list.add("1 year");
		list.add("All");
									
		return list;
	}
	
	public List<String> getDomainList() {
		if (dom!=null)
			return dom;
		
		dom = new ArrayList<String>();
		dom.add("All");
		
		for (Domain domain: getContentDao().getDomains())
			dom.add(domain.getName());
		
		Collections.sort(dom);
		return dom;
	}

	@Override
	public IModel<String> getSubmitLabel() {
		return new StringResourceModel("apply", APIReportSelectorPanel.this, null);
	}
	
	
	
	private SqlPlatform getSqlPlatform() {
		return ServiceLocator.getService(SqlService.class).getSqlPlatform();
	}
	
	private boolean isDomainKbee() {
		try {
			return getPerson().getDomain().getName().toLowerCase().trim().equals("kbee");
		} 
		catch (Exception e) {
			logger.error(" isDomainKbee " + e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			return false;
		}
	}
}
