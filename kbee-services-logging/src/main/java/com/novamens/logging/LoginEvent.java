package com.novamens.logging;


import java.util.List;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.servlet.http.HttpServletRequest;

import com.codesnippets4all.json.generators.JSONGenerator;
import com.codesnippets4all.json.generators.JsonGeneratorFactory;
import com.codesnippets4all.json.parsers.JSONParser;
import com.codesnippets4all.json.parsers.JsonParserFactory;
import com.novamens.content.model.ObjectId;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.kbee.security.KbeePrincipal;
import com.novamens.security.Principal;
import com.novamens.security.User;

@Entity
@DiscriminatorValue("LoginEvent")
public class LoginEvent extends SecurityEvent {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(LoginEvent.class.getName());
	
	private transient KbeeJson json;

	public LoginEvent() {
		super();
	}
	
	public LoginEvent(Principal principal) {
		this(principal, null, null);
	}
	
	public LoginEvent(Principal principal, HttpServletRequest request) {
		this(principal, null, request);
	}
	
	public LoginEvent(Principal principal, String description) {
		this(principal, null, null);
	}
	
	public LoginEvent(Principal principal, String description, HttpServletRequest request) {
		super();
		
		setDomain(((KbeePrincipal)principal).getDomain());
		setObjectId((new ObjectId(principal)).toString());
		setTitle(((User)principal).getFirstLastName());
		setKbeeClass("User");
		
		if (request!=null) {
			setUserAgent(request.getHeader("User-Agent"));
			setRemoteAddr(request.getRemoteAddr());
		}
		
		if (description!=null) {
			setDescription(description);
		}	
		
		setEventUser((User)principal);
	}
	
	@Override
	public String getEventType() {
		return "Login";
	}
	
	@Override
	public String getType() {
		return "Security";
	}

	@Override
	public String getObjectClass() {
		return "User";
	}
	
	@Override
	public String getAction() {
		return getEventType();
	}
	
	public void setUserAgent(String userAgent) {
		KbeeJson json = getJson();
		json.put("user-agent", escape(userAgent));
		setJson(json);
	}
	
	public String getUserAgent() {
		KbeeJson json = getJson();
		String userAgent = unescape((String)json.get("user-agent"));
		return userAgent;
	}
	
	public void setRemoteAddr(String remoteAdrr) {
		KbeeJson json = getJson();
		json.put("remoteaddr", escape(remoteAdrr));
		setJson(json);
	}
	
	public String getRemoteAddr() {
		KbeeJson json = getJson();
		String addr = unescape((String)json.get("remoteaddr"));
		return addr;
	}
	
	public void setDescription(String description) {
		KbeeJson json = getJson();
		json.put("description", escape(description));
		setJson(json);
	}
	
	@Override
	public String getDescription() {
		KbeeJson json = getJson();
		String description = unescape((String)json.get("description"));
		return description;
	}
	
	@Override
	public String toString() {
		return getType() + " - " + getAction() + ". " + getTarget();
	}
	
	@SuppressWarnings("rawtypes")
	protected KbeeJson getJson() {
		if (json == null) {
			try {
				if (getParameters()!=null) {
					JsonParserFactory factory = JsonParserFactory.getInstance();
					JSONParser parser = factory.newJsonParser();
					String parameters = super.getParameters();
					Map roots = parser.parseJson(parameters);
					List root = (List)roots.get("root");
					Map jsonData = (Map)root.get(0);
					json = new KbeeJson(jsonData);
				}
				else {
					json = new KbeeJson();
				}
			} 
			catch (com.codesnippets4all.json.exceptions.JSONParsingException e) {
				logger.error(e);
				json = new KbeeJson();
			}
		}
		return json;
	}
	
	protected void setJson(KbeeJson json) {
		JsonGeneratorFactory factory = JsonGeneratorFactory.getInstance();
		JSONGenerator generator = factory.newJsonGenerator();
		String jsonstring = generator.generateJson(json.getData());
		setParameters(jsonstring);
	}
	
	protected String escape(String value) {
		value = value.replace("\"", "\\'");
		return value;
	}
	
	protected String unescape(String value) {
		value = value!=null ? value.replace("\\'", "\"") : null;
		return value;
	}
}
