//Created on 25/07/2006
package com.novamens.spring.beans.factory.support;

import java.util.Collection;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

public class FlatParentListableBeanFactory extends DefaultListableBeanFactory {
	private String posfix = "->parent"; //$NON-NLS-1$

	/**
	 * 
	 */
	public FlatParentListableBeanFactory() {
		super();
	}

	/**
	 * @param parentBeanFactory
	 */
	public FlatParentListableBeanFactory(final BeanFactory parentBeanFactory) {
		super(parentBeanFactory);
	}

	@Override
	public void registerBeanDefinition(final String beanName,
			final BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException {
		
		
		if (this.containsBeanDefinition(beanName)) {
			if (this.containsParentReferences(beanName, beanDefinition)) {
				this.registerParentBeanDefinition(beanName);
				this.updateParentReferences(beanName, beanDefinition);
			}
		}
		super.registerBeanDefinition(beanName, beanDefinition);
	}

	private void registerParentBeanDefinition(final String beanName) {
		final BeanDefinition oldBeanDefinition = this
				.getBeanDefinition(beanName);
		if (this.logger.isInfoEnabled()) {
			this.logger
					.info("Generating bean parent definition for bean '"
							+ beanName + "' with name '" + beanName
							+ this.posfix + "'");
		}
		this.registerBeanDefinition(beanName + this.posfix, oldBeanDefinition);
	}

	private boolean containsParentReferences(final String beanName,
			final BeanDefinition beanDefinition) {
		final ConstructorArgumentValues constructorArgumentValues = beanDefinition
				.getConstructorArgumentValues();
		if (constructorArgumentValues != null) {
			Collection<ValueHolder> argumentValues = constructorArgumentValues
					.getGenericArgumentValues();
			if (this.containsParentReferences(beanName, argumentValues)) {
				return true;
			}
			argumentValues = constructorArgumentValues
					.getIndexedArgumentValues().values();
			if (this.containsParentReferences(beanName, argumentValues)) {
				return true;
			}
		}
		final MutablePropertyValues propertyValues = beanDefinition
				.getPropertyValues();
		if (propertyValues != null) {
			if (this.containsParentReferences(beanName, propertyValues)) {
				return true;
			}
		}
		return false;
	}

	private void updateParentReferences(final String beanName,
			final BeanDefinition beanDefinition) {
		final ConstructorArgumentValues constructorArgumentValues = beanDefinition
				.getConstructorArgumentValues();
		if (constructorArgumentValues != null) {
			Collection<ValueHolder> argumentValues = constructorArgumentValues
					.getGenericArgumentValues();
			this.updateParentReferences(beanName, argumentValues);
			argumentValues = constructorArgumentValues
					.getIndexedArgumentValues().values();
			this.updateParentReferences(beanName, argumentValues);
		}
		final MutablePropertyValues propertyValues = beanDefinition
				.getPropertyValues();
		if (propertyValues != null) {
			this.updateParentReferences(beanName, propertyValues);
		}
	}

	private void updateParentReferences(final String beanName,
			final MutablePropertyValues propertyValues) {
		final PropertyValue[] values = propertyValues.getPropertyValues();
		for (int i = 0; i < values.length; i++) {
			final PropertyValue value = values[i];
			if (value.getValue() instanceof RuntimeBeanReference) {
				final RuntimeBeanReference reference = (RuntimeBeanReference) value
						.getValue();
				if (reference.isToParent()
						&& reference.getBeanName().equals(beanName)) {
					final RuntimeBeanReference localReference = new RuntimeBeanReference(
							beanName + this.posfix, false);
					propertyValues.setPropertyValueAt(new PropertyValue(value
							.getName(), localReference), i);
				}
			}
		}
	}

	private void updateParentReferences(final String beanName,
			final Collection<ValueHolder> argumentValues) {
		for (final ValueHolder valueHolder : argumentValues) {
			if (valueHolder.getValue() instanceof RuntimeBeanReference) {
				final RuntimeBeanReference reference = (RuntimeBeanReference) valueHolder
						.getValue();
				if (reference.isToParent()
						&& reference.getBeanName().equals(beanName)) {
					final RuntimeBeanReference localReference = new RuntimeBeanReference(
							beanName + this.posfix, false);
					valueHolder.setValue(localReference);
				}
			}
		}
	}

	private boolean containsParentReferences(final String beanName,
			final MutablePropertyValues propertyValues) {
		final PropertyValue[] values = propertyValues.getPropertyValues();
		for (final PropertyValue value : values) {
			if (value.getValue() instanceof RuntimeBeanReference) {
				final RuntimeBeanReference reference = (RuntimeBeanReference) value
						.getValue();
				if (reference.isToParent()
						&& reference.getBeanName().equals(beanName)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean containsParentReferences(final String beanName,
			final Collection<ValueHolder> argumentValues) {
		for (final ValueHolder valueHolder : argumentValues) {
			if (valueHolder.getValue() instanceof RuntimeBeanReference) {
				final RuntimeBeanReference reference = (RuntimeBeanReference) valueHolder
						.getValue();
				if (reference.isToParent()
						&& reference.getBeanName().equals(beanName)) {
					return true;
				}
			}
		}
		return false;
	}

	public String getPosfix() {
		return this.posfix;
	}

	public void setPosfix(final String posfix) {
		this.posfix = posfix;
	}
}