// Created on May 19, 2005
package kbee.util;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;



import org.apache.commons.collections4.MultiMap;



/**
 * @author lleggieri May 19, 2005 This class provides support for loading a file
 *         with properties like lines into a map and storing as well Those lines
 *         are in the form key-string=value-string or value-string=key-string if
 *         inversed flag is true Class expects Java Readers and Writers, so
 *         encoding is left to the user.
 */
public final class PropertiesHelper {

	
	public static int getIntProperty(Properties properties, String key, int defaultValue, int minvalue, int maxvalue)  {
		String value = properties.getProperty(key);
		if (value==null) {
			return defaultValue;
		}
		int retvalue;
		try {
			Integer integer = Integer.valueOf(value);
			retvalue = integer.intValue();
			
			if (retvalue<minvalue) 
				retvalue=minvalue;
			else if (retvalue>maxvalue)
				retvalue=maxvalue;
		}
		catch (NumberFormatException e) {
			retvalue = defaultValue;
		}
		return retvalue;
	}

	
	public static void loadProperties(final Map<String, String> map,
			final Reader reader) throws IOException {
		loadProperties(map, reader, false);
	}

	public static void loadProperties(final Map<String, String> map,
			final Reader reader, final boolean inversed) throws IOException {
		BufferedReader bufferedReader;
		if (reader instanceof BufferedReader) {
			bufferedReader = (BufferedReader) reader;
		} else {
			bufferedReader = new BufferedReader(reader);
		}
		String line = bufferedReader.readLine();
		while (line != null) {
			final int equal = line.indexOf('=');
			if (equal != -1) { // si no hay = se ignora la linea
				if (inversed) {
					map
							.put(line.substring(equal + 1), line.substring(0,
									equal));
				} else {
					map
							.put(line.substring(0, equal), line
									.substring(equal + 1));
				}
			}
			line = bufferedReader.readLine();
		}
	}

	public static void storeProperties(final Map map, final Writer writer)
			throws IOException {
		storeProperties(map, writer, false);
	}

	public static void storeProperties(final Map map, final Writer writer,
			final boolean inversed) throws IOException {
		BufferedWriter bufferedWriter;
		if (writer instanceof BufferedWriter) {
			bufferedWriter = (BufferedWriter) writer;
		} else {
			bufferedWriter = new BufferedWriter(writer);
		}
		final Iterator iterator = map.entrySet().iterator();
		if (map instanceof MultiMap) {
			while (iterator.hasNext()) {
				final Map.Entry entry = (Map.Entry) iterator.next();
				writeCollection(bufferedWriter, entry.getKey(),
						(Collection) entry.getValue(), inversed);
				bufferedWriter.newLine();
			}
		} else {
			while (iterator.hasNext()) {
				final Map.Entry entry = (Map.Entry) iterator.next();
				writeEntry(bufferedWriter, entry.getKey(), entry.getValue(),
						inversed);
				bufferedWriter.newLine();
			}
		}
		bufferedWriter.flush();
	}

	private static void writeCollection(final Writer writer, final Object key,
			final Collection collection, final boolean inversed)
			throws IOException {
		if (collection != null) {
			final Iterator colIterator = collection.iterator();
			while (colIterator.hasNext()) {
				final Object value = colIterator.next();
				writeEntry(writer, key, value, inversed);
			}
		}
	}

	private static void writeEntry(final Writer writer, final Object key,
			final Object value, final boolean inversed) throws IOException {
		if (inversed) {
			writer.write(value.toString());
			writer.write('=');
			writer.write(key.toString());
		} else {
			writer.write(key.toString());
			writer.write('=');
			writer.write(value.toString());
		}
	}

	
	
	
	
	
	
	

}
