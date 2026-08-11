package com.novamens.kbee.content.resource;

import java.io.Serializable;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.model.ContentId;
import com.novamens.content.service.TokenService;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.service.ServiceLocator;

public class SharedResourceToken implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String token;
	
	public SharedResourceToken(Resource resource) {
		this(resource, null);
	}
	
	public SharedResourceToken(Resource resource, Content content) {
		KbeeJson data = new KbeeJson();
		data.put("id", String.valueOf(resource.getId()));
		data.put("name", String.valueOf(resource.getName()));
		if (content!=null)
		data.put("content", (new ContentId(content)).toString());
		data.put("date", resource.getCreationOffsetDateTime().toString());
		data.put("domain", String.valueOf(resource.getDomain().getId()));
		this.token = ServiceLocator.getService(TokenService.class).getToken(data);
	}
	
	public String toString() {
		return token;
	}
}