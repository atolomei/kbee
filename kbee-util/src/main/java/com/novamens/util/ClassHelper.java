package com.novamens.util;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/* Copyright 2002-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ClassHelper {

	private static final Map<String, Class> primitiveStringWrapperMap = new HashMap<String, Class>();
	static {
		primitiveStringWrapperMap.put(Boolean.TYPE.toString(), Boolean.TYPE);
		primitiveStringWrapperMap.put(Byte.TYPE.toString(), Byte.TYPE);
		primitiveStringWrapperMap
				.put(Character.TYPE.toString(), Character.TYPE);
		primitiveStringWrapperMap.put(Short.TYPE.toString(), Short.TYPE);
		primitiveStringWrapperMap.put(Integer.TYPE.toString(), Integer.TYPE);
		primitiveStringWrapperMap.put(Long.TYPE.toString(), Long.TYPE);
		primitiveStringWrapperMap.put(Double.TYPE.toString(), Double.TYPE);
		primitiveStringWrapperMap.put(Float.TYPE.toString(), Float.TYPE);
		primitiveStringWrapperMap.put(Void.TYPE.toString(), Void.TYPE);
	}

	/**
	 * Convenience for {@link #getClass(String, boolean) getClass(name, true)}
	 */
	public static Class<?> getClass(final String name)
			throws ClassNotFoundException {
		return getClass(name, true);
	}

	/**
	 * Returns a class object for the given name or <i>null</i>
	 */
	@SuppressWarnings("rawtypes")
	public static Class getClass(final String className,
			final boolean initialize) throws ClassNotFoundException {
		return Class.forName(className, initialize, Thread.currentThread()
				.getContextClassLoader());
	}

	public static Class getClass(final Class loadClass, final String className)
			throws ClassNotFoundException {
		ClassNotFoundException cnfe = null;
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (loader != null) {
			try {
				return loader.loadClass(className);
			} catch (final ClassNotFoundException e) {
				cnfe = e;
			}
		}

		loader = loadClass.getClassLoader();
		if (loader != null) {
			try {
				return loader.loadClass(className);
			} catch (final ClassNotFoundException e) {
				if (cnfe == null) {
					cnfe = e;
				}
			}
		}

		try {
			return Class.forName(className);
		} catch (final ClassNotFoundException e) {
			if (cnfe == null) {
				cnfe = e;
			}
			throw cnfe;
		}

	}

	public static InputStream getResourceAsStream(final Class loadClass,
			final String name) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (loader != null) {
			return loader.getResourceAsStream(name);
		}
		loader = loadClass.getClassLoader();
		if (loader != null) {
			return loader.getResourceAsStream(name);
		}
		return ClassLoader.getSystemResourceAsStream(name);
	}

	public static URL getResource(final Class loadClass, final String name) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (loader != null) {
			return loader.getResource(name);
		}
		loader = loadClass.getClassLoader();
		if (loader != null) {
			return loader.getResource(name);
		}
		return ClassLoader.getSystemResource(name);
	}

	public static InputStream getResourceAsStream(final String name) {
		final ClassLoader loader = Thread.currentThread()
				.getContextClassLoader();
		if (loader != null) {
			return loader.getResourceAsStream(name);
		}
		return ClassLoader.getSystemResourceAsStream(name);
	}

	public static URL getResource(final String name) {
		final ClassLoader loader = Thread.currentThread()
				.getContextClassLoader();
		URL url = null;
		if (loader != null) {
			url = loader.getResource(name);
		} else {
			url = ClassLoader.getSystemResource(name);
		}
		if (url == null && name.startsWith("/")) {
			url = getResource(name.substring(1));
		}
		return url;
	}

	/**
	 * Returns new instance of the given class, using the default constructor.
	 */
	public static Object newInstance(final Class target)
			throws InstantiationException, IllegalAccessException {
		return target.newInstance();
	}

	/**
	 * Returns new instance of the given class name, using the default
	 * constructor.
	 */
	public static <T> T newInstance(final Class<T> target, final Class[] types,
			final Object[] args) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		return target.getConstructor(types).newInstance(args);
	}

	/**
	 * Returns a method via reflection look-up of the specific signature.
	 * 
	 * @param clazz
	 *            method's java class
	 * @param methodName
	 *            method name
	 * @param params
	 *            method signature
	 * @return method invokable via <code>java.lang.reflect.Method#invoke</code>,
	 *         or <code>null</code> if no matching method can be found
	 */
	public static Method getMethod(final Class clazz, final String methodName,
			final Class[] params) {
		Method method;
		try {
			method = clazz.getMethod(methodName, params);
		} catch (final Exception ignore) {
			method = null;
		}
		return method;
	}

	/**
	 * Returns a field via reflection look-up.
	 * 
	 * @param clazz
	 *            fields's java class
	 * @param fieldName
	 *            field name
	 * @return field retrievable via <code>java.lang.reflect.Field#getXXX</code>,
	 *         or <code>null</code> if no matching field can be found
	 */
	public static Field getField(final Class clazz, final String fieldName) {
		Field field;
		try {
			field = clazz.getField(fieldName);
		} catch (final Exception ignore) {
			field = null;
		}
		return field;
	}

	// *******************************************************************
	// Convenience methods
	// *******************************************************************

	/**
	 * Returns new instance of the given class name, using the default
	 * constructor.
	 */
	public static Object newInstance(final String className)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		return newInstance(getClass(className));
	}

	/**
	 * Returns new instance of the given class name, using the default
	 * constructor.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T newInstance(final String className,
			final Class[] types, final Object[] args)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, ClassNotFoundException {
		return (T) newInstance(getClass(className), types, args);
	}

	public static <T> T newInstance(final Class<T> target, final Class type,
			final Object arg) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		return newInstance(target, new Class[] { type }, new Object[] { arg });
	}

	public static Object newInstance(final String className, final Class type,
			final Object arg) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException,
			SecurityException, ClassNotFoundException {
		return newInstance(className, new Class[] { type },
				new Object[] { arg });
	}

	/**
	 * Returns a method via reflection look-up of the specific signature.
	 * 
	 * @param object
	 *            runtime object instance
	 * @param methodName
	 *            method name
	 * @param params
	 *            method signature
	 * @return method invokable via <code>java.lang.reflect.Method#invoke</code>,
	 *         or <code>null</code> if no matching method can be found
	 */
	public static Method getMethod(final Object object,
			final String methodName, final Class[] params) {
		return getMethod(object.getClass(), methodName, params);
	}

	/**
	 * Returns a method via reflection look-up of the specific signature.
	 * 
	 * @param className
	 *            class name
	 * @param methodName
	 *            method name
	 * @param params
	 *            method signature
	 * @return method invokable via <code>java.lang.reflect.Method#invoke</code>,
	 *         or <code>null</code> if no matching method can be found
	 */
	public static Method getMethod(final String className,
			final String methodName, final Class[] params) {
		Method method = null;
		try {
			final Class clazz = getClass(className, false);
			method = getMethod(clazz, methodName, params);
		} catch (final Exception ignore) {
		}
		return method;
	}

	public static String[] toStringArray(final Class[] methodParameterTypes) {
		final String[] classes = new String[methodParameterTypes.length];
		for (int i = 0; i < classes.length; i++) {
			classes[i] = methodParameterTypes[i].getName();
		}
		return classes;
	}

	public static Class[] toClassArray(final String[] methodParameterTypes)
			throws ClassNotFoundException {
		final Class[] classes = new Class[methodParameterTypes.length];
		for (int i = 0; i < classes.length; i++) {
			classes[i] = primitiveStringWrapperMap.get(methodParameterTypes[i]);
			if (classes[i] == null) {
				classes[i] = ClassHelper.getClass(methodParameterTypes[i]);
			}
		}
		return classes;
	}
}