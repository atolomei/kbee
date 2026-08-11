package com.novamens.kbee.content.webapi.type;

import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.security.Identifiable;

import kbee.api.model.ApiProxy;

public class IProxyAdapter<T> implements Adapter<T, ApiProxy> {
	private String rel;
	
	public IProxyAdapter() {
	}
	
	public IProxyAdapter(String rel) {
		this.rel = rel;
	}
	
	public ApiProxy adapt(T object) {
		ApiProxy value = new ApiProxy();
		value.setId(String.valueOf(((Identifiable)object).getId()));
		value.setName(DisplayNameExtractor.get(object));
		value.setHRef(UriHelper.getUri(object));
		String rel = this.rel!=null ? this.rel : object.getClass().getSimpleName().toLowerCase();
		value.setRel(rel);		
		return value;	
	}
}