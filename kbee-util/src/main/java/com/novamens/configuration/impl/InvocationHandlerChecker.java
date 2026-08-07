//Created on 10/08/2006
package com.novamens.configuration.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.novamens.configuration.Configuration.ChangeChecker;
import com.novamens.configuration.Configuration.Factory;

public class InvocationHandlerChecker implements InvocationHandler {
	private ChangeChecker notifier;

	private Factory factory;

	private Object object;

	public InvocationHandlerChecker(final Object object, final Factory factory,
			final ChangeChecker notifier) {
		this.object = object;
		this.factory = factory;
		this.notifier = notifier;
	}

	public Object invoke(final Object proxy, final Method method,
			final Object[] args) throws Throwable {
		this.checkChange();
		return method.invoke(this.object, args);
	}

	private void checkChange() {
		synchronized (this) {
			if (this.notifier.checkChange()) {
				this.object = this.factory.getObject();
			}
		}
	}
}