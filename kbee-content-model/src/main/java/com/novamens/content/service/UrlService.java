package com.novamens.content.service;

import com.novamens.content.entity.Person;
import com.novamens.service.ObjectService;
import com.novamens.thumbnail.ThumbnailSize;

public interface UrlService extends ObjectService {
	public String getServerUrl();
	public String getUrl();
	public String getPublicTaskUrl();
	public String getTaskUrl();
	public String getPublicUrl();
	public String getPublicUrl(String password);
	
	public String getPublicUrl(Person person);
	public String getPublicUrl(Person person, String password);
	
	public String getRelativeUrl();
	public String getUrl(boolean include_server);
	public String getThumbnailUrl(ThumbnailSize size);	
	public String getThumbnailPublicUrl(ThumbnailSize size);
}