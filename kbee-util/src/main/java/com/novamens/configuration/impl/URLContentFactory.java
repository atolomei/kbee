//Created on 11/08/2006
package com.novamens.configuration.impl;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import com.novamens.configuration.Configuration.Factory;

import kbee.util.NovamensRuntimeException;

public class URLContentFactory implements Factory {
	private final URL url;

	/**
	 * @param url
	 */
	public URLContentFactory(final URL url) {
		super();
		this.url = url;
	}

	public Object getObject() {
		try {
			URLConnection connection = this.url.openConnection();
			connection.setUseCaches(false);
			return connection.getContent();
		} catch (final IOException e) {
			throw new NovamensRuntimeException(e);
		}
	}

}
