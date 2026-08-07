//Created on 10/08/2006
package com.novamens.configuration.impl;


public class CheckerConfiguration<T> extends AbstractConfiguration<T> {
	ChangeChecker changeChecker;

	private ChangeChecker delayChangeChecker;

	private ObserverManager observerManager;

	public CheckerConfiguration(final ChangeChecker changeChecker, final long delay,
			final Factory<T> factory,
			final ConfigurationProxyFactory configurationProxyFactory,
			final ObserverManager observerManager) {
		super(delay, factory, configurationProxyFactory);
		this.changeChecker = changeChecker;
		if (delay > 0) {
			this.delayChangeChecker = new DelayChangeChecker(changeChecker,
					delay);
		} else {
			this.delayChangeChecker = changeChecker;
		}
		this.observerManager = observerManager;
	}

	public ChangeChecker getChecker() {
		return this.delayChangeChecker;
	}

	public void setObserver(final ChangeObserver observer) {
		if (observer != null) {
			final Runnable runnable = new Runnable() {
				public void run() {
					if (CheckerConfiguration.this.changeChecker.checkChange()) {
						observer.changed();
					}
				}
			};
			this.observerManager.addObserver(this, runnable);
		} else {
			this.observerManager.removeObserver(this);
		}
	}
}