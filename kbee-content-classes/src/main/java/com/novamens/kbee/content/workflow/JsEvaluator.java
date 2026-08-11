package com.novamens.kbee.content.workflow;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.novamens.content.base.Content;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.JavaIndex;
import com.novamens.kbee.content.document.KbeeIDoc;
import com.novamens.kbee.content.script.KbeeClassificableScriptWrapper;
import com.novamens.kbee.content.script.KbeeServiceLocator;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.iql.SolrMemberDao;
import com.novamens.workflow.RoutingException;
import com.novamens.workflow.WorkflowContext;

public class JsEvaluator {
	private String script;
	private ScriptEngine engine = null;
	
	public JsEvaluator(String script) {
		setScript(script);
	}
	
	public Object evaluate(WorkflowContext context) {
		try {
			//KbeeContext kbeecontext = (KbeeContext)context;
			//Content content = kbeecontext.getContent();
			//KbeeProcedure procedure = (KbeeProcedure)kbeecontext.getProcedure();

 			setBindings(context);
			Object evaluation = getEngine().eval(getScript());
			return evaluation;
		}
		catch (ScriptException e) {
			throw new RoutingException(e.getMessage());
		}
	}
	
	public String validate(ContentTemplate template) {
		try {
			setBindings(template);
			getEngine().eval(getScript());
			return null;
		}
		catch (ScriptException e) {
			return e.getMessage();
		}
	}
	
	public void setScript(String script) {
		this.script = script;
	}
	
	public String getScript() {
		return this.script;
	}
	
	public static String GetHelpText(ContentTemplate template) {
		StringBuilder text = new StringBuilder();
		//String numberexample = null, booleanexample=null;
		text.append("<div class=\"panel col-lg-12\">");
		text.append("<p class=\"text col-lg-12\">Script can be write using this context variables:</p>");
		text.append("<ul class=\"col-lg-12 panel\" style=\"margin-top: 10px;\">");
		for (AttributeTemplate attributetemplate : template.getAttributes()) {
			Attribute attribute = attributetemplate.getAttribute();
			if (attribute.getAlias()!=null) {
				if (attribute.getType().equals(AttributeType.BOOLEAN)) {
					text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
					//booleanexample=attribute.getAlias();
					text.append(attribute.getAlias());
					text.append("<span class=\"ago\"> ( Boolean ) </span>");
					text.append("</li>");
				}
				if (attribute.getType().equals(AttributeType.NUMBER)) {
					text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
					//numberexample = attribute.getAlias();
					text.append(attribute.getAlias());
					text.append("<span class=\"ago\"> ( Number ) </span>");
					text.append("</li>");
				}
				if (attribute.getType().equals(AttributeType.STRING)) {
					text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
					text.append(attribute.getAlias());
					text.append("<span class=\"ago\"> ( String ) </span>");
					text.append("</li>");
				}
				if (attribute.getType().equals(AttributeType.DATE)) {
					text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
					text.append(attribute.getAlias());
					text.append("<span class=\"ago\"> ( OffsetDateTime ) </span>");
					text.append("</li>");
				}
			}
		}
		
		for (ClassifierTemplate classifiertemplate : template.getClassifiers()) {
			Classifier classifier = classifiertemplate.getClassifier();
			if (classifier.getAlias()!=null) {
				text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
				text.append(classifier.getAlias());
				text.append("<span class=\"ago\"> ( String ) </span>");
				text.append("</li>");
			}
		}
		
		text.append("<li class=\"col-lg-12\" style=\"padding-top:10px;\"><span class=\"predicate\">");
		text.append("content");
		text.append("<span class=\"ago\"> ( Content ) </span>");
		text.append("</li>");
		
		text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
		text.append("workflowservice");
		text.append("<span class=\"ago\"> ( WorkflowService ) </span>");
		text.append("</li>");
		
		text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
		text.append("context");
		text.append("<span class=\"ago\"> ( WorkflowContext ) </span>");
		text.append("</li>");
		
		text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
		text.append("servicelocator");
		text.append("<span class=\"ago\"> ( ServiceLocator ) </span>");
		text.append("</li>");
		
		text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
		text.append("valuesdao");
		text.append("<span class=\"ago\"> ( Values Dao ) </span>");
		text.append("</li>");
		
		text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
		text.append("today");
		text.append("<span class=\"ago\"> ( OffsetDateTime ) </span>");
		text.append("</li>");
		
		text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
		text.append("user");
		text.append("<span class=\"ago\"> ( User ) </span>");
		text.append("</li>");

		text.append("</ul>");
//		text.append("<p class=\"text col-lg-12\">Examples:</p>");
//		if (numberexample!=null) {
//			text.append("<p class=\"text col-lg-12\">if ("+numberexample+">100) 'Task1'; else 'Task2'</p>");
//		}
//		if (booleanexample!=null) {
//			text.append("<p class=\"text col-lg-12\">if ("+booleanexample+") 'Task1'; else 'Task2'</p>");
//		}
		text.append("</br>");
		//text.append("<p class=\"text col-lg-12\">Variables are taken from the attributes and classifiers of the content template with non-null aliases</p>");
		text.append("</div>");
		return text.toString();
	}
	
	private void setBindings(WorkflowContext context) {
		KbeeContext kbeecontext = (KbeeContext)context;
		Content content = kbeecontext.getContent();
		Bindings bindings = getEngine().getBindings(ScriptContext.ENGINE_SCOPE);
		
		for (AttributeTemplate template : content.getContentTemplate().getAttributes()) {
			Attribute attribute = template.getAttribute();
			if (attribute.getAlias()!=null) {
				List<String> values = content.getAttributeValues(attribute);
				if (attribute.getType().equals(AttributeType.BOOLEAN)) {
					Boolean value = !values.isEmpty() ? ("true".equals(values.get(0)) ? true : false) : null;
					bindings.put(attribute.getAlias(), value);
				}
				if (attribute.getType().equals(AttributeType.NUMBER)) {
					Integer value = !values.isEmpty() ? Integer.valueOf(values.get(0)) : null;
					bindings.put(attribute.getAlias(), value);
				}
				if (attribute.getType().equals(AttributeType.STRING)) {
					String value = !values.isEmpty() ? values.get(0) : null;
					bindings.put(attribute.getAlias(), value);
				}
				if (attribute.getType().equals(AttributeType.DATE)) {
					DateTimeService dateservice  = ServiceLocator.getService(DateTimeService.class);
					String value = !values.isEmpty() ? values.get(0) : null;
					OffsetDateTime date = dateservice.parseStrDate(value);
					if (date!=null)
					bindings.put(attribute.getAlias(), date);
				}
			}
		}
		
		for (ClassifierTemplate template : content.getContentTemplate().getClassifiers()) {
			Classifier classifier = template.getClassifier();
			if (classifier.getAlias()!=null) {
				List<Classification> values = content.getClassification(classifier);
				DataSetMember member = !values.isEmpty() ? values.get(0).getDataSetMember() : null;
				String value = member!=null ? member.getDisplayName() : null;
				bindings.put(classifier.getAlias(), value);
			}
		}
		
		bindings.put("content", new KbeeClassificableScriptWrapper(content));
		
		bindings.put("workflowservice",  content.getService(WorkflowService.class));
		
		bindings.put("servicelocator",  new KbeeServiceLocator());
		
		if (context.getInitialData()!=null)
			bindings.put("initialdata",  context.getInitialData());
		
		bindings.put("context",  kbeecontext);
		
		bindings.put("user",  getSessionUser());
		
		SolrMemberDao dao = new SolrMemberDao();
		Domain domain = content.getDomain();
		dao.setDomain(domain);
		dao.setIndex((JavaIndex)domain.getService(JavaIndexerService.class).getIndex());
		bindings.put("valuesdao",  dao);
		
		bindings.put("today", getToday());
	}
	
	private void setBindings(ContentTemplate template) {
		Bindings bindings = getEngine().getBindings(ScriptContext.ENGINE_SCOPE);
		
		for (AttributeTemplate attributetemplate : template.getAttributes()) {
			Attribute attribute = attributetemplate.getAttribute();
			if (attribute.getType().equals(AttributeType.BOOLEAN)) {
				bindings.put(attribute.getAlias(), null);
			}
			if (attribute.getType().equals(AttributeType.NUMBER)) {
				bindings.put(attribute.getAlias(), null);
			}
			if (attribute.getType().equals(AttributeType.STRING)) {
				bindings.put(attribute.getAlias(), null);
			}
		}
		
		for (ClassifierTemplate classifiertemplate : template.getClassifiers()) {
			Classifier classifier = classifiertemplate.getClassifier();
			bindings.put(classifier.getAlias(), null);
		}
		
		SolrMemberDao dao = new SolrMemberDao();
		Domain domain = template.getDomain();
		dao.setDomain(domain);
		dao.setIndex((JavaIndex)domain.getService(JavaIndexerService.class).getIndex());
		bindings.put("valuesdao",  dao);
		
		bindings.put("content", new KbeeClassificableScriptWrapper(new KbeeIDoc(template)));
		
		KbeeContext context = new KbeeContext(null);
		bindings.put("context", context);
		
		bindings.put("user",  getSessionUser());
		
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("sourcecontent", new KbeeClassificableScriptWrapper(new KbeeIDoc(template)));
		bindings.put("initialdata", data);

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
	
	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private ScriptEngine getEngine() {
		if (engine==null) {
			engine = new ScriptEngineManager().getEngineByName("JavaScript");
		}
		return engine;
	}
}
