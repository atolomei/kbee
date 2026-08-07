// Created on Jan 8, 2003 $Id: Handler.java,v 1.2 2007/03/08 00:46:47 alexis Exp $
package com.novamens.net.classpath;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author alexis
 * @version $Id: Handler.java,v 1.2 2007/03/08 00:46:47 alexis Exp $
 */
public final class Handler extends java.net.URLStreamHandler {

	/**
	 * @see java.net.URLStreamHandler#openConnection(URL)
	 */
	@Override
	protected URLConnection openConnection(final URL u) throws IOException {
		return new ResourceConnection(u);
	}

}
