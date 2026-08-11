package com.novamens.kbee.content.webapi.type;

import com.novamens.content.base.Resource;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.UrlService;
import com.novamens.thumbnail.ThumbnailSize;

import kbee.api.model.ApiResource;

public class IResourceAdapter implements Adapter<Resource, ApiResource> {
	
	public IResourceAdapter() {
	}
	
	// test
	public ApiResource adapt(Resource resource) {
		ApiResource iresource = new ApiResource();
		iresource.setId(String.valueOf(resource.getId()));
		iresource.setHRef(UriHelper.getUri(resource));
		iresource.setDisplayName(resource.getDisplayName()) ;
		iresource.setName(resource.getName()) ;
		String thumbnailUri = (resource).getService(UrlService.class).getThumbnailPublicUrl(ThumbnailSize.MEDIUM);
		String uri = (resource).getService(UrlService.class).getPublicUrl();
		iresource.setControlAttribute("uri", uri);
		iresource.setControlAttribute("content-type", getContentType(resource));
		iresource.setControlAttribute("thumbnail", thumbnailUri);
		iresource.setControlAttribute("size", String.valueOf(resource.getSize()));
		return iresource;	
	}
	
	private String getContentType(Resource resource) {
		String fileName = resource.getName().toLowerCase();
		if (fileName!=null && fileName.endsWith("pdf")) {
			return "application/pdf";
		}
		if (resource instanceof KBFile) {
			return ((KBFile)resource).getContentType();
		}
		return null;
	}
}
