package com.novamens.wicket.util;

import java.io.Serializable;

public class ZoneAux implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String key; // US/Central
	private String zid; // -05:00

	public ZoneAux(String key, String zid) {
		this.zid = zid;
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public String getZid() {
		return zid;
	}

	public String getLabel() {
		return String.format("UTC%s  -  %35s", this.zid, this.key);
	}

}
