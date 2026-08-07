package com.novamens.util;

import java.io.Serializable;

public class KeyValue<T extends Serializable> implements Serializable {

	private static final long serialVersionUID = 1L;

	public Serializable key;
	public T value;
	public String link;
	
	
	public String getDisplayName() {
		return key.toString();
	}

	public KeyValue(T v) {
		this(v.toString(), v, null);
	}

	
	public KeyValue(Serializable k, T v) {
		this(k, v, null);
		
	}
	public KeyValue(Serializable k, T v, String link) {
		this.key=k;
		this.value=v;
		this.link=link;
	}
	
	public String getLink() {
		return link;
	}
	public Serializable getKey() {
		return this.key;
	}
	
	public T getValue() {
		return this.value;
	}
}

