//Created on 10/08/2006
package com.novamens.configuration.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.novamens.configuration.Configuration.ChangeObserver;
import com.novamens.configuration.Configuration.Factory;

public class InvocationHandlerObserver implements InvocationHandler,
		ChangeObserver {
	private Factory factory;

	protected Object object;

	public InvocationHandlerObserver(final Object object, final Factory factory) {
		this.object = object;
		this.factory = factory;
	}

	public Object invoke(final Object proxy, final Method method,
			final Object[] args) throws Throwable {
		return method.invoke(this.object, args);
	}

	public void changed() {
		this.object = this.factory.getObject();
	}
}