package kbee.objectstorage.command;


import java.io.InputStream;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.novamens.content.command.CommandState;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbfs.FileServerMinio;
import com.novamens.kbfs.FileServerOdilon;
import com.novamens.kbfs.KBFSService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

public class ObjectStorageDomainEncryptCommand extends ObjectStorageCommand {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ObjectStorageDomainEncryptCommand.class.getName());

	private static String executing_thread; 
	private static AtomicBoolean is_executing = new AtomicBoolean(false);
							
	private SessionFactory sf = null;
	
	private long total_files_to_process = 0;
	private int total_items 			= 0;
	private int total_scanned 			= 0; 
	private int files_touched 			= 0;
	private int files_not_found			= 0;
	private int file_db_errors 			= 0; 

	private boolean aborted 			= false;

	private List<Serializable> list_ids;
	private StringBuilder relevant_errors = new StringBuilder();

	public ObjectStorageDomainEncryptCommand() {
		setName("ObjectStorageDomainEncryptCommand");
		setDescription("This command encrypts unencrypted files from a Domain, starting from the newest files up to the limit provided as parameter.");
	}

	@Override
	public long getTotalItems() {
        return this.total_items;
    }

	@Override
	public long getTotalItemsProcessed() {
        return this.total_scanned;
    }
	
	@Override
	public void stop() {
		super.stop();
		aborted = true;
	}

	@Override
	protected  void initCommand() {
		super.initCommand();
		
		total_files_to_process 	= 0;
		total_items 			= 0;
		total_scanned 			= 0; 
		files_touched 			= 0;
		files_not_found			= 0;
		file_db_errors 			= 0;
		aborted 				= false;
		relevant_errors = new StringBuilder();
		list_ids = null;
	}
	
	@Override
	protected void executeAsync() {
		try {
				
			if (is_executing.get()) {
				relevant_errors.append("Can not execute this Command while another instance is under execution");
				throw new KbeeRuntimeException("Can not execute this Command while another instance is under execution");
			}
			
			is_executing.set(true);
			executing_thread = super.getId().toString();
			
			initCommand();
			
			setDateStarted(OffsetDateTime.now());
			super.setState(CommandState.RUNNING);
			setProgress(0);

			// open Hibernate Session
			//
			this.sf = com.novamens.hibernate.session.Session.open();
			
			// Authenticate
			//
			ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");  

			if (getTargetDomain()==null) {
				super.setState(CommandState.ERROR);
				relevant_errors.append("Domain is null");
				throw new IllegalArgumentException("Domain is null");
			}
			
			
			if (!getTargetDomain().isEncryptFiles()) {
				logger.error("Target Domain must have encrypt files to true " + getTargetDomain().getName());
				relevant_errors.append("Target Domain must have encrypt files to true -> " + getTargetDomain().getName());
				throw new IllegalArgumentException("Target Domain must have encrypt files to true -> " + getTargetDomain().getName());
			}
			
				
			Query<?> query = null;
																										
			logger.debug("from KBFileImpl K where K.isEncrypted=false and K.domain.id=" + String.valueOf(getTargetDomain().getId()) + " and K.exists_in_object_storage=true  order by K.lastModifiedDate desc");
			query = sf.getCurrentSession().createQuery("from KBFileImpl K where K.isEncrypted=false and K.domain.id=" + String.valueOf(getTargetDomain().getId()) + " and K.exists_in_object_storage=true order by K.lastModifiedDate desc");
			
			this.setStatusInfo("Calculating size");
			
			logger.debug("select count(*) from KBFileImpl K where K.isEncrypted=false and K.exists_in_object_storage=true and K.domain.id=" + String.valueOf(getTargetDomain().getId()));
			this.total_files_to_process = ((Long) (sf.getCurrentSession().createQuery("select count(*) from KBFileImpl K where K.isEncrypted=false and K.exists_in_object_storage=true and K.domain.id=" + String.valueOf(getTargetDomain().getId())).uniqueResult())).longValue();

			logger.debug("Total unencrypted Files -> " + String.valueOf(this.total_files_to_process));
			
			this.setStatusInfo("Total unencrypted Files -> " + String.valueOf(this.total_files_to_process) + 
					(getMaxToProcess()>0 ?  (" (max to Process:  " + String.valueOf(getMaxToProcess()) +")"): ""));
			
			if (getMaxToProcess()>0) {
				query.setMaxResults(getMaxToProcess());
				if (this.total_files_to_process>getMaxToProcess())
					this.total_files_to_process=getMaxToProcess();
			}

			this.total_items = Math.toIntExact(this.total_files_to_process);
			
			List<?> srclist = query.list();
			
			this.setStatusInfo("Starting processing " + String.valueOf(this.total_files_to_process) + " files");
			logger.debug("Starting processing " + String.valueOf(this.total_files_to_process) + " files");

			list_ids = new ArrayList<Serializable>();
			
			for (Object kfile: srclist)
				list_ids.add( ((KBFileImpl) kfile).getId());	

			srclist  =null;
			
			for (Serializable kfile_id: list_ids) {

				try {
					if (isStopped() || aborted)
						break;
					
					this.total_scanned++;
					
					KBFileImpl file = (KBFileImpl) getContentDao().findResourceById(KBFileImpl.class, kfile_id);
					
					if (!file.getIsEncrypted()) { 
						encryptFile(file);
					}
					
					if (this.total_items>0)
						super.setProgress(100.0 * (double) this.total_scanned / (double) this.total_items);
					
					logger.debug("Scanned " + String.valueOf(this.total_scanned) + " / " +  String.valueOf(this.total_files_to_process) + ". Encrypted: " +  String.valueOf(this.files_touched));
					
					if (this.file_db_errors>10) { 
						this.setStatusInfo("Too many database errors. Command aborted");
						this.setResultComments("Too many database errors. Command aborted " + relevant_errors.toString());
						aborted=true;
					}
					
				} catch (Exception e) {
					logger.error(e);
					if (this.file_db_errors>10) {
						aborted=true;
						this.setResultComments(e.getClass().getName() + " | " + relevant_errors.toString());
					}
				}
			}
			
			
			if (!aborted && !isStopped()) {
				setProgress(100);
				setResult("OK");
				setState(CommandState.COMPLETED);
				setDateTerminated(OffsetDateTime.now());
				setResultDetails(
						"Total         : " + String.valueOf(this.total_files_to_process) + " " +
						"| Processed   : " + String.valueOf(this.total_scanned) + " " +
						"| Encrypted   : " + String.valueOf(this.files_touched) + " " +
						"| File error  : " + String.valueOf(this.files_not_found)  + " " +
						"| DB Error    : " + String.valueOf(file_db_errors));
			}
			else {
				
				if (aborted) {
					setResult("Error");
					setState(CommandState.ERROR);
					setDateTerminated(OffsetDateTime.now());												
					setResultDetails("Encrypted " +String.valueOf(this.files_touched)+" Objects. Total Files to process: " + String.valueOf(this.total_files_to_process) + " File error  : " + String.valueOf(this.files_not_found)  + ". DB  Errors: " + String.valueOf(file_db_errors));

				} 
				else {
					setResult("Canceled by user");
					setState(CommandState.CANCELED);
					setDateTerminated(OffsetDateTime.now());								
					setResultDetails("Encrypted " +String.valueOf(this.files_touched)+" Objects. Total Files to process: " + String.valueOf(this.total_files_to_process) + " File error  : " + String.valueOf(this.files_not_found)  + ". DB  Errors: " + String.valueOf(file_db_errors));
				}
			}
			
			
			logger.debug("Ending Command execution " + getName());

			
		} catch (Throwable e) {
			logger.error(e);
			setResult(e.getClass().getSimpleName());
			setResultDetails(e.getMessage());						
			setResultDetails("Encrypted " +String.valueOf(this.files_touched)+" Objects. Total Files to process: " + String.valueOf(this.total_files_to_process) + " File error  : " + String.valueOf(this.files_not_found)  + ". DB  Errors: " + String.valueOf(file_db_errors));
			setState(CommandState.ERROR);
			setDateTerminated(OffsetDateTime.now());
		
			
		} finally {
			
			if (sf!=null) {
				com.novamens.hibernate.session.Session.close();
				this.setStatusInfo("DB Session closed");
			}
			
			setResultComments(relevant_errors.toString());
			setDateTerminated(OffsetDateTime.now());
			
			if (is_executing.get() && executing_thread !=null && executing_thread.equals(super.getId().toString())) {
					is_executing.set(false);
					executing_thread=null;
			}
		}
	}

	/**
	 * @param kbfile
	 */
	private void encryptFile(KBFileImpl kbfile) {
	
		this.setStatusInfo("Encrypted: " + String.valueOf(this.files_touched) + " | File Error: " + String.valueOf(this.files_not_found) + " | DB Error: " +  String.valueOf(file_db_errors));
		logger.debug("Processing -> " +kbfile.getName() + " | " + kbfile.getUrl());
		
		// ------------------------------------------------------------------------------------
		//  save encrypted archive
		// ------------------------------------------------------------------------------------

	    KBFSService service = kbfile.getService(KBFSResourceService.class).getKBFSService();
	      
		String b_name = kbfile.getBucketName();
		String o_name = kbfile.getObjectName();
		
		int originalShard = kbfile.getShard();

		
        // get existing file
        // generate new encrypted file

		try (InputStream is = kbfile.getService(KBFSResourceService.class).getObject()) {
					kbfile.getService(KBFSResourceService.class).putObject(kbfile.getFileName(), is);
		} catch (Throwable e) {
			logger.error(e);
			files_not_found++;
			return;
		}


		// ------------------------------------------------------------------------------------
		//  save KBFile with encrypted archive
		// ------------------------------------------------------------------------------------

		String new_b_name = kbfile.getBucketName();
		String new_o_name = kbfile.getObjectName();
		
		try {
					// save new KBFile with encrypted file
					getContentDao().saveTX(kbfile);
											
		} catch (Throwable e) {

						/**
						 * if the KBFile can not be saved, it keeps the original reference to the unencrypted resource
						 * the new encrypted archive is headless and has to be removed 
						 */
						logger.error(e);
						file_db_errors++;
					
						// we have to delete the encrypted file because it will not be used due to a DB error
						//
						if (!new_o_name.equals(o_name)) {
								try {
									kbfile.getService(KBFSResourceService.class).getKBFSService().removeObject(new_b_name, new_o_name);
									
								} catch (Exception e1) {
									relevant_errors.append((relevant_errors.length()>0?" | ":"") + " FATAL ERROR can not remove -> " + new_b_name + "/" + new_o_name);
									logger.error(e1);
									files_not_found++;
									this.aborted=true;
								}
						}
						
						relevant_errors.append( (relevant_errors.length()>0?" | ":"")+ kbfile.getName());
						return;
			}
			
		// ------------------------------------------------------------------------------------
		//  remove original unEncrypted Archive
		// ------------------------------------------------------------------------------------
		try {

			if ((new_o_name!=null) && (!new_o_name.equals(o_name))) {
				
				// ---------------------------------------------------
				// remove the unencrypted file from the object service
				//
				logger.debug("Deleting.  b-> " +b_name + " | o->" + o_name);
		        
				if (service instanceof FileServerMinio) {
				    FileServerMinio original_file_server_minio = ServiceLocator.getService(FileServerMinio.class);
	                original_file_server_minio.removeObject(originalShard, b_name, o_name);    
				}
				else if (service instanceof FileServerOdilon) {
				    FileServerOdilon original_file_server_odilon = ServiceLocator.getService(FileServerOdilon.class);
                    original_file_server_odilon.removeObject(originalShard, b_name, o_name);
				}
			}
			else {
				logger.debug("FILE NOT DELETED  b-> " +b_name + " | o->" + o_name);
			}
			
			this.files_touched++;
				
		} catch (Throwable e) {
				files_not_found++;
				this.aborted=true;
				logger.error(e);
				relevant_errors.append((relevant_errors.length()>0?" | ":"") + " FATAL ERROR can not remove original resource -> " + b_name + "/" + o_name);
		}
	}
}
