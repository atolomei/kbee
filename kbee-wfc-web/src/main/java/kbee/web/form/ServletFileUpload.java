package kbee.web.form;


import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.io.FilenameUtils;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.io.Streams;

import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbfs.FileServerException;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
			
public class ServletFileUpload extends FileUpload
{

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ServletFileUpload.class.getName());

	// ---------------------------------------------------------- Class methods

	/**
	 * Utility method that determines whether the request contains multipart content.
	 * 
	 * @param request
	 *            The servlet request to be evaluated. Must be non-null.
	 * 
	 * @return <code>true</code> if the request is multipart; <code>false</code> otherwise.
	 */
	public static final boolean isMultipartContent(final HttpServletRequest request)
	{
		if (!"post".equals(request.getMethod().toLowerCase()))
		{
			return false;
		}
		String contentType = request.getContentType();
		if (contentType == null)
		{
			return false;
		}
		if (contentType.toLowerCase().startsWith(MULTIPART))
		{
			return true;
		}
		return false;
	}


	// ----------------------------------------------------------- Constructors


	/**
	 * Constructs an uninitialised instance of this class. A factory must be configured, using
	 * <code>setFileItemFactory()</code>, before attempting to parse requests.
	 * 
	 * @see FileUpload#FileUpload(FileItemFactory)
	 */
	public ServletFileUpload()
	{
		super();
	}


	/**
	 * Constructs an instance of this class which uses the supplied factory to create
	 * <code>FileItem</code> instances.
	 * 
	 * @see FileUpload#FileUpload()
	 * @param fileItemFactory
	 *            The factory to use for creating file items.
	 */
	public ServletFileUpload(final FileItemFactory fileItemFactory)
	{
		super(fileItemFactory);
	}


	// --------------------------------------------------------- Public methods

	

	/**
	 * 
	 * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a> compliant
	 * <code>multipart/form-data</code> stream.
	 * 
	 * @param request
	 *            The servlet request to be parsed.
	 * 
	 * @return A list of <code>FileItem</code> instances parsed from the request, in the order that
	 *         they were transmitted.
	 * 
	 * @throws FileUploadException
	 *             if there are problems reading/parsing the request or storing files.
	 */
	public KBFileImpl parseMultipart(final HttpServletRequest request) throws FileUploadException
	{
		String name = "", path="";
		KBFileImpl file = null;
		try{
			FileItemIterator iter = getItemIterator(request);
			FileItemFactory fac = getFileItemFactory();
			if (fac == null){
				throw new NullPointerException("No FileItemFactory has been set.");
			}
			while (iter.hasNext()){
				
				FileItemStream item = iter.next();
				
				try{
					item.toString();
					if (item.getFieldName().equals("name")) {
						FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(), item.isFormField(), item.getName());
						Streams.copyAndClose(item.openStream(), fileItem.getOutputStream());
						name = fileItem.getString();
					}
					if (item.getFieldName().equals("relativePath")) {
						FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(), item.isFormField(), item.getName());
						Streams.copyAndClose(item.openStream(), fileItem.getOutputStream());
						path = fileItem.getString();
						path = getPath(path);
				}
//					if (item.getFieldName().equals("relativePath")) {
//						FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(), item.isFormField(), item.getName());
//						Streams.copyAndClose(item.openStream(), fileItem.getOutputStream());
//						name = fileItem.getString();
//					} 
					if (item.getFieldName().equals("file")) {
						
						Domain domain = ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
						
						// name = "".equals(name) ? normalize(item.getName()) : name;
						
						//name = normalize(item.getName());
						
						if ("".equals(path))
						path = getPath(item.getName());
						
						file = (KBFileImpl) ServiceLocator.getService(ContentFactoryService.class).createKBFileNoTrx(name);
						file.setInPortalVersion(true);
						file.setDomain(domain);
						file.setLocalPath(path);
						file.setName(name);
						String title = FilenameUtils.getBaseName(name);
						
						title= ((title!=null &&title.length()>1) ? title.replace(".", "") : "-");
						file.setTitle(title);
						file.setState(ObjectState.ENABLED);
						file.setCreationOffsetDateTime(OffsetDateTime.now());
						file.setLastModifiedUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
						file.setUploadOffsetDateTime(OffsetDateTime.now());

						// KBFS V1, V2 
						InputStream is = null;
						try {
							is=item.openStream();
							file.getService(KBFSResourceService.class).putObject(name, is);
						} 
						catch (FileServerException | ServiceNotFoundException e) {
							logger.error(e);
							throw new FileUploadException(e.getClass().getSimpleName(), e);
						} 
						finally {
							if (is!=null)
								IOUtils.closeQuietly(is);
						} 
					}
				}
				catch (FileUploadIOException e) {
					logger.error(e);
					throw new FileUploadException("Processing of " + MULTIPART_FORM_DATA + " request failed. " + e.getMessage(), e);
				}
				catch (IOException e){
					logger.error(e);
					throw new FileUploadException("Processing of " + MULTIPART_FORM_DATA + " request failed. " + e.getMessage(), e);
				}
			}
			return file;
		}
		catch (FileUploadIOException e) {
			logger.error(e);
			throw new FileUploadException("Processing of " + MULTIPART_FORM_DATA + " request failed. " + e.getMessage(), e);
		}
		catch (IOException e){
			logger.error(e);
			throw new FileUploadException("Processing of " + MULTIPART_FORM_DATA + " request failed. " + e.getMessage(), e);
		}
	}

	/**
	 * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a> compliant
	 * <code>multipart/form-data</code> stream.
	 * 
	 * @param request
	 *            The servlet request to be parsed.
	 * 
	 * @return An iterator to instances of <code>FileItemStream</code> parsed from the request, in
	 *         the order that they were transmitted.
	 * 
	 * @throws FileUploadException
	 *             if there are problems reading/parsing the request or storing files.
	 * @throws IOException
	 *             An I/O error occurred. This may be a network error while communicating with the
	 *             client or a problem while storing the uploaded content.
	 */
	public FileItemIterator getItemIterator(final HttpServletRequest request)
		throws FileUploadException, IOException
	{
		return super.getItemIterator(new ServletRequestContext(request));
	}
	
	private String getPath(String name) {
		name = name.trim();
		if (name.startsWith("/")) name = name.substring(1);
		String pathstring = "";
		String[] path = name.split("/");
		if (path.length>1) {
			for (int p=0; p<path.length-1; p++) {
				if (p>0) pathstring += "/";
				pathstring +=  normalize(path[p]);
			}
		}
		return pathstring;
	}
	
	private String normalize(String str) {
		 
		String p=str.replaceAll("[\\t|\\s|(|)]", "")
				 .replace("'", "-")
				 .replace("á", "a")
				 .replace("é", "e")
				 .replace("í", "i")
				 .replace("ó", "o")
				 .replace("ú", "u")
				 .replace("ñ", "n")
				 .replace(";", "")
				 .replace(":", "")
		 		 .replace("°", "")
				 .replace("|", "")
				 .replace("#", "")
				 .replace("$", "")
				 .replace("%", "")
				 .replace("&", "")
				 .replace("/", "")
				 .replace("¡", "")
				 .replace("?", "")
				 .replace("=", "")
				 .replace("}", "")
				 .replace("{", "")
				 .replace(":", "")
				 .replace("Á,", "A")
				 .replace("É,", "E")
				 .replace("Í,", "I")
				 .replace("Ó,", "O")
				 .replace("Ú,", "U")
				 .replace("Ñ",  "N");
		
		

		 return p;
	}

}
