//Created on 11/08/2006
package com.novamens.configuration.impl;

import com.novamens.configuration.Configuration.ChangeChecker;

public class DelayChangeChecker implements ChangeChecker {
	protected long lastChecked;

	private long refreshDelay;

	private ChangeChecker changeChecker;

	/**
	 * @param refreshDelay
	 */
	public DelayChangeChecker(final ChangeChecker changeChecker,
			final long refreshDelay) {
		super();
		this.changeChecker = changeChecker;
		this.refreshDelay = refreshDelay;
	}

	public boolean checkChange() {
		final long now = System.currentTimeMillis();
		if (now > this.lastChecked + this.refreshDelay) {
			this.lastChecked = now;
			return this.changeChecker.checkChange();
		} else {
			return false;
		}
	}
}
