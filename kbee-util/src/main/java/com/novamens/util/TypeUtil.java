//Created on Jul 3, 2004
package com.novamens.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TYPE Utilities. Provides various static utiltiy methods for manipulating
 * types and their string representations.
 * 
 * @since Jetty 4.1
 * @version $Revision: 1.6 $
 * @author Greg Wilkins (gregw)
 */
public class TypeUtil {
	private static final Log log = LogFactory.getLog(TypeUtil.class);

	private static final HashMap<String, Class> name2Class = new HashMap<String, Class>();
	static {
		name2Class.put("boolean", java.lang.Boolean.TYPE); //$NON-NLS-1$
		name2Class.put("byte", java.lang.Byte.TYPE); //$NON-NLS-1$
		name2Class.put("char", java.lang.Character.TYPE); //$NON-NLS-1$
		name2Class.put("double", java.lang.Double.TYPE); //$NON-NLS-1$
		name2Class.put("float", java.lang.Float.TYPE); //$NON-NLS-1$
		name2Class.put("int", java.lang.Integer.TYPE); //$NON-NLS-1$
		name2Class.put("long", java.lang.Long.TYPE); //$NON-NLS-1$
		name2Class.put("short", java.lang.Short.TYPE); //$NON-NLS-1$
		name2Class.put("void", java.lang.Void.TYPE); //$NON-NLS-1$

		name2Class.put("java.lang.Boolean.TYPE", java.lang.Boolean.TYPE); //$NON-NLS-1$
		name2Class.put("java.lang.Byte.TYPE", java.lang.Byte.TYPE); //$NON-NLS-1$
		name2Class.put("java.lang.Character.TYPE", java.lang.Character.TYPE); //$NON-NLS-1$
		name2Class.put("java.lang.Double.TYPE", java.lang.Double.TYPE); //$NON-NLS-1$
		name2Class.put("java.lang.Float.TYPE", java.lang.Float.TYPE); //$NON-NLS-1$
		name2Class.put("java.lang.Integer.TYPE", java.lang.Integer.TYPE); //$NON-NLS-1$
		name2Class.put("java.lang.Long.TYPE", java.lang.Long.TYPE); //$NON-NLS-1$
		name2Class.put("java.lang.Short.TYPE", java.lang.Short.TYPE); //$NON-NLS-1$
		name2Class.put("java.lang.Void.TYPE", java.lang.Void.TYPE); //$NON-NLS-1$

		name2Class.put("java.lang.Boolean", java.lang.Boolean.class); //$NON-NLS-1$
		name2Class.put("java.lang.Byte", java.lang.Byte.class); //$NON-NLS-1$
		name2Class.put("java.lang.Character", java.lang.Character.class); //$NON-NLS-1$
		name2Class.put("java.lang.Double", java.lang.Double.class); //$NON-NLS-1$
		name2Class.put("java.lang.Float", java.lang.Float.class); //$NON-NLS-1$
		name2Class.put("java.lang.Integer", java.lang.Integer.class); //$NON-NLS-1$
		name2Class.put("java.lang.Long", java.lang.Long.class); //$NON-NLS-1$
		name2Class.put("java.lang.Short", java.lang.Short.class); //$NON-NLS-1$

		name2Class.put("Boolean", java.lang.Boolean.class); //$NON-NLS-1$
		name2Class.put("Byte", java.lang.Byte.class); //$NON-NLS-1$
		name2Class.put("Character", java.lang.Character.class); //$NON-NLS-1$
		name2Class.put("Double", java.lang.Double.class); //$NON-NLS-1$
		name2Class.put("Float", java.lang.Float.class); //$NON-NLS-1$
		name2Class.put("Integer", java.lang.Integer.class); //$NON-NLS-1$
		name2Class.put("Long", java.lang.Long.class); //$NON-NLS-1$
		name2Class.put("Short", java.lang.Short.class); //$NON-NLS-1$

		name2Class.put(null, java.lang.Void.TYPE);
		name2Class.put("string", java.lang.String.class); //$NON-NLS-1$
		name2Class.put("String", java.lang.String.class); //$NON-NLS-1$
		name2Class.put("java.lang.String", java.lang.String.class); //$NON-NLS-1$
	}

	/* ------------------------------------------------------------ */
	private static final HashMap<Class, String> class2Name = new HashMap<Class, String>();
	static {
		class2Name.put(java.lang.Boolean.TYPE, "boolean"); //$NON-NLS-1$
		class2Name.put(java.lang.Byte.TYPE, "byte"); //$NON-NLS-1$
		class2Name.put(java.lang.Character.TYPE, "char"); //$NON-NLS-1$
		class2Name.put(java.lang.Double.TYPE, "double"); //$NON-NLS-1$
		class2Name.put(java.lang.Float.TYPE, "float"); //$NON-NLS-1$
		class2Name.put(java.lang.Integer.TYPE, "int"); //$NON-NLS-1$
		class2Name.put(java.lang.Long.TYPE, "long"); //$NON-NLS-1$
		class2Name.put(java.lang.Short.TYPE, "short"); //$NON-NLS-1$
		class2Name.put(java.lang.Void.TYPE, "void"); //$NON-NLS-1$

		class2Name.put(java.lang.Boolean.class, "java.lang.Boolean"); //$NON-NLS-1$
		class2Name.put(java.lang.Byte.class, "java.lang.Byte"); //$NON-NLS-1$
		class2Name.put(java.lang.Character.class, "java.lang.Character"); //$NON-NLS-1$
		class2Name.put(java.lang.Double.class, "java.lang.Double"); //$NON-NLS-1$
		class2Name.put(java.lang.Float.class, "java.lang.Float"); //$NON-NLS-1$
		class2Name.put(java.lang.Integer.class, "java.lang.Integer"); //$NON-NLS-1$
		class2Name.put(java.lang.Long.class, "java.lang.Long"); //$NON-NLS-1$
		class2Name.put(java.lang.Short.class, "java.lang.Short"); //$NON-NLS-1$

		class2Name.put(null, "void"); //$NON-NLS-1$
		class2Name.put(java.lang.String.class, "java.lang.String"); //$NON-NLS-1$
	}

	/* ------------------------------------------------------------ */
	private static final HashMap<Class, Method> class2Value = new HashMap<Class, Method>();
	static {
		try {
			final Class[] s = { java.lang.String.class };

			class2Value.put(java.lang.Boolean.TYPE, java.lang.Boolean.class
					.getMethod("valueOf", s)); //$NON-NLS-1$
			class2Value.put(java.lang.Byte.TYPE, java.lang.Byte.class
					.getMethod("valueOf", s)); //$NON-NLS-1$
			class2Value.put(java.lang.Double.TYPE, java.lang.Double.class
					.getMethod("valueOf", s)); //$NON-NLS-1$
			class2Value.put(java.lang.Float.TYPE, java.lang.Float.class
					.getMethod("valueOf", s)); //$NON-NLS-1$
			class2Value.put(java.lang.Integer.TYPE, java.lang.Integer.class
					.getMethod("valueOf", s)); //$NON-NLS-1$
			class2Value.put(java.lang.Long.TYPE, java.lang.Long.class
					.getMethod("valueOf", s)); //$NON-NLS-1$
			class2Value.put(java.lang.Short.TYPE, java.lang.Short.class
					.getMethod("valueOf", s)); //$NON-NLS-1$

			class2Value.put(java.lang.Boolean.class, java.lang.Boolean.class
					.getMethod("valueOf", s)); //$NON-NLS-1$
			class2Value.put(java.lang.Byte.class, java.lang.Byte.class
					.getMethod("valueOf", s)); //$NON-NLS-1$
			class2Value.put(java.lang.Double.class, java.lang.Double.class
					.getMethod("valueOf", s)); //$NON-NLS-1$
			class2Value.put(java.lang.Float.class, java.lang.Float.class
					.getMethod("valueOf", s)); //$NON-NLS-1$
			class2Value.put(java.lang.Integer.class, java.lang.Integer.class
					.getMethod("valueOf", s)); //$NON-NLS-1$
			class2Value.put(java.lang.Long.class, java.lang.Long.class
					.getMethod("valueOf", s)); //$NON-NLS-1$
			class2Value.put(java.lang.Short.class, java.lang.Short.class
					.getMethod("valueOf", s)); //$NON-NLS-1$
		} catch (final Throwable e) {
			log.warn(e);
		}
	}

	/* ------------------------------------------------------------ */
	private static final Class[] stringArg = { java.lang.String.class };

	/* ------------------------------------------------------------ */
	private static final int intCacheSize = Integer.getInteger(
			"org.mortbay.util.TypeUtil.IntegerCacheSize", 600).intValue(); //$NON-NLS-1$

	private static final Integer[] integerCache = new Integer[intCacheSize];

	private static final String[] integerStrCache = new String[intCacheSize];

	private static final Integer minusOne = Integer.valueOf(-1);

	/* ------------------------------------------------------------ */
	/**
	 * Class from a canonical name for a type.
	 * 
	 * @param name
	 *            A class or type name.
	 * @return A class , which may be a primitive TYPE field..
	 */
	public static Class fromName(final String name) {
		return name2Class.get(name);
	}

	/* ------------------------------------------------------------ */
	/**
	 * Canonical name for a type.
	 * 
	 * @param type
	 *            A class , which may be a primitive TYPE field.
	 * @return Canonical name.
	 */
	public static String toName(final Class type) {
		return class2Name.get(type);
	}

	/* ------------------------------------------------------------ */
	/**
	 * Convert String value to instance.
	 * 
	 * @param type
	 *            The class of the instance, which may be a primitive TYPE
	 *            field.
	 * @param value
	 *            The value as a string.
	 * @return The value as an Object.
	 */
	public static Object valueOf(final Class<?> type, final String value) {
		try {
			if (type.equals(java.lang.String.class)) {
				return value;
			}

			final Method m = class2Value.get(type);
			if (m != null) {
				return m.invoke(null, new Object[] { value });
			}

			if (type.equals(java.lang.Character.TYPE)
					|| type.equals(java.lang.Character.class)) {
				return new Character(value.charAt(0));
			}

			final Constructor<?> c = type.getConstructor(stringArg);
			return c.newInstance(new Object[] { value });
		} catch (final NoSuchMethodException e) {
		} catch (final IllegalAccessException e) {
		} catch (final InstantiationException e) {
		} catch (final InvocationTargetException e) {
			if (e.getTargetException() instanceof Error) {
				throw (Error) e.getTargetException();
			}
		}
		return null;
	}

	/* ------------------------------------------------------------ */
	/**
	 * Convert String value to instance.
	 * 
	 * @param type
	 *            classname or type (eg int)
	 * @param value
	 *            The value as a string.
	 * @return The value as an Object.
	 */
	public static Object valueOf(final String type, final String value) {
		return valueOf(fromName(type), value);
	}

	/* ------------------------------------------------------------ */
	/**
	 * Convert int to Integer using cache.
	 */
	public static Integer newInteger(final int i) {
		if (i >= 0 && i < intCacheSize) {
			if (integerCache[i] == null) {
				integerCache[i] = Integer.valueOf(i);
			}
			return integerCache[i];
		} else if (i == -1) {
			return minusOne;
		}
		return Integer.valueOf(i);
	}

	/* ------------------------------------------------------------ */
	/**
	 * Convert int to String using cache.
	 */
	public static String toString(final int i) {
		if (i >= 0 && i < intCacheSize) {
			if (integerStrCache[i] == null) {
				integerStrCache[i] = Integer.toString(i);
			}
			return integerStrCache[i];
		} else if (i == -1) {
			return "-1"; //$NON-NLS-1$
		}
		return Integer.toString(i);
	}

	/* ------------------------------------------------------------ */
	/**
	 * Parse an int from a substring. Negative numbers are not handled.
	 * 
	 * @param s
	 *            String
	 * @param offset
	 *            Offset within string
	 * @param length
	 *            Length of integer or -1 for remainder of string
	 * @param base
	 *            base of the integer
	 * @exception NumberFormatException
	 */
	public static int parseInt(final String s, final int offset, int length,
			final int base) throws NumberFormatException {
		int value = 0;

		if (length < 0) {
			length = s.length() - offset;
		}

		for (int i = 0; i < length; i++) {
			final char c = s.charAt(offset + i);

			int digit = c - '0';
			if (digit < 0 || digit >= base || digit >= 10) {
				digit = 10 + c - 'A';
				if (digit < 10 || digit >= base) {
					digit = 10 + c - 'a';
				}
			}
			if (digit < 0 || digit >= base) {
				throw new NumberFormatException(s.substring(offset, offset
						+ length));
			}
			value = value * base + digit;
		}
		return value;
	}

	/* ------------------------------------------------------------ */
	public static byte[] parseBytes(final String s, final int base) {
		final byte[] bytes = new byte[s.length() / 2];
		for (int i = 0; i < s.length(); i += 2) {
			bytes[i / 2] = (byte) parseInt(s, i, 2, base);
		}
		return bytes;
	}

	/* ------------------------------------------------------------ */
	public static String toString(final byte[] bytes, final int base) {
		final StringBuilder buf = new StringBuilder();
		for (final byte element : bytes) {
			final int bi = 0xff & element;
			int c = '0' + bi / base % base;
			if (c > '9') {
				c = 'a' + c - '0' - 10;
			}
			buf.append((char) c);
			c = '0' + bi % base;
			if (c > '9') {
				c = 'a' + c - '0' - 10;
			}
			buf.append((char) c);
		}
		return buf.toString();
	}

	/* ------------------------------------------------------------ */
	/**
	 * @param b
	 *            An ASCII encoded character 0-9 a-f A-F
	 * @return The byte value of the character 0-16.
	 */
	public static byte convertHexDigit(final byte b) {
		if (b >= '0' && b <= '9') {
			return (byte) (b - '0');
		}
		if (b >= 'a' && b <= 'f') {
			return (byte) (b - 'a' + 10);
		}
		if (b >= 'A' && b <= 'F') {
			return (byte) (b - 'A' + 10);
		}
		return 0;
	}

	/* ------------------------------------------------------------ */
	public static String toHexString(final byte[] b) {
		final StringBuilder buf = new StringBuilder();
		for (final byte element : b) {
			final int bi = 0xff & element;
			int c = '0' + bi / 16 % 16;
			if (c > '9') {
				c = 'A' + c - '0' - 10;
			}
			buf.append((char) c);
			c = '0' + bi % 16;
			if (c > '9') {
				c = 'a' + c - '0' - 10;
			}
			buf.append((char) c);
		}
		return buf.toString();
	}

	/* ------------------------------------------------------------ */
	public static String toHexString(final byte[] b, final int offset,
			final int length) {
		final StringBuilder buf = new StringBuilder();
		for (int i = offset; i < offset + length; i++) {
			final int bi = 0xff & b[i];
			int c = '0' + bi / 16 % 16;
			if (c > '9') {
				c = 'A' + c - '0' - 10;
			}
			buf.append((char) c);
			c = '0' + bi % 16;
			if (c > '9') {
				c = 'a' + c - '0' - 10;
			}
			buf.append((char) c);
		}
		return buf.toString();
	}

	/* ------------------------------------------------------------ */
	public static byte[] fromHexString(final String s) {
		if (s.length() % 2 != 0) {
			throw new IllegalArgumentException(s);
		}
		final byte[] array = new byte[s.length() / 2];
		for (int i = 0; i < array.length; i++) {
			final int b = Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
			array[i] = (byte) (0xff & b);
		}
		return array;
	}

}