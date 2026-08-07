//Created on Sep 26, 2005
package kbee.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class FactoryFinder {
	private final Log logger = LogFactory.getLog(FactoryFinder.class);

	private static FactoryFinder factoryFinder;

	public FactoryFinder() {
	}

	public static FactoryFinder getInstance() {
		if (factoryFinder == null) {
			try {
				factoryFinder = new FactoryFinder().newInstance(
						FactoryFinder.class, System.getProperties(),
						FactoryFinder.class.getName());
			} catch (final InstantiationException e) {
				throw new RuntimeException(e);
			} catch (final IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (final ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return factoryFinder;
	}

	public final <T> T newInstance(final Class<T> factoryClass,
			final String fallbackClassName) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		return (T)this.newInstance(factoryClass.getName(), PropertiesFactory
				.getInstance().getProperties(), fallbackClassName);
	}

	public final <T> T newInstance(final Class<T> factoryClass,
			final Properties properties, final String fallbackClassName)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		return (T)this.newInstance(factoryClass.getName(), properties,
				fallbackClassName);
	}

	public final <T> T newInstance(final String factoryId,
			final String fallbackClassName) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		return (T)this.newInstance(factoryId, PropertiesFactory.getInstance()
				.getProperties(), fallbackClassName);
	}

	public final <T> T newInstance(final String factoryId,
			final Properties properties, final String fallbackClassName)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		return (T)this.newInstanceImpl(factoryId, properties, fallbackClassName);
	}

	protected <T> T newInstanceImpl(final String factoryId,
			final Properties properties, final String fallbackClassName)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		final Class<? extends T> clazz = (Class<? extends T>)this.find(factoryId, properties,
				fallbackClassName);
		if (clazz == null) {
			throw new RuntimeException("No implementation defined for "
					+ factoryId);
		}
		return clazz.newInstance();
	}

	public final <T> Class<? extends T> find(final Class<? extends T> factoryClass,
			final String fallbackClassName) throws ClassNotFoundException {
		return (Class<? extends T>)this.find(factoryClass.getName(), System.getProperties(),
				fallbackClassName);
	}

	public final <T> Class<? extends T> find(final Class<? extends T> factoryClass,
			final Properties properties, final String fallbackClassName)
			throws ClassNotFoundException {
		return (Class<? extends T>)this.find(factoryClass.getName(), properties, fallbackClassName);
	}

	public final <T> Class<? extends T> find(final String factoryId,
			final String fallbackClassName) throws ClassNotFoundException {
		return (Class<? extends T>)this.find(factoryId, System.getProperties(), fallbackClassName);
	}

	public final <T> Class<? extends T> find(final String factoryId,
			final Properties properties, final String fallbackClassName)
			throws ClassNotFoundException {
		return (Class<? extends T>)this.findImpl(factoryId, properties, fallbackClassName);
	}

	protected <T> Class<? extends T> findImpl(final String factoryId,
			final Properties properties, final String fallbackClassName)
			throws ClassNotFoundException {
		final ClassLoader classLoader = findClassLoader();

		// Use the property first
		try {
			final String property = properties.getProperty(factoryId);
			if (property != null) {
				this.logger.debug("found property" + property); //$NON-NLS-1$
				return (Class<? extends T>)this.getClass(property, classLoader);
			}
		} catch (final SecurityException se) {
		}

		final String serviceId = "META-INF/services/" + factoryId; //$NON-NLS-1$
		// try to find services in CLASSPATH
		try {
			InputStream is = null;
			if (classLoader == null) {
				is = ClassLoader.getSystemResourceAsStream(serviceId);
			} else {
				is = classLoader.getResourceAsStream(serviceId);
			}

			if (is != null) {
				this.logger.debug("found " + serviceId); //$NON-NLS-1$
				try {
					final BufferedReader rd = new BufferedReader(
							new InputStreamReader(is, "UTF-8")); //$NON-NLS-1$

					final String factoryClassName = rd.readLine();
					rd.close();

					if (factoryClassName != null
							&& !"".equals(factoryClassName)) { //$NON-NLS-1$
						this.logger
								.debug("loaded from services: " + factoryClassName); //$NON-NLS-1$
						return (Class<? extends T>)this.getClass(factoryClassName, classLoader);
					}
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}
		} catch (final SecurityException ex) {
			this.logger.debug(ex);
		}

		if (fallbackClassName == null) {
			return null;
		}

		this.logger.debug("loaded from fallback value: " + fallbackClassName); //$NON-NLS-1$
		return (Class<? extends T>)this.getClass(fallbackClassName, classLoader);
	}

	/**
	 * Create an instance of a class using the specified ClassLoader
	 * 
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	private <T> Class<? extends T> getClass(final String className,
			final ClassLoader classLoader) throws ClassNotFoundException {
		Class spiClass;
		if (classLoader == null) {
			spiClass = Class.forName(className);
		} else {
			spiClass = classLoader.loadClass(className);
		}
		return spiClass;
	}

	static abstract class ClassLoaderFinder {
		abstract ClassLoader getContextClassLoader();
	}

	static class ClassLoaderFinderConcrete extends ClassLoaderFinder {
		@Override
		ClassLoader getContextClassLoader() {
			return Thread.currentThread().getContextClassLoader();
		}
	}

	/**
	 * Figure out which ClassLoader to use. For JDK 1.2 and later use the
	 * context ClassLoader if possible. Note: we defer linking the class that
	 * calls an API only in JDK 1.2 until runtime so that we can catch
	 * LinkageError so that this code will run in older non-Sun JVMs such as the
	 * Microsoft JVM in IE.
	 */
	private static ClassLoader findClassLoader() {
		ClassLoader classLoader;
		try {
			// Construct the name of the concrete class to instantiate
			final Class clazz = Class.forName(FactoryFinder.class.getName()
					+ "$ClassLoaderFinderConcrete"); //$NON-NLS-1$
			final ClassLoaderFinder clf = (ClassLoaderFinder) clazz
					.newInstance();
			classLoader = clf.getContextClassLoader();
		} catch (final LinkageError le) {
			// Assume that we are running JDK 1.1, use the current ClassLoader
			classLoader = FactoryFinder.class.getClassLoader();
		} catch (final ClassNotFoundException x) {
			// This case should not normally happen. MS IE can throw this
			// instead of a LinkageError the second time Class.forName() is
			// called so assume that we are running JDK 1.1 and use the
			// current ClassLoader
			classLoader = FactoryFinder.class.getClassLoader();
		} catch (final Exception x) {
			// Something abnormal happened so throw an error
			throw new RuntimeException(x.toString(), x);
		}
		return classLoader;
	}
}
