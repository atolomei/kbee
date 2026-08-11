package com.novamens.kbee.content.service;

import java.time.OffsetDateTime;

import com.novamens.content.base.Content;
import com.novamens.content.document.IDoc;
import com.novamens.content.entity.Person;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.service.TokenService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.service.ServiceLocator;

public class KbeeContentUrlService extends KbeeAbstractUrlService {
												
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeContentUrlService.class.getName());
	
	private Content content;
	
	public KbeeContentUrlService() {
	}

	public KbeeContentUrlService(Content content) {
		this.content = content;
	}
	
	@Override
	public String getRelativeUrl() {
		return getUrl(false);
	}
	
	@Override
	public String getUrl() {
		return getUrl(true);
	}
	
	@Override
	public String getUrl(boolean include_server) {
		
		if (getContent().getWorkspace()!=null)
			return getTaskUrl(include_server);
		
		if (getContent().isHeadVersion())
			return (include_server ? (getServerUrl() + "/") : "/" ) + IDoc.CLASS_CODE + "/" + String.valueOf(getContent().getOId());
		
		return (include_server ? (getServerUrl() + "/") : "/" )  + IDoc.CLASS_CODE + "/" + String.valueOf(getContent().getOId())+"/"+ String.valueOf( getContent().getVersion()) + "/"+String.valueOf(getContent().getId());
	}
	
	
	public String getTaskUrl() {
		KbeeContext context = (KbeeContext)getContent().getService(WorkflowService.class).getContext();
		
		if (context==null) 
			return null;
		
		if (context.getTask()==null)
			return null;
		
		String taskid = context.getTask().getId().replaceAll("\\s", "-").toLowerCase();
		String url = getServerUrl() + "/task/" + getContent().getClassCode()+ "/v6/"	+ taskid + "/" + String.valueOf(content.getId());
		return url;
	}
	
	
	/**
	 * PONER EL TIMESTAMP
	 */
	public String getPublicTaskUrl() {
		
		KbeeContext context = (KbeeContext)getContent().getService(WorkflowService.class).getContext();
		
		if (context==null) 
			return null;
		
		KbeeJson data = new KbeeJson();
		
		data.put("content", String.valueOf(content.getId()));
		String taskid = context.getTask().getId().replaceAll("\\s", "-").toLowerCase();
		data.put("task", taskid);
		data.put("user", context.getCurrentActivity().getUser().getName());
		data.put("expiration", ServiceLocator.getService(DateTimeService.class).getStr_ISO_OFFSET_DATE_TIME(OffsetDateTime.now().plusDays(7)));

		return getServerUrl() + "/sharedtask/" + ServiceLocator.getService(TokenService.class).getToken(data);
	}

	@Override
	public String getPublicUrl() {
		return getPublicUrl(null, null);
	}
	

	@Override
	public String getPublicUrl(String password) {
		return getPublicUrl(null, password);
	}

	@Override
	public String getPublicUrl(Person person) {
		return getPublicUrl(person, null);
	}
	
	@Override
	public String getPublicUrl(Person person, String password) {
	
		KbeeJson data = new KbeeJson();
		
		data.put("id", String.valueOf(content.getId()));
		data.put("oid", String.valueOf(content.getOId()));
		
		if (person!=null)
			data.put("person", String.valueOf(person.getId()));
		
		if (password!=null)
			data.put("password", password);
		
		data.put("date", content.getCreationOffsetDateTime().toString());
		data.put("domain", String.valueOf(content.getDomain().getId()));
		
		String tokenversion = (String)content.getService(PropertyService.class).getProperty("token");
		if (tokenversion==null) {
			content.getService(PropertyService.class).updateProperty("token", "1");
			tokenversion = "1";
		}
		data.put("token", tokenversion);
	
		WorkflowService ws = content.getService(WorkflowService.class);

		if (ws!=null && ws.active()) {
			data.put("process", String.valueOf(ws.getContext().getProcess().getId()));
		}
		
		logger.debug(data.toString());
		
		return getServerUrl() + "/shared/" + ServiceLocator.getService(TokenService.class).getToken(data);
	
	}
	

	
	public Content getContent() {
		return content;
	}
	
	protected Domain getDomain() {
		return getContent().getDomain();
	}
	
	private String getTaskUrl(boolean include_server) {
		try {
			WorkflowService workflowService = getContent().getService(WorkflowService.class);
			return ( include_server ? (getServerUrl()+"/")  :"/" ) + "task/"+IDoc.CLASS_CODE+"/v6/"+workflowService.getTask().getId().replaceAll("\\s", "-").toLowerCase().trim() + "/" + getContent().getId();
		} 
		catch (Exception e) {
				logger.error(e);
			return null;
		}
	}
}