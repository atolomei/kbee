//Created on Sep 26, 2005
package kbee.util;

public class FactoryFinderException extends NovamensRuntimeException {
	private static final long serialVersionUID = 8025936448183646531L;

	/**
	 * Construct a new instance with the specified detail string and exception.
	 */
	FactoryFinderException(final String message, final Exception e) {
		super(message, e);
	}

	public FactoryFinderException(final Exception e) {
		super(e);
	}

	public FactoryFinderException(final String message) {
		super(message);
	}

}