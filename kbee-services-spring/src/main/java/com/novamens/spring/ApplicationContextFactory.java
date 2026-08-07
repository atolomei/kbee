package com.novamens.spring;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.AbstractApplicationContext;

import com.novamens.spring.context.support.FlatParentClassPathXmlApplicationContext;


import kbee.util.FactoryFinderException;
import kbee.util.PropertiesFactory;

public class ApplicationContextFactory {
	private static ApplicationContextFactory applicationContextFactory;

	protected AbstractApplicationContext getApplicationContextInstance(
			final String context, final boolean refresh) {
		final PropertiesFactory propertiesFactory = PropertiesFactory
				.getInstance(context);

		final String[] configLocations = this.getConfigLocations(context,
				propertiesFactory.getModes(), propertiesFactory.getModules());

		final AbstractApplicationContext applicationContext = new FlatParentClassPathXmlApplicationContext(
				configLocations, false);

		final PropertyPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertyPlaceholderConfigurer();
		propertyPlaceholderConfigurer.setProperties(propertiesFactory
				.getProperties());
		applicationContext
				.addBeanFactoryPostProcessor(propertyPlaceholderConfigurer);
		if (refresh) {
			applicationContext.refresh();
		}
		return applicationContext;
	}

	private String[] getConfigLocations(final String context,
			final String[] modes, final String[] modules) {

		final List<String> directories = new ArrayList<String>(modes.length
				* (modules.length + 1));



		for (int i = modules.length - 1; i >= 0; i--) {
			final String module = modules[i];
			for (final String mode : modes) {
				directories.add("classpath*:/META-INF/" + context + "/" //$NON-NLS-1$ //$NON-NLS-2$
						+ module + mode + "spring/*-context.xml"); //$NON-NLS-1$
			}
		}
		for (final String mode : modes) {
			directories
					.add("classpath*:/" + context + mode + "spring/*-context.xml"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		directories.add("classpath*:/system/spring/local-context.xml"); //$NON-NLS-1$ //$NON-NLS-2$
		directories
				.add("classpath*:/META-INF/" + context + "/spring/*-context.xml"); //$NON-NLS-1$//$NON-NLS-2$
		return directories.toArray(new String[directories.size()]);
	}

	public static AbstractApplicationContext getInstance(final String context) {
		return getInstance(context, true);
	}

	public static AbstractApplicationContext getInstance(final String context,
			final boolean refresh) {
		if (applicationContextFactory == null) {
			try {
				applicationContextFactory = kbee.util.FactoryFinder.getInstance()
						.newInstance(ApplicationContextFactory.class,
								ApplicationContextFactory.class.getName());
			} catch (final InstantiationException e) {
				throw new FactoryFinderException(e);
			} catch (final IllegalAccessException e) {
				throw new FactoryFinderException(e);
			} catch (final ClassNotFoundException e) {
				throw new FactoryFinderException(e);
			}
		}
		return applicationContextFactory.getApplicationContextInstance(context,
				refresh);
	}
}
