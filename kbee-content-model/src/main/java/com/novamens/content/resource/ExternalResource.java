package com.novamens.content.resource;

import com.novamens.content.base.Resource;

public interface ExternalResource extends Resource {
	public String getUrl();
	public void setUrl(String url);
}