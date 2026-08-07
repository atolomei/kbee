//Created on 11/08/2006
package com.novamens.configuration.impl;

import java.net.URL;

import com.novamens.configuration.Configuration;
import com.novamens.configuration.ConfigurationFactory;
import com.novamens.configuration.Configuration.ChangeChecker;
import com.novamens.configuration.Configuration.Factory;

public class ConfigurationFactoryImpl extends ConfigurationFactory {
	private static final long DEFAULT_DELAY = 5000;

	private ConfigurationProxyFactory configurationProxyFactory;

	private ObserverManager observerManager;

	public ConfigurationFactoryImpl() {
		this(new ConfigurationProxyFactoryImpl(), new ObserverManagerImpl());
	}

	/**
	 * @param configurationProxyFactory
	 * @param observerManager
	 */
	public ConfigurationFactoryImpl(
			final ConfigurationProxyFactory configurationProxyFactory,
			final ObserverManager observerManager) {
		super();
		this.configurationProxyFactory = configurationProxyFactory;
		this.observerManager = observerManager;
	}

	@Override
	final public Configuration getConfiguration(final URL url) {
		return this.getConfiguration(url, DEFAULT_DELAY, this
				.getDefaultFactory(url));
	}

	@Override
	final public Configuration getConfiguration(final URL url, final long delay) {
		return this.getConfiguration(url, delay, this.getDefaultFactory(url));
	}

	@Override
	final public <T> Configuration<T> getConfiguration(final URL url,
			final Factory<T> factory) {
		return this.getConfiguration(url, DEFAULT_DELAY, factory);
	}

	@Override
	final public <T> Configuration<T> getConfiguration(final ChangeChecker changeChecker,
			final Factory<T> factory) {
		return this.getConfiguration(changeChecker, DEFAULT_DELAY, factory);
	}

	@Override
	public <T> Configuration<T> getConfiguration(final URL url,
			final long delay, final Factory<T> factory) {
		return new CheckerConfiguration<T>(this.getChangeChecker(url), delay, factory, this
				.getConfigurationProxyFactory(), this.getObserverManager());
	}

	@Override
	public <T> Configuration<T> getConfiguration(final ChangeChecker changeChecker,
			final long delay, final Factory<T> factory) {
		return new CheckerConfiguration<T>(changeChecker, delay, factory, this
				.getConfigurationProxyFactory(), this.getObserverManager());
	}

	protected ChangeChecker getChangeChecker(final URL url) {
		return new URLChangeChecker(url);
	}

	public ConfigurationProxyFactory getConfigurationProxyFactory() {
		return this.configurationProxyFactory;
	}

	public void setConfigurationProxyFactory(
			final ConfigurationProxyFactory configurationProxyFactory) {
		this.configurationProxyFactory = configurationProxyFactory;
	}

	public ObserverManager getObserverManager() {
		return this.observerManager;
	}

	public void setObserverManager(final ObserverManager observerManager) {
		this.observerManager = observerManager;
	}

	protected Factory<?> getDefaultFactory(final URL url) {
		return new URLContentFactory(url);
	}
}
