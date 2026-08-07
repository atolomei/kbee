//Created on 02/05/2006
package com.novamens.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.novamens.wildcard.Pattern;

public class WildcardResourceResolver {

	/** URL protocol for an entry from a jar file: "jar" */
	private static final String URL_PROTOCOL_JAR = "jar"; //$NON-NLS-1$

	/** URL protocol for an entry from a zip file: "zip" */
	private static final String URL_PROTOCOL_ZIP = "zip"; //$NON-NLS-1$

	/** Separator between JAR URL and file path within the JAR */
	private static final String JAR_URL_SEPARATOR = "!/"; //$NON-NLS-1$

	protected final Log logger = LogFactory.getLog(this.getClass());

	private ClassLoader classLoader;

	public WildcardResourceResolver() {
		this(null);
	}

	public WildcardResourceResolver(final ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * Return the ClassLoader that this pattern resolver works with (never
	 * <code>null</code>).
	 */
	public ClassLoader getClassLoader() {
		if (this.classLoader == null) {
			return this.getDefaultClassLoader();
		} else {
			return this.classLoader;
		}
	}

	public URL[] getResources(final String locationPattern) throws IOException {
		// a class path resource (multiple resources for same name possible)
		if (this.isPattern(locationPattern)) {
			// a class path resource pattern
			return this.findPathMatchingResources(locationPattern);
		} else {
			// all class path resources with the given name
			return this.findAllClassPathResources(locationPattern);
		}
	}

	/**
	 * Find all class location resources with the given location via the
	 * ClassLoader.
	 * 
	 * @param location
	 *            the absolute path within the classpath
	 * @return the result as Resource array
	 * @throws IOException
	 *             in case of I/O errors
	 * @see java.lang.ClassLoader#getResources
	 */
	protected URL[] findAllClassPathResources(final String location)
			throws IOException {
		String path = location;
		if (path.startsWith("/")) { //$NON-NLS-1$
			path = path.substring(1);
		}
		final Enumeration resourceUrls = this.getClassLoader().getResources(
				path);
		final Set<URL> result = new LinkedHashSet<URL>(16);
		while (resourceUrls.hasMoreElements()) {
			final URL url = (URL) resourceUrls.nextElement();
			result.add(url);
		}
		return result.toArray(new URL[result.size()]);
	}

	/**
	 * Find all resources that match the given location pattern via the
	 * Ant-style PathMatcher. Supports resources in jar files and zip files and
	 * in the file system.
	 * 
	 * @param locationPattern
	 *            the location pattern to match
	 * @return the result as Resource array
	 * @throws IOException
	 *             in case of I/O errors
	 * @see #doFindPathMatchingJarResources
	 * @see #doFindPathMatchingFileResources
	 * @see org.springframework.util.PathMatcher
	 */
	protected URL[] findPathMatchingResources(final String locationPattern)
			throws IOException {
		final String rootDirPath = this.determineRootDir(locationPattern);
		final String subPattern = locationPattern.substring(rootDirPath
				.length());
		final URL[] rootDirResources = this.getResources(rootDirPath);
		final Set<URL> result = new LinkedHashSet<URL>(16);
		for (final URL rootDirResource : rootDirResources) {
			if (this.isJarResource(rootDirResource)) {
				result.addAll(this.doFindPathMatchingJarResources(
						rootDirResource, subPattern));
			} else {
				result.addAll(this.doFindPathMatchingFileResources(
						rootDirResource, subPattern));
			}
		}
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Resolved location pattern [" + locationPattern
					+ "] to resources " + result);
		}
		return result.toArray(new URL[result.size()]);
	}

	/**
	 * Determine the root directory for the given location.
	 * <p>
	 * Used for determining the starting point for file matching, resolving the
	 * root directory location to a java.io.File and passing it into
	 * <code>retrieveMatchingFiles</code>, with the remainder of the location
	 * as pattern.
	 * <p>
	 * Will return "/WEB-INF" for the pattern "/WEB-INF/*.xml", for example.
	 * 
	 * @param location
	 *            the location to checkn
	 * @return the part of the location that denotes the root directory
	 * @see #retrieveMatchingFiles
	 */
	protected String determineRootDir(final String location) {
		final int prefixEnd = location.indexOf(":") + 1; //$NON-NLS-1$
		int rootDirEnd = location.length();
		while (rootDirEnd > prefixEnd
				&& this.isPattern(location.substring(prefixEnd, rootDirEnd))) {
			rootDirEnd = location.lastIndexOf('/', rootDirEnd - 2) + 1;
		}
		if (rootDirEnd == 0) {
			rootDirEnd = prefixEnd;
		}
		return location.substring(0, rootDirEnd);
	}

	/**
	 * Return whether the given resource handle indicates a jar resource that
	 * the <code>doFindPathMatchingJarResources</code> method can handle.
	 * 
	 * @param resource
	 *            the resource handle to check (usually the root directory to
	 *            start path matching from)
	 * @see #doFindPathMatchingJarResources
	 */
	protected boolean isJarResource(final URL resource) {
		final String protocol = resource.getProtocol();
		return URL_PROTOCOL_JAR.equals(protocol)
				|| URL_PROTOCOL_ZIP.equals(protocol);
	}

	/**
	 * Find all resources in jar files that match the given location pattern via
	 * the Ant-style PathMatcher.
	 * 
	 * @param rootDirResource
	 *            the root directory as Resource
	 * @param subPattern
	 *            the sub pattern to match (below the root directory)
	 * @return the Set of matching Resource instances
	 * @throws IOException
	 *             in case of I/O errors
	 * @see java.net.JarURLConnection
	 * @see org.springframework.util.PathMatcher
	 */
	protected Set<URL> doFindPathMatchingJarResources(
			final URL rootDirResource, final String subPattern)
			throws IOException {
		final URLConnection con = rootDirResource.openConnection();
		JarFile jarFile = null;
		String jarFileUrl = null;
		String rootEntryPath = null;

		if (con instanceof JarURLConnection) {
			// Should usually be the case for traditional JAR files.
			final JarURLConnection jarCon = (JarURLConnection) con;
			jarFile = jarCon.getJarFile();
			jarFileUrl = jarCon.getJarFileURL().toExternalForm();
			rootEntryPath = jarCon.getJarEntry().getName();
		} else {
			// No JarURLConnection -> need to resort to URL file parsing.
			// We'll assume URLs of the format "jar:path!/entry", with the
			// protocol
			// being arbitrary as long as following the entry format.
			// We'll also handle paths with and without leading "file:" prefix.
			final String urlFile = rootDirResource.getFile();
			final int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
			jarFileUrl = urlFile.substring(0, separatorIndex);
			if (jarFileUrl.startsWith("file")) { //$NON-NLS-1$
				jarFileUrl = jarFileUrl.substring("file".length()); //$NON-NLS-1$
			}
			jarFile = new JarFile(jarFileUrl);
			jarFileUrl = "file" + jarFileUrl; //$NON-NLS-1$
			rootEntryPath = urlFile.substring(separatorIndex
					+ JAR_URL_SEPARATOR.length());
		}

		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Looking for matching resources in jar file ["
					+ jarFileUrl + "]");
		}
		if (!rootEntryPath.endsWith("/")) { //$NON-NLS-1$
			// Root entry path must end with slash to allow for proper matching.
			// The Sun JRE does not return a slash here, but BEA JRockit does.
			rootEntryPath = rootEntryPath + "/"; //$NON-NLS-1$
		}
		final Set<URL> result = new LinkedHashSet<URL>(8);
		for (final Enumeration entries = jarFile.entries(); entries
				.hasMoreElements();) {
			final JarEntry entry = (JarEntry) entries.nextElement();
			final String entryPath = entry.getName();
			if (entryPath.startsWith(rootEntryPath)) {
				final String relativePath = entryPath.substring(rootEntryPath
						.length());
				if (this.match(subPattern, relativePath)) {
					result.add(new URL(rootDirResource, relativePath));
				}
			}
		}
		return result;
	}

	/**
	 * Find all resources in the file system that match the given location
	 * pattern via the Ant-style PathMatcher.
	 * 
	 * @param rootDirResource
	 *            the root directory as Resource
	 * @param subPattern
	 *            the sub pattern to match (below the root directory)
	 * @return the Set of matching Resource instances
	 * @throws IOException
	 *             in case of I/O errors
	 * @see #retrieveMatchingFiles
	 * @see org.springframework.util.PathMatcher
	 */
	protected Set<URL> doFindPathMatchingFileResources(
			final URL rootDirResource, final String subPattern)
			throws IOException {
		File rootDir;
		try {
			rootDir = new File(rootDirResource.toURI());
		} catch (URISyntaxException e) {
			throw new IOException(e.getMessage());
		}
		if (this.logger.isDebugEnabled()) {
			this.logger
					.debug("Looking for matching resources in directory tree ["
							+ rootDir.getPath() + "]");
		}
		final Set<File> matchingFiles = this.retrieveMatchingFiles(rootDir,
				subPattern);
		final Set<URL> result = new LinkedHashSet<URL>(matchingFiles.size());
		for (File file : matchingFiles) {
			result.add(file.toURI().toURL());
		}
		return result;
	}

	/**
	 * Retrieve files that match the given path pattern, checking the given
	 * directory and its subdirectories.
	 * 
	 * @param rootDir
	 *            the directory to start from
	 * @param pattern
	 *            the pattern to match against, relative to the root directory
	 * @return the Set of matching File instances
	 * @throws IOException
	 *             if directory contents could not be retrieved
	 */
	protected Set<File> retrieveMatchingFiles(final File rootDir,
			final String pattern) throws IOException {
		if (!rootDir.isDirectory()) {
			// throw new IllegalArgumentException("'rootDir' parameter [" +
			// rootDir + "] does not denote a directory");
			return Collections.EMPTY_SET;
		}
		String fullPattern = rootDir.getAbsolutePath().replace(File.separator,
				"/"); //$NON-NLS-1$
		if (!pattern.startsWith("/")) { //$NON-NLS-1$
			fullPattern += "/"; //$NON-NLS-1$
		}
		fullPattern = fullPattern + pattern.replace(File.separator, "/"); //$NON-NLS-1$
		final Set<File> result = new LinkedHashSet<File>(8);
		this.doRetrieveMatchingFiles(fullPattern, rootDir, result);
		return result;
	}

	/**
	 * Recursively retrieve files that match the given pattern, adding them to
	 * the given result list.
	 * 
	 * @param fullPattern
	 *            the pattern to match against, with preprended root directory
	 *            path
	 * @param dir
	 *            the current directory
	 * @param result
	 *            the Set of matching File instances to add to
	 * @throws IOException
	 *             if directory contents could not be retrieved
	 */
	protected void doRetrieveMatchingFiles(final String fullPattern,
			final File dir, final Set<File> result) throws IOException {
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Searching directory [" + dir.getAbsolutePath()
					+ "] for files matching pattern [" + fullPattern + "]");
		}
		final File[] dirContents = dir.listFiles();
		if (dirContents == null) {
			throw new IOException("Could not retrieve contents of directory ["
					+ dir.getAbsolutePath() + "]");
		}
		final boolean dirDepthNotFixed = fullPattern.indexOf("**") != -1; //$NON-NLS-1$
		for (File element : dirContents) {
			final String currPath = element.getAbsolutePath().replace(
					File.separator, "/"); //$NON-NLS-1$
			if (element.isDirectory()
					&& (dirDepthNotFixed || countOccurrencesOf(currPath, "/") < countOccurrencesOf(fullPattern, "/"))) { //$NON-NLS-1$ //$NON-NLS-2$
				this.doRetrieveMatchingFiles(fullPattern, element, result);
			}
			if (this.match(fullPattern, currPath)) {
				result.add(element);
			}
		}
	}

	private boolean match(final String fullPattern, final String currPath) {
		return Pattern.matches(fullPattern, currPath);
	}

	public boolean isPattern(final String str) {
		return str.indexOf('*') != -1 || str.indexOf('?') != -1;
	}

	public static int countOccurrencesOf(final String str, final String sub) {
		if (str == null || sub == null || str.length() == 0
				|| sub.length() == 0) {
			return 0;
		}
		int count = 0, pos = 0, idx = 0;
		while ((idx = str.indexOf(sub, pos)) != -1) {
			++count;
			pos = idx + sub.length();
		}
		return count;
	}

	public ClassLoader getDefaultClassLoader() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			cl = this.getClass().getClassLoader();
		}
		return cl;
	}
}
