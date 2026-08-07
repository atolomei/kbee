//Created on 20/07/2006
package com.novamens.spring.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;

public abstract class AbstractFactoryBean extends MethodInvokingFactoryBean
		implements BeanFactoryAware {
	private String targetBeanName;

	private BeanFactory beanFactory;

	public AbstractFactoryBean(final String targetMethod,
			final String targetBeanName) {
		this.setTargetMethod(targetMethod);
		this.targetBeanName = targetBeanName;
	}

	public AbstractFactoryBean(final String targetMethod,Object targetObject) {
		this.setTargetMethod(targetMethod);
		this.setTargetObject(targetObject);
	}

	@Override
	public void setBeanFactory(final BeanFactory beanFactory)
			throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public void prepare() throws ClassNotFoundException, NoSuchMethodException {
		if (this.getTargetObject() == null && this.targetBeanName != null) {
			final Object targetObject = this.beanFactory
					.getBean(this.targetBeanName);
			this.setTargetObject(targetObject);
		}
		super.prepare();
	}

	public String getTargetBeanName() {
		return targetBeanName;
	}

	public void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = targetBeanName;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}	
}
