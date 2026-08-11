package com.novamens.kbee.content.form;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

import com.novamens.content.base.Content;
import com.novamens.content.form.EClassifierModel;
import com.novamens.content.form.EDateField;
import com.novamens.content.form.EFieldModel;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.ENumberField;
import com.novamens.content.form.EStringField;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.JavaIndex;
import com.novamens.kbee.content.script.KbeeClassificableScriptWrapper;
import com.novamens.kbee.content.script.KbeeUserScriptWrapper;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.iql.SolrMemberDao;

import kbee.util.logging.Logger;

public class ScriptEvaluator {
	
	private ScriptEngine engine = null;
	private static Logger logger = Logger.getLogger(ScriptEvaluator.class.getName());

	private Map<String, Object> bindings = new HashMap<String, Object>();

	public Object evaluate(String condition, EFormData data) {
		try {
			if (data!=null) {
				setBindings(data);
				Object evaluation = getEngine().eval(condition);
				return evaluation;
			}
			else {
				return null;
			}
		}
		catch (ScriptException e) {
			logger.error(e);
			return "false";
		}
	}
	
	public void setBinding(EFormField<?> field, Object value) {
		this.bindings.put(field.getName(), value);
	}
	
	public void setBinding(String variable, Object value) {
		this.bindings.put(variable, value);
	}
	
	public static String GetHelpText(EForm eform) {
		StringBuilder text = new StringBuilder();
		text.append("<div class=\"panel col-lg-12\">");
		text.append("<p class=\"text col-lg-12\">Script can be write using this context variables:</p>");
		text.append("<ul class=\"col-lg-12 panel\" style=\"margin-top: 10px;\">");
		for (EFormField<?> field : eform.getFields()) {
			EFieldModel<?> model = field.getModel();
			if (model!=null && field.getName()!=null) {
				if (model instanceof EClassifierModel) {
					text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
					text.append(field.getName());
					text.append("<span class=\"ago\"> ( DataSet Value ) </span>");
					text.append("</li>");
				}
				if (field instanceof ENumberField) {
					text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
					text.append(field.getName());
					text.append("<span class=\"ago\"> ( Number ) </span>");
					text.append("</li>");
				}
				if (field instanceof EDateField) {
					text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
					text.append(field.getName());
					text.append("<span class=\"ago\"> ( Date ) </span>");
					text.append("</li>");
				}
				if (field instanceof EStringField) {
					text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
					text.append(field.getName());
					text.append("<span class=\"ago\"> ( String ) </span>");
					text.append("</li>");
				}
			}
		}
		text.append("<li class=\"col-lg-12\" style=\"padding-top:10px;\"><span class=\"predicate\">");
		text.append("content");
		text.append("<span class=\"ago\"> ( Content ) </span>");
		text.append("</li>");
		
		text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
		text.append("workflowservice");
		text.append("<span class=\"ago\"> ( Workflow Service ) </span>");
		text.append("</li>");
		
		text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
		text.append("valuesdao");
		text.append("<span class=\"ago\"> ( Values Dao ) </span>");
		text.append("</li>");
		
		text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
		text.append("user");
		text.append("<span class=\"ago\"> ( User ) </span>");
		text.append("</li>");
		
		text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
		text.append("today");
		text.append("<span class=\"ago\"> ( Date ) </span>");
		text.append("</li>");
		
		text.append("</ul>");
		text.append("</br>");
		text.append("</div>");
		return text.toString();
	}
	
	private void setBindings(EFormData formdata) {
		Bindings bindings = getEngine().getBindings(ScriptContext.ENGINE_SCOPE);

		for (EFormField<?> field : formdata.getForm().getFields()) {
			Object data = this.bindings.get(field.getName());
			if (data==null) data = formdata.getData(field);
			if (data!=null) {
				
				if (data instanceof HibernateProxy) {
					HibernateProxy proxy = (HibernateProxy)data;
					LazyInitializer initializer = proxy.getHibernateLazyInitializer();
					data = initializer.getImplementation();
				}
				if (data instanceof DataSetMember) {
					bindings.put(field.getName(), new KbeeClassificableScriptWrapper((DataSetMember)data));
				}
				else {
					bindings.put(field.getName(), data);
				}
			}
		}
		Content content = null;
		if (formdata instanceof EFormContentData) {
			content = ((EFormContentData)formdata).getContent();
			if (content!=null) {
				bindings.put("content", new KbeeClassificableScriptWrapper(content));
			}
		}
		if (content!=null) {
			bindings.put("workflowservice",  content.getService(WorkflowService.class));
			SolrMemberDao dao = new SolrMemberDao();
			Domain domain = content.getDomain();
			dao.setDomain(domain);
			dao.setIndex((JavaIndex)domain.getService(JavaIndexerService.class).getIndex());
			bindings.put("valuesdao",  dao);
		}

		for (String variable : this.bindings.keySet()) {
			bindings.put(variable, this.bindings.get(variable));
		}
		bindings.put("today", getToday());
		bindings.put("user", new KbeeUserScriptWrapper(getSessionUser()));
	}	
	
	private ScriptEngine getEngine() {
		if (engine==null) {
			engine = new ScriptEngineManager().getEngineByName("JavaScript");
		}
		return engine;
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private OffsetDateTime getToday() {
		OffsetDateTime today = OffsetDateTime.now();
		today = OffsetDateTime.of(today.getYear(),
			today.getMonthValue(),
			today.getDayOfMonth(),
			0,
			0,
			0,
			0,
			ZoneOffset.from(today));
		return today;
	}
}