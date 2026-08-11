package com.novamens.content.web.admin.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.dom.Domain;
import com.novamens.kbee.wicket.markup.html.event.FilterSelectorEvent;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.form.EditButtonsV5;

public class APISOAPReportSelectorPanel extends ObjectEditor<APISOAPReportFilter> {
				
	private static final long serialVersionUID = 1L;

	
	static private Logger logger = LogManager.getLogger(APISOAPReportSelectorPanel.class.getName());

	private String query;
	
	public APISOAPReportSelectorPanel(String id) {
		super(id);
		setOutputMarkupId(true);
		setModel(new Model<APISOAPReportFilter>(new APISOAPReportFilter()));
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
					
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new TextField<String>("externalId"));
		form.add(new TextField<String>("text"));
		form.add(new BooleanField("json"));
																		
		form.add(new ChoiceField<String> ("domain", new Model<String>(getModel().getObject().getDomain()), new PropertyModel<List<String>>(this,"domain"), true) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				 APISOAPReportSelectorPanel.this.getModel().getObject().setDomain(getValue());
			}
			@Override
			public boolean isVisible() {
				return isDomainKbee();
			}
		});
		
		
		form.add(new ChoiceField<String> ("status", new Model<String>(getModel().getObject().getStatus()), new PropertyModel<List<String>>(this,"st"), true) {
				/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

				@Override
				public void onUpdate(AjaxRequestTarget target) {
					 APISOAPReportSelectorPanel.this.getModel().getObject().setStatus(getValue());
				}});
		
		
		form.add(new ChoiceField<String> ("range", new Model<String>(getModel().getObject().getRange()), new PropertyModel<List<String>>(this,"range"), true) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void onUpdate(AjaxRequestTarget target) {
				 APISOAPReportSelectorPanel.this.getModel().getObject().setRange(getValue());
			}
		});
		
		form.add(new EditButtonsV5<APISOAPReportFilter>(this, true) {

			private static final long serialVersionUID = 1L;
			
			@Override
			public boolean getDisableAfterSubmit() {
				return false;
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
		
		String js = ((( getModel().getObject().getJson()!=null) && (getModel().getObject().getJson().booleanValue())) ? ", event_request \"Request\" ": "");
		String w_s=null;
		
		if 				(getModel().getObject().getStatus().equals("All"))					w_s=null;
		else if 		(getModel().getObject().getStatus().equals("200 OK"))				w_s="event_status=200";
		else if 		(getModel().getObject().getStatus().equals("201+ and 300s"))		w_s="event_status>200 and event_status<400";
		else if 		(getModel().getObject().getStatus().equals("400"))					w_s="event_status>=400 and event_status<500";
		else if 		(getModel().getObject().getStatus().equals("400 (less 404)"))		w_s="event_status>=400 and event_status<500 and event_status!=404";
		else if 		(getModel().getObject().getStatus().equals("500"))					w_s="event_status>=500 and event_status<600";
		else if 		(getModel().getObject().getStatus().equals("All Errors"))			w_s="event_status!=200";
		
		String w_r = null;
		
		if				(getModel().getObject().getRange().equals("1m"))					w_r="event_time >(now() - INTERVAL '1 minute')::timestamp";
		else if 		(getModel().getObject().getRange().equals("5m"))					w_r="event_time >(now() - INTERVAL '5 minute')::timestamp";
		else if			(getModel().getObject().getRange().equals("1h"))					w_r="event_time >(now() - INTERVAL '1 hour')::timestamp";
		else if			(getModel().getObject().getRange().equals("1d"))					w_r="event_time >(now() - INTERVAL '1 day')::timestamp";															
		else if			(getModel().getObject().getRange().equals("1w"))					w_r="event_time >(now() - INTERVAL '1 week')::timestamp";															
		else if			(getModel().getObject().getRange().equals("1M"))					w_r="event_time >(now() - INTERVAL '1 month')::timestamp";															
		else if			(getModel().getObject().getRange().equals("3M"))					w_r="event_time >(now() - INTERVAL '3 month')::timestamp";											
		else if			(getModel().getObject().getRange().equals("1y"))					w_r="event_time >(now() - INTERVAL '1 year')::timestamp";											
		else if			(getModel().getObject().getRange().equals("All"))					w_r=null;
							
		String w_d = null;
		if 			(getModel().getObject().getDomain().equals("All"))						
			w_d=null;
		else
			w_d="event_domain='"+getModel().getObject().getDomain()+"'";
		
		String wer = "";
		
		StringBuilder ttt = new StringBuilder();
		boolean requires_and = false;
		
		
		String qeid = getModel().getObject().getExternalId();
		String w_eid = null;
		if (qeid!=null && qeid.length()>0) {
			w_eid = "event_file='"+qeid+"'";
		}
		
		
		String qtxt = getModel().getObject().getText();
		String w_txt = null;
		if (qtxt!=null && qtxt.length()>0) {
			w_txt = "event_request like'%"+qtxt+"%'";
		}
		
		
		if (w_s !=null || w_r !=null || w_d!=null || w_eid!=null || w_txt!=null) {
		
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
			
			if (w_txt!=null) {
				if (requires_and)
					ttt.append(" and ");
				ttt.append(w_txt);
				requires_and=true;
			}
		}
		
		if (ttt.length()>0)
			wer=ttt.toString();
		else
			wer="";
		
		String sq = "select event_time \"Date\", event_domain \"Domain\", EVENT_USER \"User\",  EVENT_METHOD \"method\", event_file \"File\", EVENT_TRANSACTION \"Trx\", EVENT_URI \"uri\", EVENT_STATUS \"Status\", EVENT_RESPONSE \"Response\" " + js  +" from api_soapevent " + wer +  " order by event_time desc";
		return sq.toLowerCase();
	}

	/**
	 * 
	 */
	@Override
	public  void update(AjaxRequestTarget target) {
					
	String sq = getModelQuery();
		
		setQuery(sq);
		logger.info(sq);
		
		setEditionEnabled(true);
		target.add(this);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("query", sq);
		
		fire(new FilterSelectorEvent(target, map));
		
	}
	
	public List<String> getSt() {
		List<String> list = new ArrayList<String>();
		list.add("All");
		list.add("200 OK");
		list.add("201+ and 300s");
		list.add("400");
		list.add("400 (less 404)");
		list.add("500");
		list.add("All Errors");
		return list;
	}
	
	
	public List<String> getRange() {
		List<String> list = new ArrayList<String>();
		list.add("1m");
		list.add("5m");
		list.add("1h");
		list.add("1d");
		list.add("1w");
		list.add("1M");
		list.add("3M");
		list.add("1y");
		list.add("All");
		
		return list;
	}
	
	
	public List<String> getDomainList() {
		List<String> list = new ArrayList<String>();
		list.add("All");
		for (Domain domain: getContentDao().getDomains()) {
			list.add(domain.getName());
		}
		return list;
	}

	
	private boolean isDomainKbee() {
		try {
			return getPerson().getDomain().getName().toLowerCase().trim().equals("kbee");
		} 
		catch (Exception e) {
			logger.error(" isDomainKbee " + e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			logger.error(e.getStackTrace());
			return false;
		}
	}

}
