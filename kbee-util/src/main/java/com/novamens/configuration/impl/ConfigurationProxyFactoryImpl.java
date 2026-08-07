//Created on 10/08/2006
package com.novamens.configuration.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import com.novamens.Initializable;
import com.novamens.configuration.Configuration;
import com.novamens.configuration.Configuration.Factory;

public class ConfigurationProxyFactoryImpl implements ConfigurationProxyFactory {

	public <T> T getObserverProxy(final Configuration<T> configuration) {
		final Factory<T> factory = configuration.getFactory();
		final T object = factory.getObject();
		final InvocationHandlerObserver invocationHandler = new InvocationHandlerObserver(
				object, factory) {
			boolean shutdown = false;

			@Override
			public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable {
				Object result;
				boolean initializable = method.getDeclaringClass().equals(Initializable.class);
				if (initializable) {
					if (this.object instanceof Initializable) {
						result = super.invoke(proxy, method, args);
					} else {
						result = null;
					}
					if (method.getName().equals("shutdown")) { //$NON-NLS-1$
						configuration.release();
						this.shutdown = true;
					}
				} else {
					result = super.invoke(proxy, method, args);
				}
				return result;
			}

			@Override
			protected void finalize() throws Throwable {
				super.finalize();
				if (!this.shutdown) {
					configuration.release();
				}
			}
		};
		configuration.setObserver(invocationHandler);
		return this.proxy(object, invocationHandler, true);
	}

	public <T> T getProxyChecker(final Configuration<T> configuration) {
		InvocationHandler invocationHandler;
		final Factory<T> factory = configuration.getFactory();
		final T object = factory.getObject();
		invocationHandler = new InvocationHandlerChecker(object, factory,
				configuration.getChecker());
		return this.proxy(object, invocationHandler, false);
	}

	@SuppressWarnings("unchecked")
	private <T> T proxy(final T object,
			final InvocationHandler invocationHandler,
			final boolean addInitializable) {
		final Class[] interfaces = this.getInterfaces(object.getClass(),
				addInitializable);
		return (T) Proxy.newProxyInstance(Thread.currentThread()
				.getContextClassLoader(), interfaces, invocationHandler);
	}

	private Class[] getInterfaces(final Class clazz,
			final boolean addInitializable) {
		Class[] classes = clazz.getInterfaces();
		final Set<Class> interfaces = new HashSet<Class>();
		for (final Class interfaze : classes) {
			if (interfaze.isInterface()) {
				interfaces.add(interfaze);
			}
		}
		final Class superclass = clazz.getSuperclass();
		if (superclass != null) {
			classes = this.getInterfaces(superclass, false);
			for (final Class interfaze : classes) {
				if (interfaze.isInterface()) {
					interfaces.add(interfaze);
				}
			}
		}
		if (addInitializable) {
			interfaces.add(Initializable.class);
		}
		return interfaces.toArray(new Class[interfaces.size()]);
	}

}