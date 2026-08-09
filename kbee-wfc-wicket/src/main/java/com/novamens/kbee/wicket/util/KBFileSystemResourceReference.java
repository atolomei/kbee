package com.novamens.kbee.wicket.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.FileSystemPathService;
import org.apache.wicket.resource.FileSystemResource;

public class KBFileSystemResourceReference extends ResourceReference {
	
	private static final long serialVersionUID = 1L;

	//private Path path;
	
	private String absolute_path;

	/**
	 * Creates a file system resource reference based on the given path
	 * 
	 * @param name
	 *            the name of the resource reference to expose data
	 * @param path
	 *            the path to create the resource reference
	 */
	public KBFileSystemResourceReference(String name, String path)
	{
		super(name);
		//this.path = path;
		this.absolute_path=path;
	}

	/**
	 * Creates a file system resource reference based on the given name
	 * 
	 * @param name
	 *            the name of the resource reference
	 * 
	 */
	public KBFileSystemResourceReference(String name)
	{
		super(name);
	}

	/**
	 * Creates a file system resource reference based on the given scope and name
	 * 
	 * @param scope
	 *            the scope as class
	 * @param name
	 *            the name of the resource reference
	 * 
	 */
	public KBFileSystemResourceReference(Class<?> scope, String name)
	{
		super(scope, name);
	}

	/**
	 * Creates a new {@link FileSystemResource} and applies the
	 * path to it.
	 */
	@Override
	public IResource getResource()
	{
		return getFileSystemResource();
	}

	/**
	 * Gets the file system resource to be used for the resource reference
	 * 
	 * @return the file system resource to be used for the resource reference
	 */
	protected FileSystemResource getFileSystemResource()
	{
		if (absolute_path == null)
		{
			throw new WicketRuntimeException(
				"Please override #getResource() and provide a path if using a constructor which doesn't take one as argument.");
		}
		return new FileSystemResource(Paths.get(absolute_path));
	}

	/**
	 * Creates a path and a file system (if required) based on the given URI
	 * 
	 * @param uri
	 *            the URI to create the file system and the path of
	 * @param env
	 *            the environment parameter to create the file system with
	 * @return the path of the file in the file system
	 */
	public static Path getPath(URI uri, Map<String, String> env)
	{
		Iterator<FileSystemPathService> pathServiceIterator = ServiceLoader
			.load(FileSystemPathService.class).iterator();
		while (pathServiceIterator.hasNext())
		{
			FileSystemPathService pathService = pathServiceIterator.next();
			if (pathService.isResponsible(uri))
			{
				Path fileSystemPath = pathService.getPath(uri, env);
				if (fileSystemPath != null)
				{
					return fileSystemPath;
				}
			}
		}
		// fall back to just get the path from the URI
		return Paths.get(uri);
	}

	/**
	 * Creates a path and a file system (if required) based on the given URI
	 * 
	 * @param uri
	 *            the URI to create the file system and the path of
	 * @return the path of the file in the file system
	 * @throws IOException
	 *             if the file system could'nt be created
	 * @throws URISyntaxException
	 *             if the URI has no valid syntax
	 */
	public static Path getPath(URI uri) throws IOException, URISyntaxException
	{
		return getPath(uri, null);
	}
	

	
	
	
	 

}
