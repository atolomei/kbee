//Created on 25/07/2006
package com.novamens.spring.context.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.novamens.spring.beans.factory.support.FlatParentListableBeanFactory;

public class FlatParentClassPathXmlApplicationContext extends
		ClassPathXmlApplicationContext {

	/**
	 * @param configLocation
	 * @throws BeansException
	 */
	public FlatParentClassPathXmlApplicationContext(final String configLocation)
			throws BeansException {
		super(configLocation);
	}

	/**
	 * @param configLocations
	 * @param parent
	 * @throws BeansException
	 */
	public FlatParentClassPathXmlApplicationContext(
			final String[] configLocations, final ApplicationContext parent)
			throws BeansException {
		super(configLocations, parent);
	}

	/**
	 * @param configLocations
	 * @param refresh
	 * @param parent
	 * @throws BeansException
	 */
	public FlatParentClassPathXmlApplicationContext(
			final String[] configLocations, final boolean refresh,
			final ApplicationContext parent) throws BeansException {
		super(configLocations, refresh, parent);
	}

	/**
	 * @param configLocations
	 * @param refresh
	 * @throws BeansException
	 */
	public FlatParentClassPathXmlApplicationContext(
			final String[] configLocations, final boolean refresh)
			throws BeansException {
		super(configLocations, refresh);
	}

	/**
	 * @param configLocations
	 * @throws BeansException
	 */
	public FlatParentClassPathXmlApplicationContext(
			final String[] configLocations) throws BeansException {
		super(configLocations);
	}

	@Override
	protected DefaultListableBeanFactory createBeanFactory() {
		return new FlatParentListableBeanFactory(this
				.getInternalParentBeanFactory());
	}
}
