package com.novamens.kbee.content.command;

import java.io.File;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;

import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.document.IDoc;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.model.KbeeValueMember;
import com.novamens.kbee.content.resource.KBFileImpl;



import com.novamens.lock.ValueLockerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


/** 
 * 
 *  <p>Comando que recorre el File System definido en el {@link Domain} de tipo
 *  {@link DomainType.FILE_SYSTEM_READER} y genera un {@link idoc} por cada archivo. 
 *  
 *  <ul>
 *  <li><b>limit</b>		: cantidad maxima de idocs a generar (incluyendo los que dan error al generar). Default: sin limite</li>
 *  <li><b>type</b>			: nombre del clasificador type (default "Type")</li>
 *  <li><b>object uri</b>	: nombre del clasificador object uri (default "Object URI")</li>
 *  <li><b>extension</b>	: nombre del clasificador extension (default "Extension")</li>
 *  <li><b>type_member</b>	: dataset member a asignar  el type.</li>
 *  <li><b>base_dir</b>		: directorio a scannear (default: home dir del Dominio). Notar que es posible scanear directorios que no están
 *  						  efectivamente por debajo del arbol del home del Dominio</li>
 *  </ul>
 *  </p>

 */
public class ScanFileSystemCommand extends AbstractCommand implements Runnable  {

	static Logger logger = LogManager.getLogger(ScanFileSystemCommand.class.getName());

	static final int BATCH_SIZE = 64;

	
	private static final double GB = 1000000000.0;
	
	private List<File> list;

	private Thread thread;
	private boolean running;
	
	
	private Serializable domainId = null;
	private Domain domain = null;
	
	private int counter 		= 0;
	private int err_count 		= 0;
	private int total_files		= 0;
	private int already_exists	= 0	;
	private int limit = 0;
	
	private Logger result_logger;
	private Logger file_list_logger;

	private ContentTemplate idoc_template;

	private Classifier classifier 				= null;
	private Classifier type_classifier 			= null;
	private Classifier extension_classifier 	= null;
	private DataSetMember type_dataset_member 	= null;

	private String type_member			 		= "File";
	private String classifier_name	 	 		= "Object URI";
	private String type_classifier_name  		= "Type";
	private String extension_classifier_name  	= "Extension";
	private String base_dir				 		= null;
	
									
	private boolean calculated_extension_classifier = false;
	private boolean calculated_classifier 			= false;
	private boolean calculated_type_dataset_member 	= false;
	private boolean calculated_type_classifier 		= false;

	//private long estimated_total  = 0;
	private long file_disk_size  = 0;
	
	double alpha = 0.92;
	double beta  = 0.08;
	
	double mean_n1 = 0.0;
	double mean_n  = 0.0;
	
	
	/** --------------------------------------------------------------------
	 */
	public ScanFileSystemCommand() {
		setName("Scan File System Command");
	}

	
	/** --------------------------------------------------------------------
	 */
	public ScanFileSystemCommand(Domain domain) {
		setName("Scan File System Command");
		setDomain(domain);
		this.limit=0;
		
	}

	/** --------------------------------------------------------------------
	 */
	public ScanFileSystemCommand(Domain domain, int limit) {
		setName("Scan File System Command (" + String.valueOf(limit) + ")");
		setDomain(domain);
		this.limit=limit;
	}

	
	
	/** --------------------------------------------------------------------
	 */
	@Override
	public void run() {
		setState(CommandState.RUNNING);
		executeTask();
	}

	
	
	
	/** --------------------------------------------------------------------------------
	 *  Scan File Server:
     *  Overwrite existing
	 */
	protected void executeTask() {
		
		setDateStarted(OffsetDateTime.now());
		setProgress(0);
		
		assignParameters();
		
		this.counter=0;
		try {
				com.novamens.hibernate.session.Session.open();
				ServiceLocator.getService(SecurityService.class).authenticate("root@" + getDomain().getName().trim());
				Domain domain = getDomain();
				result_logger 	 = LogManager.getLogger("ScanFileReader");
				file_list_logger = LogManager.getLogger("ScanFileReaderList");
				
				list = new ArrayList<File>(BATCH_SIZE);
				
				List<ContentTemplate> templates = getContentDao().getTemplates(getDomain());
				for(ContentTemplate template: templates) {
					if (template.getContentClass().getName().toLowerCase().equals("idoc") ) {
						idoc_template=template;
						break;
					}
				}
				if (idoc_template==null) {
					logger.error(domain.getName() + " iDOC not found");
					setResult(" iDOC not found");
					setResultDetails(domain.getName() + " iDOC not found");
					finalize(CommandState.ERROR);
					return;
				}
				
				if (domain.getDomainType()==DomainType.FILE_SYSTEM_READER) {
																	
							String base_dir_str = (this.base_dir!= null ?  this.base_dir : domain.getFileReaderDirectory());
							
							if (base_dir_str!=null) { 
									
								File base_dir = new File(base_dir_str);

									if (base_dir.exists() && base_dir.isDirectory()) {
										
										setStatusInfo("Estimating total hard disk");
										this.file_disk_size  = base_dir.getTotalSpace();
										
										setStatusInfo("Total hard disk: " + String.valueOf( (double) this.file_disk_size / GB));
										
										result_logger.info("Starting processing " + base_dir.getAbsolutePath());
										setStatusInfo("Starting processing " + base_dir.getAbsolutePath());
										
										process(base_dir);
										
										result_logger.info("Ending Command execution " + getName());
										setStatusInfo("Ending Command execution " + getName());

										if (!isStopped()) {
											setProgress(100);
											setResult("OK");
											finalize(CommandState.COMPLETED);
										}
										else if ( !(this.limit>0 && this.limit<=(this.counter+this.err_count))) {
											setResult("Terminated by user");
											finalize(CommandState.CANCELED);
										}
									}
									else {
										logger.error(domain.getName() + " " + (base_dir.exists() ?  base_dir.getAbsolutePath() +" is not a Directory" : base_dir.getAbsolutePath() + " does not exists."));
										setResult("File Reader Dir is null");
										setResultDetails(domain.getName() + "File Reader Dir is null");
										finalize(CommandState.ERROR);
									}
							}
							else {
								logger.error(domain.getName() + " File Reader Dir is null");
								setResult("File Reader Dir is null");
								setResultDetails(domain.getName() + "File Reader Dir is null");
								finalize(CommandState.ERROR);
							}
				}
				
				else {

					logger.error(domain.getName() + " invalid Domain Type");
					setResult("Invalid Domain Type");
					setResultDetails(domain.getName() + " invalid Domain Type");
					finalize(CommandState.ERROR);
				}
				
				
		} finally {
			
			com.novamens.hibernate.session.Session.close();	
		}				
	}
	
	//private long getEstimatedTotal() {
	//	return estimated_total;
	//}

	/** --------------------------------------------------------------------------------
	 *   <li><b>limit</b>		: cantidad maxima de idocs a generar (incluyendo los que dan error al generar). Default: sin limite</li>
	 *  <li><b>type</b>			: nombre del clasificador type (default "Type")</li>
	 * 	<li><b>object uri</b>	: nombre del clasificador object uri (default "Object URI")</li>
	 *  <li><b>type_member</b>	: dataset member a asignar  el type.</li>
	 *  <li><b>base_dir</b>		: directorio a scannear (default: home dir del Dominio). Notar que es posible scanear directorios que no están

	 */
	private void assignParameters() {
		if (getParameters()!=null) {
			if (getParameters().containsKey("limit")) {
				try {
					this.limit= (Integer.valueOf(  ((String) getParameters().get("limit")).trim())).intValue();
				} catch (Exception e) {
					logger.error(e.getStackTrace());
					this.limit=0;
				}
			}
			if (getParameters().containsKey("type")) {
				this.type_classifier_name= ((String)getParameters().get("type")).trim();
			}
			if (getParameters().containsKey("type_member")) {
				this.type_member=((String)getParameters().get("type_member")).trim();
			}
			if (getParameters().containsKey("base_dir")) {
				this.base_dir=((String) getParameters().get("base_dir")).trim();
			}
			if (getParameters().containsKey("extension")) {
				this.extension_classifier_name=((String) getParameters().get("extension")).trim();
			}
		}

	}

	/** --------------------------------------------------------------------------------
	 */

	private void estimateTotalFiles() {
//		if (mean_n>0)
//			estimated_total = (long) (this.file_disk_size / mean_n);
//		else if (file_disk_size>0) 
//			estimated_total = (long) (this.file_disk_size / 200000); 
	}
	
	/** --------------------------------------------------------------------------------
	 */
	@Transactional
	private void processBatch() {
		
		if (this.err_count>500) { 
			setResult("Too many errors");
			setResultDetails(domain.getName() + "Too many errors");
			this.stop();
		}
		

		for (File file: list) {

			if (this.limit>0 && this.limit<=(this.counter+this.err_count)) {
				setProgress(100);
				setResult("OK");
				finalize(CommandState.COMPLETED);
				this.stop();
			}
			
			if (this.isStopped()) {
				list = new ArrayList<File>(BATCH_SIZE); 
				return;
			} 
			
			try {	
					total_files++;

					long start 			 = System.currentTimeMillis();
					String path 		 = file.getAbsolutePath();
					String md5_hex 		 = org.apache.commons.codec.digest.DigestUtils.md5Hex(path);
					
					try {
					
						ServiceLocator.getService(ValueLockerService.class).lock(path);
						
						KBFile existing_file = getContentDao().findFileByPath(path); 
	
						boolean exists=(existing_file!=null);
						
						if (!exists) {
							
									//KBFileImpl kb_file = new KBFileImpl();
									KBFileImpl kb_file = (KBFileImpl) ServiceLocator.getService(ContentFactoryService.class).createKBFileNoTrx(FilenameUtils.getName(path));
									kb_file.setOId(ServiceLocator.getService(ContentFactoryService.class).getResourceNewOId());
									kb_file.setTitle(FilenameUtils.getBaseName(path));
									kb_file.setDomain(getDomain());
									kb_file.setName(FilenameUtils.getName(path));
									kb_file.setState(ObjectState.ENABLED);
									kb_file.setLastModifiedUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
									kb_file.setLastModifiedOffsetDateTime(OffsetDateTime.now());
									kb_file.setSize(org.apache.commons.io.FileUtils.sizeOf(file));
									kb_file.setUrl(path);	
									
									try {
										if (mean_n1>0)
											this.mean_n = alpha * mean_n1 + beta * (double) kb_file.getSize();
										else
											this.mean_n = (double) kb_file.getSize();
										this.mean_n1 = mean_n;
										
										// long crc32 = org.apache.commons.io.FileUtils.checksumCRC32(file);
										// kb_file.setCRC32(Long.toHexString(crc32));
											
										
									} catch (Exception e1) {
										logger.error(e1.getStackTrace());
									}
															
									try {
											// Save File.para evitar problemas de Hibernate
											//
											//
											getContentDao().save(kb_file);
											
											
											// Create iDOC 
											//
											//
											IDoc idoc = (IDoc) ServiceLocator.getService(ContentFactoryService.class).create(idoc_template.getName(), false, false);
												
											idoc.setDomain(domain);		
											idoc.setName(md5_hex);	
											idoc.setState(ObjectState.ENABLED);
											idoc.setTitle(kb_file.getTitle());
											idoc.setAbstract(file.getAbsolutePath());
											idoc.setLastModifiedOffsetDateTime(OffsetDateTime.now());
											idoc.addFile(kb_file);
	

											// Extension
											//
											//
											if (getExtensionClassifier()!=null) {
													
												try { 
														Classifier ext = getExtensionClassifier(); 
														String extension = FilenameUtils.getExtension(path);
														DataSet ds = ext.getDataSet();
														
														DataSetMember member  = getContentDao().findMemberByValue(ds, extension);
														
														if (member ==null) {
															Object xmember = ServiceLocator.getService(ObjectFactoryService.class).createMember(ds);
															((DataSetMember) xmember).setStrValue(extension);
															((DataSetMember) xmember).getService(DOMObjectService.class).update();
															
															idoc.addClassification(getExtensionClassifier(), ((DataSetMember) xmember));
														} 
														else
															idoc.addClassification(getExtensionClassifier(), member);
												} catch (Exception e) {
													logger.error(e.getStackTrace());
												}
											}
											
											// Type
											//
											//
											if (getTypeClassifier()!=null) {
													DataSetMember dm = getTypeDataSetMember(); 
													if (dm!=null) 
														idoc.addClassification(getTypeClassifier(), dm);
											}
	
											// Path 	
											//
											//
											if (getClassifier()!=null) {
												DataSet dataset  = getClassifier().getDataSet();
												if (dataset!=null) {
													
													String folder = FilenameUtils.getFullPath(path); 
													String md5_folder_hex 	= org.apache.commons.codec.digest.DigestUtils.md5Hex(folder);
													DataSetMember idoc_dm = (DataSetMember) getContentDao().findModelObjectByName(DataSetMember.class, dataset, md5_folder_hex);
													if (idoc_dm==null) {
														KbeeValueMember dmx = (KbeeValueMember) ServiceLocator.getService(ObjectFactoryService.class).createMember(dataset);
														dmx.setValue(md5_folder_hex);
														dmx.getService(DOMObjectService.class).update();
														if (dmx!=null) 
															idoc.addClassification(getClassifier(), dmx);
													}
													else {
														idoc.addClassification(getClassifier(), idoc_dm);
													}
												}
											}
						
											// UPDATE
											//
											//
											idoc.getService(ContentService.class).update();
	
											this.counter++;
											long end = System.currentTimeMillis();
											
	
											if(this.total_files%51==0)
												this.estimateTotalFiles();
	
											
											long tot;
											
											if (this.limit>0) {
												tot=this.limit;
												if (tot>0)
													setProgress((int)( 100.0 * this.counter/tot));
											}
											else {
												tot=this.file_disk_size;
												if (tot>0)
													setProgress((int)(100.0 * this.total_files * this.mean_n / tot));
											}
											
											
											this.setStatusInfo(String.format("Total Processed: %7d.  Converted: %7d  -  Error: %7d  (last: %d ms, mean size: %6.2f KB)", this.total_files, this.counter, this.err_count, (end-start), this.mean_n / 1000.0 ));
											
											result_logger.info(file.getAbsolutePath() + "  " + String.valueOf(end-start) + " ms.");
											file_list_logger.info(file.getName());
	
						
											} catch (Exception e) {
												logger.error(e.getStackTrace());
												result_logger.info("ERROR. " + e.getMessage() + " . " +  file.getAbsolutePath());
												this.err_count++;
											}
											
								
								} else {
									this.already_exists++;
								}
					
					}	finally {
							ServiceLocator.getService(ValueLockerService.class).unlock(path);
					}
					
			} catch (RuntimeException e) {
				logger.error(e.getStackTrace());
			}
		}
		
		//list.clear();
		list = new ArrayList<File>(BATCH_SIZE);
	}
	
	/** --------------------------------------------------------------------------------
	 */
	private void process(File dir) {

		String directoryName = dir.getAbsolutePath() +  File.separator;
		
		String[] files = dir.list();
		
		if(files!=null) {
			
			for (int i = 0; i < files.length; i++) { 
		
				if (!this.isStopped()) {

					final File file = new File(directoryName + files[i]);
					
					if (file.isDirectory()) {
						
						process(file);
						
					} else {
			
						list.add(file);
						
						if (list.size()==BATCH_SIZE) {
							processBatch();
							((SessionFactory) ServiceLocator.getService(BeansService.class).getBean("sessionFactory")).getCurrentSession().clear();	
						}
					}
				}
				else {
					list.clear();
					list = new ArrayList<File>(BATCH_SIZE);
					return;
				}
			}
			
			if (list.size()>0) {
				processBatch();
				((SessionFactory) ServiceLocator.getService(BeansService.class).getBean("sessionFactory")).getCurrentSession().clear();
			}
		}
	}
	/** --------------------------------------------------------------------
	 */
	@Override
	public synchronized void stop() {
		super.stop();
	}

	/** --------------------------------------------------------------------
	 */
	public void setDomainId(Serializable id) {
		domainId = id;
	}
	
	/** --------------------------------------------------------------------
	 */
	public Serializable getDomainId() {
		return domainId;
	}
	
	/** --------------------------------------------------------------------
	 */

	public void setDomain(Domain domain) {
		this.domain = domain;
		domainId = domain.getId();
	}
	
	/** --------------------------------------------------------------------
	 */
	public boolean isRunning() {
	    	return this.running;
	}
	/** --------------------------------------------------------------------
	 */
	@Override
	public void execute() {
		this.thread = new Thread(this);
    	this.thread.setDaemon(false);
    	this.thread.setName(getName());
    	this.thread.setPriority(Thread.NORM_PRIORITY);
    	this.thread.start();
	}

	/** --------------------------------------------------------------------
	 */
	protected void setRunning(boolean value) {
    	this.running = value;
	}
	
	
	public Domain getDomain() {
		if (domain == null) {
			if (domainId == null) {
				domain = ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
			}	
			else
				domain = getContentDao().findDomainById(Long.valueOf(domainId.toString()));
		}
		return domain;
	}
	
	
	private Classifier getTypeClassifier() {

		if (calculated_type_classifier)
			return type_classifier;
		
		try {
			
			calculated_type_classifier = true;
			
			if (type_classifier_name==null)
				return null;
			
			String c1 = type_classifier_name.trim().toLowerCase();
			for(Classifier clasi: getContentDao().getClassifiers(getDomain())) {
				if (clasi.getName().trim().toLowerCase().equals(c1)) {
					type_classifier=clasi;
					return type_classifier;
				}
			}
			return null;
		}  catch (RuntimeException e) {
			logger.error(e.getStackTrace());
			return null;
		}
	}
	
	/** --------------------------------------------------------------------
	 * 
	 */
	private DataSetMember getTypeDataSetMember() {
			
		if (calculated_type_dataset_member)
			return type_dataset_member;
		
		try {
			calculated_type_dataset_member = true;
			type_dataset_member = (DataSetMember) getContentDao().findModelObjectByName(DataSetMember.class,  getTypeClassifier().getDataSet(), type_member);
			return type_dataset_member;
			
		}  catch (RuntimeException e) {
			logger.error(e.getStackTrace());
			return null;
		}
	}
	/** --------------------------------------------------------------------
	 */
	private Classifier getClassifier() {
		
		if (calculated_classifier)
				return classifier;
		try {
			calculated_classifier = true;
			if (classifier_name==null)
				return null;
			String c1 = classifier_name.trim().toLowerCase();
			for(Classifier clasi: getContentDao().getClassifiers(getDomain())) {
				if (clasi.getName().trim().toLowerCase().equals(c1)) {
					classifier=clasi;
					return classifier;
				}
			}
			return null;
		}  catch (RuntimeException e) {
			logger.error(e.getStackTrace());
			return null;
		}
	}

	/** --------------------------------------------------------------------
	 */
	private Classifier getExtensionClassifier() {
								
		if (calculated_extension_classifier)
				return extension_classifier;
		try {
			calculated_extension_classifier = true;
			if (extension_classifier_name==null)
				return null;
			String c1 = extension_classifier_name.trim().toLowerCase();
			for(Classifier clasi: getContentDao().getClassifiers(getDomain())) {
				if (clasi.getName().trim().toLowerCase().equals(c1)) {
					extension_classifier=clasi;
					return extension_classifier;
				}
			}
			return null;
		}  catch (RuntimeException e) {
			logger.error(e.getStackTrace());
			return null;
		}
	}
	
	
	/** --------------------------------------------------------------------
	 */
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	/** --------------------------------------------------------------------------------
	 */
	private void finalize(CommandState state) {

		setResultComments(
				"Total       : "  	+ String.valueOf(this.total_files) +
				". Converted : "    + String.valueOf(counter) + 
				". Errors    : "   	+ String.valueOf(this.err_count) +
				". Existing  : "   	+ String.valueOf(this.already_exists));
		setDateTerminated(OffsetDateTime.now());
		setState(state);
	}


}
