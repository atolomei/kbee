//Created on 19/01/2006
package kbee.util;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.novamens.util.WildcardResourceResolver;


/**
 * El modo se obtiene de System properties /{contexto}/*.properies, si el
 * contexto es nulo toma solo las system-properties
 * 
 * Precedencia de properties: System properties /{contexto}/{modo}/*.properies
 * /{contexto}/*.properies modulos en forma recursiva por cada modulo
 * /META-INF/{contexto}/{modulo*}/{modo}/*.properies
 * /META-INF/{contexto}/{modulo*}/*.properies /META-INF/{contexto}/*.properies
 * 
 * {modo} = com.novamens.{contexto}.mode puede contener '/' para marcar
 * jerarquia {modulo} = com.novamens.{contexto}.modules
 * 
 * modos usuales: prod,dev,test,build
 * 
 * remoto distribuido son modulos?
 * 
 * finalmente se resetea la property com.novamens.{contexto}.modules con todos
 * los modulos incluidos
 * 
 * @author alexis
 * @version $Id: PropertiesFactory.java,v 1.9 2008/07/10 19:28:07 alexis Exp $
 */
// TODO casos de test
// TODO mejorar la implementacion en general
public class PropertiesFactory {
	private static final String MODULES_PROPERTY_NAME = "com.novamens.modules"; //$NON-NLS-1$

	private static final String MODE_PROPERTY_NAME = "com.novamens.mode"; //$NON-NLS-1$

	private static final Map<ClassLoader, Map<String, PropertiesFactory>> classLoadersPropertiesFactories = new HashMap<ClassLoader, Map<String, PropertiesFactory>>();

	private static boolean systemPropertiesLoaded;

	private static final Log log = LogFactory.getLog(PropertiesFactory.class);

	private final WildcardResourceResolver resolver = new WildcardResourceResolver();

	private String context;

	private String[] modes;

	private String[] modules;

	private Properties properties;

	private String modePropertyName;

	private String modulesPropertyName;

	protected PropertiesFactory() {
	}

	public String getcontext() {
		return this.context;
	}

	public void setContext(final String context) {
		this.context = context;
		this.modePropertyName = "com.novamens." + context + ".mode"; //$NON-NLS-1$ //$NON-NLS-2$
		this.modulesPropertyName = "com.novamens." + context + ".modules"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public Properties getProperties() {
		if (this.properties == null) {
			this.loadProperties();
		}
		return this.properties;
	}

	public String[] getModules() {
		if (this.modules == null) {
			this.loadProperties();
		}
		return this.modules;
	}

	public String[] getModes() {
		if (this.modes == null) {
			this.loadProperties();
		}
		return this.modes;
	}

	private void loadProperties() {
		this.loadSystemProperties();
		if (this.context == null || this.context.length() == 0) {
			this.properties = System.getProperties();
			this.modules = getModules(this.properties);
			this.modes = getModes(this.properties);
		} else {
			this.loadModes();

			final List<URL> globalResources = new ArrayList<URL>();
			
			final Properties globalProperties = this.getGlobalProperties(
					this.modes, globalResources);
			
			final List<URL> contextResources = new ArrayList<URL>();
			final Properties properties = this
					.getContextProperties(contextResources);
			
			this.putAll(properties, globalProperties);
			
			final List<String> procecedModules = new ArrayList<String>();
			final List<Properties> modulesProperties = new ArrayList<Properties>();

			final Map<String, List<URL>> modulesResources = new HashMap<String, List<URL>>();
			this.loadModulesProperties(this.modes, properties,
					modulesProperties, procecedModules, modulesResources);

 
			for (int i = modulesProperties.size() - 1; i >= 0; i--) {
				final Properties moduleProperties = modulesProperties.get(i);
				this.putAll(properties, moduleProperties);
			}

 
			final StringBuilder modules = new StringBuilder();
			for (int i = 0; i < procecedModules.size(); i++) {
				final String module = procecedModules.get(i);
				if (i != 0) {
					modules.append(',');
				}
				modules.append(module);
			}
			this.modules = getModules(modules.toString());
			properties.setProperty(MODULES_PROPERTY_NAME, modules.toString());
			properties.setProperty(MODE_PROPERTY_NAME, this.modes.toString());

			this.properties = properties;

			if (log.isInfoEnabled()) {
				log.info("Loaded context [" + this.context + "]."); //$NON-NLS-1$ //$NON-NLS-2$
				final StringBuilder stringModes = new StringBuilder();
				for (int i = 0; i < this.modes.length; i++) {
					final String mode = this.modes[i];
					if (i != 0) {
						stringModes.append(',');
					}
					stringModes.append(mode);
				}
				log.info("Loaded modes [" + stringModes + "]."); //$NON-NLS-1$ //$NON-NLS-2$
				log.info("Loaded modules [" + modules + "]."); //$NON-NLS-1$ //$NON-NLS-2$
				log.info("Properties loaded from System properties."); //$NON-NLS-1$
				for (int i = globalResources.size() - 1; i >= 0; i--) {
					final URL resource = globalResources.get(i);
					log.info("Properties loaded from URL [" + resource + "]."); //$NON-NLS-1$ //$NON-NLS-2$
				}
				for (int i = 0; i < procecedModules.size(); i++) {
					final String module = procecedModules.get(i);
					final List<URL> resources = modulesResources.get(module);
					for (int j = resources.size() - 1; j >= 0; j--) {
						final URL resource = resources.get(j);
						log.info("Properties loaded from URL [" + resource //$NON-NLS-1$ 
								+ "]."); //$NON-NLS-1$
					}
				}
				for (int i = contextResources.size() - 1; i >= 0; i--) {
					final URL resource = contextResources.get(i);
					log.info("Properties loaded from URL [" + resource + "]."); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
	}

	private Properties getContextProperties(final List<URL> contextResources) {
		return this.getMetaProperties("*", null, contextResources); //$NON-NLS-1$		
	}

	private void loadSystemProperties() {
		if (!systemPropertiesLoaded) {
			systemPropertiesLoaded = true;

			final List<URL> resourceList = new ArrayList<URL>();

			URL[] resources = this.getClassResources("/system/*.properties"); //$NON-NLS-1$
			for (final URL resource : resources) {
				resourceList.add(resource);
			}

			resources = this.getClassResources("/META-INF/system/*.properties"); //$NON-NLS-1$
			for (final URL resource : resources) {
				resourceList.add(resource);
			}

			if (log.isInfoEnabled()) {
				for (final URL resource : resourceList) {
					log.info("System properties loaded from URL [" + resource //$NON-NLS-1$ 
							+ "]."); //$NON-NLS-1$
				}
			}
			for (final URL resource : resourceList) {
				InputStream in;
				try {
					in = resource.openStream();
					try {
						final Properties systemProperties = new Properties();
						systemProperties.load(in);
						for (final Map.Entry<Object, Object> entry : systemProperties
								.entrySet()) {
							final String key = (String) entry.getKey();
							String value = (String) entry.getValue();
							try {
								final boolean isResource = Boolean
										.parseBoolean(systemProperties
												.getProperty(
														key + ".resource", Boolean.FALSE //$NON-NLS-1$
																.toString()));

								final boolean append = Boolean
										.parseBoolean(systemProperties
												.getProperty(
														key + ".append", Boolean.FALSE //$NON-NLS-1$
																.toString()));
								final String appendSeparator = systemProperties
										.getProperty(
												key + ".append-separator", ","); //$NON-NLS-1$ //$NON-NLS-2$

								String oldValue = System.getProperty(key);
								if (isResource) {
									final URL url = this
											.getDefaultClassLoader()
											.getResource(value);
									if (url != null) {
										value = url.toExternalForm();
									} else {
										log.error("Not found [" + value //$NON-NLS-1$
												+ "] in classpath."); //$NON-NLS-1$
									}
								}
								if (append) {
									if (oldValue != null) {
										value = oldValue + appendSeparator
												+ value;
										oldValue = null;
									}
								}
								if (oldValue == null) {
									log.info("Setting system properties key [" //$NON-NLS-1$
											+ key + "] value [" + value + "]."); //$NON-NLS-1$//$NON-NLS-2$						
									System.setProperty(key, value);
								}
							} catch (final SecurityException ignore) {
							}
						}
					} finally {
						in.close();
					}
				} catch (final IOException e) {
					throw new NovamensRuntimeException(e);
				}
			}
		}
	}

	private void loadModes() {
		String modeProperty = System.getProperty(this.modePropertyName);
		if (modeProperty == null) {
			final Properties properties = this.getLocalProperties("*", null); //$NON-NLS-1$
			modeProperty = properties.getProperty(this.modePropertyName, ""); //$NON-NLS-1$
		}
		this.modes = getModes(modeProperty);
	}

	private void putAll(final Properties properties,
			final Properties moduleProperties) {
		final Enumeration keys = moduleProperties.propertyNames();
		while (keys.hasMoreElements()) {
			final String key = (String) keys.nextElement();
			properties.setProperty(key, moduleProperties.getProperty(key));
		}
	}

	static private String[] getModes(final String modeProperty) {
		final String[] modes = split(modeProperty, "/"); //$NON-NLS-1$
		final List<String> modeList = new ArrayList<String>();
		modeList.add("/"); //$NON-NLS-1$
		for (int i = 0; i < modes.length; i++) {
			final String mode = modes[i];
			if (mode.length() > 0) {
				final StringBuilder builder = new StringBuilder(modeList.get(i));
				builder.append(mode);
				builder.append('/');
				modeList.add(builder.toString());
			}
		}
		return modeList.toArray(new String[modeList.size()]);
	}

	private void loadModulesProperties(final String[] modes,
			final Properties properties,
			final List<Properties> modulesProperties,
			final List<String> procecedModules,
			final Map<String, List<URL>> modulesResources) {

		final String[] modules = getModules(properties,
				this.modulesPropertyName);
		final Map<String, Properties> localModules = new HashMap<String, Properties>();

		for (final String module : modules) {
			if (!procecedModules.contains(module)) {
				procecedModules.add(module);
				final List<URL> resources = new ArrayList<URL>();
				final Properties moduleProperties = this.getModuleProperties(
						modes, module, resources);
				if (moduleProperties != null) {
					modulesProperties.add(moduleProperties);
					localModules.put(module, moduleProperties);
				}
				modulesResources.put(module, resources);
			}
		}
		for (final String module : modules) {
			final Properties moduleProperties = localModules.get(module);
			if (moduleProperties != null) {
				this.loadModulesProperties(modes, moduleProperties,
						modulesProperties, procecedModules, modulesResources);
			}
		}
	}

	private Properties getModuleProperties(final String[] modes,
			final String module, final List<URL> resourceList) {
		Properties properties = null;
		for (final String mode : modes) {
			properties = this.getMetaProperties(
					module + mode + "*", properties, resourceList); //$NON-NLS-1$
		}
		return properties;
	}

	private Properties getGlobalProperties(final String[] modes,
			final List<URL> resourceList) {
		Properties properties = null;
		for (final String mode : modes) {
			properties = this.getLocalProperties(
					mode.substring(1) + "*", properties, resourceList); //$NON-NLS-1$
		}
		properties.putAll(System.getProperties());
		return properties;
	}

	private Properties getLocalProperties(final String name,
			final List<URL> resourceList) {
		return this.getLocalProperties(name, null, resourceList);
	}

	private Properties getLocalProperties(final String name,
			final Properties properties, final List<URL> resourceList) {
		return this.getLocalProperties(this.context, name, properties,
				resourceList);
	}

	private Properties getLocalProperties(final String context,
			final String name, final Properties properties,
			final List<URL> resourceList) {
		return this
				.getClassProperties(
						"/" + context + "/" + name + ".properties", properties, resourceList); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 	}

	private Properties getMetaProperties(final String name,
			final Properties properties, final List<URL> resourceList) {
		return this.getMetaProperties(this.context, name, properties,
				resourceList);
	}

	private Properties getMetaProperties(final String context,
			final String name, final Properties properties,
			final List<URL> resourceList) {
		return this
				.getClassProperties(
						"/META-INF/" + context + "/" + name + ".properties", properties, resourceList); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private Properties getClassProperties(final String name,
			final Properties defaultProperties, final List<URL> resourceList) {
		final Properties properties = new Properties(defaultProperties);
		try {
			final URL[] resources = this.getClassResources(name);
			if (resources != null) {
				for (int i = resources.length - 1; i >= 0; i--) {
					final URL resource = resources[i];
					if (log.isDebugEnabled()) {
						log.debug("Loading properties [" + resource + "]..."); //$NON-NLS-1$ //$NON-NLS-2$
					}
					final InputStream in = resource.openStream();
					try {
						properties.load(in);
					} finally {
						in.close();
					}
					if (resourceList != null) {
						resourceList.add(resource);
					}
				}
			}
		} catch (final IOException e) {
			throw new NovamensRuntimeException(e);
		}
		return properties;
	}

	private URL[] getClassResources(final String name) {
		try {
			return this.resolver.getResources(name);
		} catch (final IOException e) {
			throw new NovamensRuntimeException(e);
		}
	}

	static public String[] getModes(final Properties properties) {
		final String modeProperty = properties.getProperty(MODE_PROPERTY_NAME,
				""); //$NON-NLS-1$
		return getModes(modeProperty);
	}

	static public String[] getModules(final Properties properties) {
		return getModules(properties, MODULES_PROPERTY_NAME);
	}

	static private String[] getModules(final String moduleString) {
		return split(moduleString, ","); //$NON-NLS-1$
	}

	static private String[] split(String string, String regex) {
		String[] tmp = string.trim().split(regex);
		String[] result = new String[tmp.length];
		for (int i = 0; i < tmp.length; i++) {
			result[i] = tmp[i].trim();
		}
		return result; //$NON-NLS-1$
	}

	static private String[] getModules(final Properties properties,
			final String modulesPropertyName) {
		final String moduleString = properties.getProperty(modulesPropertyName);
		if (moduleString != null) {
			return split(moduleString, ","); //$NON-NLS-1$
		} else {
			return new String[0];
		}
	}

	public static PropertiesFactory getInstance() {
		return getInstance(null);
	}

	public static PropertiesFactory getInstance(String context) {
		if (context == null) {
			context = ""; //$NON-NLS-1$
		}
		ClassLoader classLoader = getDefaultClassLoader();
		Map<String, PropertiesFactory> propertiesFactories = classLoadersPropertiesFactories
				.get(classLoader);
		if (propertiesFactories == null) {
			propertiesFactories = new HashMap<String, PropertiesFactory>();
			classLoadersPropertiesFactories.put(classLoader,propertiesFactories);
		}
		PropertiesFactory propertiesFactory = propertiesFactories.get(context);
		if (propertiesFactory == null) {
			try {
				propertiesFactory = FactoryFinder.getInstance().newInstance(
						PropertiesFactory.class, System.getProperties(),
						PropertiesFactory.class.getName());
				propertiesFactory.setContext(context);
				propertiesFactories.put(context, propertiesFactory);
			} catch (final InstantiationException e) {
				throw new FactoryFinderException(e);
			} catch (final IllegalAccessException e) {
				throw new FactoryFinderException(e);
			} catch (final ClassNotFoundException e) {
				throw new FactoryFinderException(e);
			}
		}
		return propertiesFactory;
	}
    public static void release(ClassLoader classLoader) {
    	classLoadersPropertiesFactories.remove(classLoader);
    }
    
	private static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			cl = PropertiesFactory.class.getClassLoader();
		}
		return cl;
	}
}
