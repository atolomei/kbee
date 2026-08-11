package com.novamens.kbee.content.service;

import com.novamens.content.base.Resource;
import com.novamens.content.entity.Person;
import com.novamens.content.service.TokenService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.resource.SharedResourceToken;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailSize;

public class KbeeResourceUrlService extends KbeeAbstractUrlService {
			
	private Resource resource;
	
	public KbeeResourceUrlService() {
	}

	public KbeeResourceUrlService(Resource resource) {
		this.resource = resource;
	}
	
	@Override
	public String getUrl(boolean include_server) {
		return (include_server ? getServerUrl(): "" )  + getUrl();
	}
	
	public String getUrl() {
		return "/resource/kbfile/"+ getResource().getId() + "/" + getResource().getName();
	}
	
	public String getThumbnailUrl(ThumbnailSize size) {
		return "/resource/thumbnail-"+size.name().toLowerCase()+"/"+ getResource().getId() + "/" + getResource().getName();
	}
	
	public String getThumbnailPublicUrl(ThumbnailSize size) {
		return getServerUrl()+"/resource/shared-thumbnail/"+ getToken(size);
	}
	
	public String getPublicUrl(Person person) {
		return null;
	}
	
	public String getPublicUrl() {
		return  getServerUrl() + "/resource/shared/" + new SharedResourceToken(resource).toString();
	}
	
	public Resource getResource() {
		return resource;
	}
	
	protected Domain getDomain() {
		return getResource().getDomain();
	}

	@Override
	public String getRelativeUrl() {
		return getUrl();
	}
	
	private  String getToken(ThumbnailSize size) {
		KbeeJson data = new KbeeJson();
		data.put("resource", String.valueOf(getResource().getId()));
		data.put("size", size.name());
		return ServiceLocator.getService(TokenService.class).getToken(data);
	}

	@Override
	public String getPublicUrl(String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPublicUrl(Person person, String password) {
		// TODO Auto-generated method stub
		return null;
	}

}