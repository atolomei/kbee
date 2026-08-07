//Created on 21/04/2006
package com.novamens.spring.beans.factory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

public class PatternListFactoryBean extends AbstractFactoryBean implements
		BeanFactoryAware {
	private ListableBeanFactory beanFactory;

	private Pattern sourcePattern;

	private Pattern excludePattern;

	private Class sourceClass = null;

	private boolean includePrototypes = true;

	private boolean includeFactoryBeans = true;

	private Class targetListClass = ArrayList.class;

	private String logCategory;

	private String logMessage;

	private Log log;

	private MessageFormat logFormater;

	/**
	 */
	public PatternListFactoryBean() {
		super();
	}

	public void setSourcePattern(final Pattern sourcePattern) {
		this.sourcePattern = sourcePattern;
	}

	public void setSourceClass(final Class sourceClass) {
		this.sourceClass = sourceClass;
	}

	public void setIncludeFactoryBeans(final boolean includeFactoryBeans) {
		this.includeFactoryBeans = includeFactoryBeans;
	}

	public void setIncludePrototypes(final boolean includePrototypes) {
		this.includePrototypes = includePrototypes;
	}

	/**
	 * Set the class to use for the target List. Can be populated with a fully
	 * qualified class name when defined in a Spring application context.
	 * <p>
	 * Default is a <code>java.util.ArrayList</code>.
	 * 
	 * @see java.util.ArrayList
	 */
	public void setTargetListClass(final Class targetListClass) {
		if (targetListClass == null) {
			throw new IllegalArgumentException(
					"targetListClass must not be null");
		}
		if (!List.class.isAssignableFrom(targetListClass)) {
			throw new IllegalArgumentException(
					"targetListClass must implement [java.util.List]");
		}
		this.targetListClass = targetListClass;
	}

	@Override
	public Class getObjectType() {
		return List.class;
	}

	@Override
	protected Object createInstance() {
		if (this.sourcePattern == null) {
			throw new IllegalArgumentException("sourcePattern is required");
		}
		final List result = (List) BeanUtils
				.instantiateClass(this.targetListClass);

		final String[] names = this.beanFactory.getBeanNamesForType(
				this.sourceClass, this.includePrototypes,
				this.includeFactoryBeans);
		for (final String name : names) {
			if (this.sourcePattern.matcher(name).matches()) {
				if (this.excludePattern == null
						|| !this.excludePattern.matcher(name).matches()) {
					final Object bean = this.beanFactory.getBean(name);
					this.log(name, bean);
					result.add(bean);
				}
			}
		}
		return result;
	}

	private void log(final String name, final Object bean) {
		if (this.log != null) {
			if (this.log.isInfoEnabled()) {
				this.log.info(this.logFormater
						.format(new Object[] { name, bean }));
			}
		}
	}

	@Override
	public void setBeanFactory(final BeanFactory beanFactory)
			throws BeansException {
		if (!(beanFactory instanceof ListableBeanFactory)) {
			throw new IllegalArgumentException(
					"Cannot do auto-list creation with a BeanFactory that doesn't implements ListableBeanFactory: "
							+ beanFactory);
		}
		this.beanFactory = (ListableBeanFactory) beanFactory;
	}

	public String getLogCategory() {
		return this.logCategory;
	}

	public void setLogCategory(final String logCategory) {
		this.logCategory = logCategory;
		if (this.logCategory != null) {
			this.logCategory = this.logCategory.trim();
			if (this.logMessage != null) {
				this.log = LogFactory.getLog(this.logCategory);
			}
		} else {
			this.log = null;
		}
	}

	public String getLogMessage() {
		return this.logMessage;
	}

	public void setLogMessage(final String logMessage) {
		this.logMessage = logMessage;
		if (this.logMessage != null) {
			this.logMessage = this.logMessage.trim();
			this.logFormater = new MessageFormat(this.logMessage);
			if (this.logCategory != null) {
				this.log = LogFactory.getLog(this.logCategory);
			} else {
				this.log = null;
			}
		} else {
			this.logFormater = null;
			this.log = null;
		}
	}

	public boolean isIncludeFactoryBeans() {
		return this.includeFactoryBeans;
	}

	public boolean isIncludePrototypes() {
		return this.includePrototypes;
	}

	public Class getSourceClass() {
		return this.sourceClass;
	}

	public Pattern getSourcePattern() {
		return this.sourcePattern;
	}

	public Class getTargetListClass() {
		return this.targetListClass;
	}

	public Pattern getExcludePattern() {
		return excludePattern;
	}

	public void setExcludePattern(Pattern excludePattern) {
		this.excludePattern = excludePattern;
	}
}
