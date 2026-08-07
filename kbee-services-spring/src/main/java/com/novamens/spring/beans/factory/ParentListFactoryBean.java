//Created on 21/04/2006
package com.novamens.spring.beans.factory;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.AbstractFactoryBean;

@SuppressWarnings("rawtypes")
public class ParentListFactoryBean extends AbstractFactoryBean {
	private List parentSourceList;

	private List sourceList;

	private Class targetListClass = ArrayList.class;

	/**
	 */
	public ParentListFactoryBean() {
		super();
	}

	/**
	 * @param parentSourceList
	 */
	public ParentListFactoryBean(final List parentSourceList) {
		super();
		this.parentSourceList = parentSourceList;
	}

	/**
	 * Set the source List, typically populated via XML "ref" elements.
	 */
	public void setParentSourceList(final List parentSourceList) {
		this.parentSourceList = parentSourceList;
	}

	/**
	 * Set the source List, typically populated via XML "list" elements.
	 */
	public void setSourceList(final List sourceList) {
		this.sourceList = sourceList;
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
		if (this.sourceList == null) {
			throw new IllegalArgumentException("sourceList is required");
		}
		if (this.parentSourceList == null) {
			throw new IllegalArgumentException("parentSourceList is required");
		}
		final List result = (List) BeanUtils
				.instantiateClass(this.targetListClass);
		result.addAll(this.parentSourceList);
		result.addAll(this.sourceList);
		return result;
	}
}
