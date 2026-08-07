//Created on 11/08/2006
package com.novamens.configuration.impl;

import com.novamens.configuration.Configuration.ChangeChecker;

public abstract class AbstractLastModifiedChangeChecker implements
		ChangeChecker {
	/** The last time the configuration file was modified. */
	protected long lastModified;

	/**
	 * @param url
	 */
	public AbstractLastModifiedChangeChecker() {
	}
	
	protected void init() {
		this.lastModified = this.getLastModified();
	}

	public boolean checkChange() {
		final long newLastModified = this.getLastModified();
		if (newLastModified > this.lastModified) {
			this.lastModified = newLastModified;
			return true;
		} else {
			return false;
		}
	}

	protected abstract long getLastModified();
}
