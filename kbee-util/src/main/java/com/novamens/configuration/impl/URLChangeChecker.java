//Created on 11/08/2006
package com.novamens.configuration.impl;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import kbee.util.NovamensRuntimeException;

public class URLChangeChecker extends AbstractLastModifiedChangeChecker {
	/** Stores a reference to the configuration to be monitored. */
	private final URL url;


	/**
	 * @param url
	 */
	public URLChangeChecker(final URL url) {
		this.url = url;
		this.init();
	}

	@Override
	protected long getLastModified() {
		try {
			URLConnection connection = this.url.openConnection();
			connection.setUseCaches(false);
			return connection.getLastModified();
		} catch (final IOException e) {
			throw new NovamensRuntimeException(e);
		}
	}
}
