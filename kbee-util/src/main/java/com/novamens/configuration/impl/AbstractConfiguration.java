//Created on 10/08/2006
package com.novamens.configuration.impl;

import com.novamens.configuration.Configuration;

public abstract class AbstractConfiguration<T> implements Configuration<T> {
	private final ConfigurationProxyFactory configurationProxyFactory;

	private final Factory<T> factory;

	private final long delay;

	public AbstractConfiguration(final long delay, final Factory<T> factory,
			final ConfigurationProxyFactory configurationProxyFactory) {
		this.delay = delay;
		this.factory = factory;
		this.configurationProxyFactory = configurationProxyFactory;
	}

	public T getObserverProxy() {
		return this.configurationProxyFactory.getObserverProxy(this);
	}

	public T getProxyChecker() {
		return this.configurationProxyFactory.getProxyChecker(this);
	}

	public ConfigurationProxyFactory getConfigurationProxyFactory() {
		return this.configurationProxyFactory;
	}

	public long getDelay() {
		return this.delay;
	}

	public Factory<T> getFactory() {
		return this.factory;
	}

	public void release() {
		this.setObserver(null);
	}
}
