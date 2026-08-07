// Created on Jan 8, 2003 $Id: ResourceConnection.java,v 1.3 2008/11/10 19:20:41 alexis Exp $
package com.novamens.net.classpath;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import kbee.util.NovamensRuntimeException;

/**
 * @author alexis
 * @version $Id: ResourceConnection.java,v 1.3 2008/11/10 19:20:41 alexis Exp $
 */
public final class ResourceConnection extends URLConnection {
	private URLConnection connection;

	/**
	 * Constructor for KbeeConnection.
	 * 
	 * @param url
	 *            url
	 */
	public ResourceConnection(final URL url) {
		super(url);
	}

	/**
	 * @see java.net.URLConnection#connect()
	 */
	@Override
	public void connect() throws IOException {
		if (!this.connected) {
			String name = this.url.getHost();
			final String file = this.url.getFile();
			if (file != null && !file.equals("")) { //$NON-NLS-1$
				name += file;
			}
			// first try TCL and then SCL
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			URL target = cl.getResource(name);
			if (target == null) {
				cl = ClassLoader.getSystemClassLoader();
				target = cl.getResource(name);
			}
			if (target == null) {
				throw new FileNotFoundException("Could not locate resource: " //$NON-NLS-1$
						+ name);
			}
			this.connection = target.openConnection();
			this.connection.setUseCaches(true);
			this.connected = true;
		}
	}

	/**
	 * @see java.net.URLConnection#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		this.connect();
		return this.getConnection().getInputStream();
	}

	/**
	 * @see java.net.URLConnection#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() throws IOException {
		this.connect();
		return this.getConnection().getOutputStream();
	}

	/**
	 * @see java.net.URLConnection#getContent()
	 */
	@Override
	public Object getContent() throws IOException {
		this.connect();
		return this.getConnection().getContent();
	}

	/**
	 * @see java.net.URLConnection#getContentEncoding()
	 */
	@Override
	public String getContentEncoding() {
		return this.getConnection().getContentEncoding();
	}

	/**
	 * @see java.net.URLConnection#getContentLength()
	 */
	@Override
	public int getContentLength() {
		return this.getConnection().getContentLength();
	}

	/**
	 * @see java.net.URLConnection#getContentType()
	 */
	@Override
	public String getContentType() {
		return this.getConnection().getContentType();
	}

	/**
	 * @see java.net.URLConnection#getDate()
	 */
	@Override
	public long getDate() {
		return this.getConnection().getDate();
	}

	/**
	 * @see java.net.URLConnection#getLastModified()
	 */
	@Override
	public long getLastModified() {
		return this.getConnection().getLastModified();
	}

	/**
	 * Returns the kbeeObject.
	 * 
	 * @return KbeeObject
	 * @throws IOException
	 *             IOException
	 */
	public URLConnection getConnection() {
		try {
			this.connect();
		} catch (final IOException e) {
			throw new NovamensRuntimeException(e);
		}
		return this.connection;
	}

}
