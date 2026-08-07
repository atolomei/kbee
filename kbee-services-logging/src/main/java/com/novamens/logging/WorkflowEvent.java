package com.novamens.logging;

import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Transient;

import com.codesnippets4all.json.generators.JSONGenerator;
import com.codesnippets4all.json.generators.JsonGeneratorFactory;
import com.codesnippets4all.json.parsers.JSONParser;
import com.codesnippets4all.json.parsers.JsonParserFactory;
import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ContentId;
import com.novamens.content.model.ObjectId;
import com.novamens.content.workflow.WorkflowDao;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.service.ServiceLocator;

@Entity
public abstract class WorkflowEvent extends ContentEvent {
																									
	@Transient
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(WorkflowEvent.class.getName());
	
	@Transient
	private transient KbeeJson json;

	public WorkflowEvent() {
	}
	
	public WorkflowEvent(Content content) {
		super(content);
	}
	
	@Override
	public Object getContent() {
		ObjectId oid = new ObjectId(this.getObjectId());
		ContentId cid = new ContentId(oid.getClassName(), oid.getId());
		return getContentDao().findContentById(cid);
	}
	
	@Override
	public String getType() {
		return "Content";
	}
	
	@Override
	public String getObjectClass() {
		return "Content"; // o lo que sea !!!  VER
	}
	
	@Override
	public String getAction() {
		return "Workflow";
	}
	
	@SuppressWarnings("rawtypes")
	public KbeeJson getJson() {
		if (json == null) {
			try {
				JsonParserFactory factory = JsonParserFactory.getInstance();
				JSONParser parser = factory.newJsonParser();
				String parameters = super.getParameters();
				Map roots = parser.parseJson(parameters);
				List root = (List)roots.get("root");
				Map jsonData = (Map)root.get(0);
				json = new KbeeJson(jsonData);
			} 
			catch (com.codesnippets4all.json.exceptions.JSONParsingException e) {
				logger.error(e);
				json = new KbeeJson();
			}
		}
		return json;
	}

	public String getCondition() {
		return "condition";
	}
	
	@Override
	public boolean isNotifiable() {
		return true;
	}
	
	protected void setJson(KbeeJson json) {
		try {
			JsonGeneratorFactory factory = JsonGeneratorFactory.getInstance();
			JSONGenerator generator = factory.newJsonGenerator();
			String jsonstring = generator.generateJson(json.getData());
			setParameters(jsonstring);
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	protected String escape(String value) {
		value = value!=null? value.replace("\"", "\\'") : null;
		return value;
	}
	
	protected String unescape(String value) {
		value = value!=null ? value.replace("\\'", "\"") : null;
		return value;
	}
	
	protected ContentDao getContentDao() {
		 BeansService beans = ServiceLocator.getService(BeansService.class);
		 return (ContentDao) beans.getBean("contentDao");
	}
	
	protected WorkflowDao getWorkflowDao() {
		return (WorkflowDao)ServiceLocator.getService(BeansService.class).getBean("WorkflowDao");
	}

}
