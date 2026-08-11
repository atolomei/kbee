package com.novamens.kbee.content.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// import org.apache.wicket.util.io.IOUtils;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.document.TreeFile;
import com.novamens.content.document.TreeFileDir;
import com.novamens.content.document.TreeFileKBFile;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.TreeFileFactoryService;
import com.novamens.content.service.TreeFileService;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbfs.FileServerException;
import com.novamens.logging.TreeFileDeleteEvent;
import com.novamens.logging.TreeFileUpdateEvent;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;

public class KbeeTreeFileService implements TreeFileService {

	private TreeFile object = null;
	private ContentDao contentDao = null; 
												
	static private kbee.util.logging.Logger kblogger = kbee.util.logging.Logger.getLogger(KbeeTreeFileService.class.getName());
	static private Logger txlogger = LogManager.getLogger("TxLogger");
	
	public KbeeTreeFileService() {}
	
	public KbeeTreeFileService(TreeFile object) {
		this.object=object;
	}

	/**
	 * This method adds all <b>contents</b> of local_directory
	 * local_directory is excluded.
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void addDirectory(File local_directory) throws IOException, ContentMgmtException {
		Map<String, Number> metrics = new ConcurrentHashMap<String, Number>(5, 0.9f, 1);
		addDirectory(local_directory, metrics);
	}
	
	/**
	 * 
	 * This method adds all <b>contents</b> of local_directory
	 * local_directory is excluded.
	 * 
	 */										
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void addDirectory(File local_directory, Map<String, Number> metrics) throws IOException, ContentMgmtException {
		
		if (local_directory==null)
			throw new IllegalArgumentException("local_directory is null");
		
		if (!local_directory.exists())
			throw new IOException(local_directory + " does not exists");
		
		if (!local_directory.isDirectory())
			throw new IOException(local_directory + " is not a Directory");
		
		// String md5_hex = org.apache.commons.codec.digest.DigestUtils.md5Hex(path);

		long start 	 = System.currentTimeMillis();
		String path  = null;
		
		try {
			path = local_directory.getAbsolutePath();

			kblogger.debug("--------------------------------------------------");
			kblogger.debug("Starting with Path: " + path);
			
			ServiceLocator.getService(com.novamens.lock.ValueLockerService.class).lock(path);
			
			initializeMetrics(metrics);
			
			/** excludes 1st level local_directory, just its contents */
			calculateBaseTotals(local_directory, metrics);

			/** excludes 1st level local_directory, just its contents */
			importDirContentsExcludeDir(local_directory, metrics);
			
			StringBuilder str=new StringBuilder();
			
			str.append("dirs: "  + ((AtomicInteger) metrics.get("dirs")).toString());
			str.append(" files: " + ((AtomicInteger) metrics.get("files")).toString());
			str.append(" size: "  + ServiceLocator.getService(DateTimeService.class).formatFileSize( ((AtomicLong) metrics.get("size")).longValue(), Locale.getDefault()));
		
			if (kblogger.isDebugEnabled()) {
				
				kblogger.debug("Imported: " +  str.toString());
				
													
				if (((AtomicInteger) metrics.get("dirs")).intValue()!=((AtomicInteger) metrics.get("total_dirs_to_import")).intValue()) {
					kblogger.debug("Error. Dirs imported: "+ String.valueOf((AtomicInteger) metrics.get("dirs")) +" vs  Local: " + String.valueOf((AtomicInteger) metrics.get("total_dirs_to_import")));
				}
				else 
					kblogger.debug("Imported dirs: OK");
				
				if (((AtomicInteger) metrics.get("files")).intValue()!=((AtomicInteger) metrics.get("total_files_to_import")).intValue()) {
					kblogger.debug("Error. Files imported: "+ String.valueOf((AtomicInteger) metrics.get("files")) +" vs  Local: " + String.valueOf((AtomicInteger) metrics.get("total_files_to_import")));
				}
				else 
					kblogger.debug("Imported files: OK");
				
				
				if (((AtomicLong) metrics.get("size")).longValue()!=((AtomicLong) metrics.get("total_size_to_import")).longValue()) {
					kblogger.debug("Error. Total Size imported: "+ String.valueOf((AtomicLong) metrics.get("size")) +" bytes  vs  Local: " + String.valueOf((AtomicLong) metrics.get("total_size_to_import")+" bytes"));
				}
				else 
					kblogger.debug("Imported file size: OK");
				
				kblogger.debug("--------------------------------------------------");
			}

			txlogger.info(new TreeFileUpdateEvent(getObject(), "import: " + local_directory.getName() + " | " + str.toString()));
		}
		catch ( IllegalArgumentException e) {
			throw (e);
		}
		catch (Exception e) {
			kblogger.error(e);
			throw (new ContentMgmtException(e));
		}
		finally {
			if (path!=null)
				ServiceLocator.getService(com.novamens.lock.ValueLockerService.class).unlock(path);
			kblogger.debug("Total time: " +String.valueOf((System.currentTimeMillis()-start) ) + " ms");
		}
	}
	
	/**
	 * 
	 * excludes 1st directory. includes only its contents
	 * 
	 * @param dir
	 * @throws IOException
	 * @throws ServiceNotFoundException 
	 * @throws FileServerException 
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	private void importDirContentsExcludeDir(File dir, Map<String, Number> metrics) throws IOException, FileServerException, ServiceNotFoundException  {
		
		if (!dir.exists())
			throw new IOException(dir + " does not exists");
		
		if (!dir.isDirectory())
			throw new IOException(dir + " is not a Directory");
		
		String[] files = dir.list();
		
		String directoryName = dir.getAbsolutePath() +  File.separator;
		
		TreeFile tree_file = getObject();
		
		if(files!=null) {
			for (int i = 0; i < files.length; i++) { 
					final File file = new File(directoryName + files[i]);
					if (file.exists()) {
							if (file.isDirectory())
								processDir(file, tree_file, metrics); // recursive
							else
								processFile(file, tree_file, metrics);
					}
			}
		}
	}

	/**
	 * includes 1st directory
	 * 
	 * @param dir
	 * @param metrics
	 * @throws IOException
	 * @throws FileServerException
	 * @throws ServiceNotFoundException
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	private void importDirContents(File dir, Map<String, Number> metrics) throws IOException, FileServerException, ServiceNotFoundException  {
		processDir(dir, this.getObject(), metrics);
	}

		
	/**
	 * @param dir
	 * @throws IOException
	 * @throws ServiceNotFoundException 
	 * @throws FileServerException 
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	private void processDir(File dir, TreeFile parent, Map<String, Number> metrics) throws IOException, FileServerException, ServiceNotFoundException  {
		
		if (!dir.exists())
			throw new IOException(dir + " does not exists");
		
		if (!dir.isDirectory())
			throw new IOException(dir + " is not a Directory");

		TreeFileDir tree_file = ServiceLocator.getService(TreeFileFactoryService.class).createTreeFileDir();
		tree_file.setDirectoryName(dir.getName());
		parent.addTreeFileChild(tree_file);
		(((AtomicInteger) metrics.get("dirs"))).addAndGet(1);

		String directoryName = dir.getAbsolutePath() +  File.separator;
		
		String[] files = dir.list();
		
		if(files!=null) {
			for (int i = 0; i < files.length; i++) { 
				final File file = new File(directoryName + files[i]);
				if (file.exists()) {
					if (file.isDirectory())
						processDir(file, tree_file, metrics); // recursive
					else 
						processFile(file, tree_file, metrics);
				}
			}
		}
	}
	
	/**
	 * @param file
	 * @param parent
	 * @throws IOException
	 * @throws ServiceNotFoundException 
	 * @throws FileServerException 
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	private void processFile(File file, TreeFile parent, Map<String, Number> metrics) throws IOException, FileServerException, ServiceNotFoundException  {
		
		if (!file.exists()) 
			throw new IOException(file + " does not exists");
		
		if (file.isDirectory())
			throw new IOException(file + " is not a File but a Directory");
		
		if (exclude(file)) {
			AtomicInteger files = ((AtomicInteger) metrics.get("files"));
			files.addAndGet(1);
			return;
		}
		
		TreeFileKBFile tree_file = ServiceLocator.getService(TreeFileFactoryService.class).createTreeFileKBFile();
		KBFile kbfile = upload(file);
		tree_file.setFile(kbfile);
		parent.addTreeFileChild(tree_file);
	
		AtomicInteger files=((AtomicInteger) metrics.get("files"));
		files.addAndGet(1);

		AtomicLong size=((AtomicLong) metrics.get("size"));
		size.addAndGet(kbfile.getSize());
		
		if (kblogger.isDebugEnabled()) {
			if (kbfile.getSize()!=file.length())
				kblogger.error("File " + file.getAbsolutePath() +" and its KBFile has different sizes");
		}
	}
	
	/**
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws FileServerException
	 * @throws ServiceNotFoundException
	 */
	private KBFile upload(File file) throws FileNotFoundException, FileServerException, ServiceNotFoundException  {
	
		Domain domain = ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
		
		
		String path = file.getName();
		KBFileImpl kbfile = (KBFileImpl) ServiceLocator.getService(ContentFactoryService.class).createKBFileNoTrx(file.getName());
		
		//KBFileImpl kbfile = new KBFileImpl();
		//kbfile.set OId(ServiceLocator.getService(ContentFactoryService.class).getResourc eNewOId());
		
		kbfile.setDomain(domain);
		
		kbfile.setName(path);
		String title = FilenameUtils.getBaseName(path).replaceAll("(-|_)", " ");
		
		kbfile.setTitle(title);
		kbfile.setState(ObjectState.ENABLED);
		kbfile.setCreationOffsetDateTime(OffsetDateTime.now());
		kbfile.setLastModifiedUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		kbfile.setUploadOffsetDateTime(OffsetDateTime.now());

		// KBFS V1, V2 
		KBFSResourceService service = kbfile.getService(KBFSResourceService.class);

		BufferedInputStream stream = null;
		
		try {
			stream = new BufferedInputStream(new FileInputStream(file), 4096);
			service.putObject(file.getName(), stream);
			getContentDao().save(kbfile);
			return kbfile;
		} 
		catch (FileServerException | ServiceNotFoundException e) {
			kblogger.error(e);
			throw e;
		} 
		finally {
			if (stream!=null) {
				try {
					stream.close();
				}
				catch (IOException e) {
					kblogger.error(e);
					throw new FileServerException(e);
				}
			}
		} 
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete() throws ContentMgmtException {
		getContentDao().delete(getObject());
		txlogger.info(new TreeFileDeleteEvent(getObject(), "delete"));

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void save() throws ContentMgmtException {
		getContentDao().save(getObject());
		txlogger.info(new TreeFileUpdateEvent(getObject(), "update"));
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update() throws ContentMgmtException {
		getContentDao().save(getObject());
		txlogger.info(new TreeFileUpdateEvent(getObject(), "update"));
	}
	
	public TreeFile getObject() {
		return this.object;
	}
	
	@Override
	public String toHTMLString() { 
		StringBuilder str = new StringBuilder();
		printHTMLDir(getObject(), str);
		return str.toString();
	}
	
	
	protected boolean exclude(File file) {
		return file.getName().endsWith(".metadata");
	}
	
	/**
	 * @param node
	 * @param str
	 */
	private void printHTMLDir(TreeFile node, StringBuilder str)  {

		if (node.isDirectory()) {
			if (node.getParent()==null) {
				str.append("<html>\n"); 
				str.append("<body>\n"); 
				str.append("<div class=\"tree_file\">\n"); 
			}
			
			str.append(node.toHTMLString());
			if (node.getChildren()!=null && node.getChildren().size()>0) {
				str.append("<ul  class=\"level_" + String.valueOf(node.getLevel()).trim() + "\"  >\n"); 			
				for (TreeFile son: node.getChildren())
	 					printHTMLDir(son, str);
				str.append("</ul>\n");  // 4
			}

			if (node.getParent()==null) {
				str.append("</div>\n"); 
				str.append("</body>\n"); 
				str.append("</html>\n"); 
			}
		}
		else { 
			
			str.append(node.toHTMLString()+"\n");
			str.append("</li>\n");
		}
	}
	
	/**
	 * <p>Counts the contents of local_directory, not the directory itself</p>
	 * 
	 * @param local_directory
	 * @param metrics
	 * @throws IOException
	 */
	private void calculateBaseTotals(File local_directory, Map<String, Number> metrics) throws IOException {
		
		AtomicInteger total_files_to_import = (AtomicInteger) metrics.get("total_files_to_import");
		AtomicInteger total_dirs_to_import 	= (AtomicInteger) metrics.get("total_dirs_to_import");
		AtomicLong total_size_to_import 	= (AtomicLong) 	  metrics.get("total_size_to_import");
		
		
		// calculateTotalToImport(local_directory, total_files_to_import, total_dirs_to_import, total_size_to_import);
		
		if (!local_directory.exists())
			throw new IOException(local_directory + " does not exists");
		
		if (!local_directory.isDirectory())
			throw new IOException(local_directory + " is not a Directory");
		
		String[] files = local_directory.list();
		
		String directoryName = local_directory.getAbsolutePath() +  File.separator;
		
		if(files!=null) {
			for (int i = 0; i < files.length; i++) { 
					final File file = new File(directoryName + files[i]);
					if (file.exists()) {
						if (file.isDirectory())
							calculateTotalToImport(file, total_files_to_import, total_dirs_to_import, total_size_to_import);
						else  {
							total_files_to_import.addAndGet(1);
							total_size_to_import.addAndGet(file.length());
						}
					}
			}
		}
	}
																		
	private void calculateTotalToImport(File local_directory, AtomicInteger total_files_to_import, AtomicInteger total_dirs_to_import, AtomicLong total_size_to_import) throws IOException {
		
		if (local_directory==null)
			throw new IllegalArgumentException("local_directory is null");
		
		if (!local_directory.exists())
			throw new IOException(local_directory + " does not exists");
		
		if (!local_directory.isDirectory())
			throw new IOException(local_directory + " is not a Directory");

		
		total_dirs_to_import.addAndGet(1);
		
		String directoryName = local_directory.getAbsolutePath() +  File.separator;
		
		String[] files = local_directory.list();
		
		if(files!=null) {
			for (int i = 0; i < files.length; i++) { 
					final File file = new File(directoryName + files[i]);
					if (file.exists()) {
							if (file.isDirectory())
								calculateTotalToImport(file, total_files_to_import, total_dirs_to_import, total_size_to_import);
							else  {
								total_files_to_import.addAndGet(1);
								total_size_to_import.addAndGet(file.length());
							}
					}
			}
		}

	}
	
	/**
	 * @param metrics
	 */
	private void initializeMetrics(Map<String, Number> metrics) throws IllegalArgumentException  {

		AtomicInteger total_dirs = new AtomicInteger(0);
		AtomicInteger total_files = new AtomicInteger(0);
		AtomicLong	total_disk	= new AtomicLong(0);
		AtomicInteger total_files_to_import = new AtomicInteger(0);
		AtomicInteger total_dirs_to_import = new AtomicInteger(0);
		AtomicLong total_size_to_import = new AtomicLong(0);
		
		if (!metrics.containsKey("dirs"))
			metrics.put("dirs", total_dirs);
		else {
			if (! (metrics.get("dirs") instanceof AtomicInteger))
				throw new IllegalArgumentException("dirs must be AtomicInteger");
		}
		
		if (!metrics.containsKey("files"))
			metrics.put("files", total_files);
		else {
			if (!(metrics.get("files") instanceof AtomicInteger))
				throw new IllegalArgumentException("files must be AtomicInteger");
		}

		if (!metrics.containsKey("size")) 
			metrics.put("size", total_disk);
		else {
			if (!(metrics.get("size") instanceof AtomicLong))
				throw new IllegalArgumentException("files must be AtomicInteger");
		}
		
		if (!metrics.containsKey("total_files_to_import"))
			metrics.put("total_files_to_import", total_files_to_import);
		else {
			if (!(metrics.get("total_files_to_import") instanceof AtomicInteger))
				throw new IllegalArgumentException("total_files_to_import must be AtomicInteger");
		}
			
		if (!metrics.containsKey("total_dirs_to_import"))
			metrics.put("total_dirs_to_import",  total_dirs_to_import);
		else {
			if (!(metrics.get("total_dirs_to_import") instanceof AtomicInteger))
				throw new IllegalArgumentException("total_dirs_to_import must be AtomicInteger");
		}
				
		if (!metrics.containsKey("total_size_to_import"))
			metrics.put("total_size_to_import",  total_size_to_import);
		else {
			if (!(metrics.get("total_size_to_import") instanceof AtomicLong))
				throw new IllegalArgumentException("total_size_to_import must be AtomicInteger");
		}
	}
	
	// Set by Spring
	public ContentDao getContentDao() {
		return contentDao;
	}
	
	public void setContentDao(ContentDao dao) {
		contentDao=dao;
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
