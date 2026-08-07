//Created on 10/08/2006
package com.novamens.configuration;

import java.net.URL;

import com.novamens.configuration.Configuration.ChangeChecker;
import com.novamens.configuration.Configuration.Factory;
import com.novamens.configuration.impl.ConfigurationFactoryImpl;


import kbee.util.NovamensRuntimeException;

public abstract class ConfigurationFactory {
	private static ConfigurationFactory configurationFactory;

	public abstract <T> Configuration<T> getConfiguration(URL url);

	public abstract <T> Configuration<T> getConfiguration(URL url, long delay);

	public abstract <T> Configuration<T> getConfiguration(URL url,
			Factory<T> factory);

	public abstract <T> Configuration<T> getConfiguration(
			ChangeChecker changeChecker,
			Factory<T> factory);

	public abstract <T> Configuration<T> getConfiguration(URL url, long delay,
			Factory<T> factory);

	public abstract <T> Configuration<T> getConfiguration(ChangeChecker changeChecker, long delay,
			Factory<T> factory);

	public static ConfigurationFactory getInstance() {
		if (configurationFactory == null) {
			try {
				configurationFactory = com.novamens.util.FactoryFinder.getInstance().newInstance(
						ConfigurationFactory.class,
						ConfigurationFactoryImpl.class.getName());
			} catch (final ClassNotFoundException e) {
				throw new NovamensRuntimeException(e);
			} catch (final IllegalArgumentException e) {
				throw new NovamensRuntimeException(e);
			} catch (final SecurityException e) {
				throw new NovamensRuntimeException(e);
			} catch (final InstantiationException e) {
				throw new NovamensRuntimeException(e);
			} catch (final IllegalAccessException e) {
				throw new NovamensRuntimeException(e);
			}
		}
		return configurationFactory;
	}
}
