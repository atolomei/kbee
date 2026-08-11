package com.novamens.kbee.content.command;

import java.time.OffsetDateTime;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.service.ContentService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;

/**
 * <p>This is a Async Command. It does not use Scheduler's Trx
 * For this reason it must Open its Hibernate Session
 * and it doesn't need to propagate SQL Exceptions</p>
 * 
 * <p>By default 6 hours for external contents and 12 months internal contents.</p>
 *
 */
public class RecycleBinCleanUpCommand extends AsyncCommand {
									
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(RecycleBinCleanUpCommand.class.getName());
	
	public RecycleBinCleanUpCommand() {
			setName(this.getClass().getName());
	}
	
	private Long days_internal_files;
	private Long hours_external_files;
	private Domain domain;
	
	long size = 0;
	long total_processed = 0;
	
	@Override
	public long getTotalItems() {
		return size;
	}
	
	
	public long getTotalItemsProcessed() {
		return total_processed;
	}
	
	
	public Domain getDomain() {
		return this.domain;
	}
	
	public void setDomain(Domain domain) {
		this.domain=domain;
	}
	
	public void setDaysInternalFiles(Long days) {
		this.days_internal_files=days;
	}
	
	public void setHoursExternalFiles(Long hours) {
		this.hours_external_files=hours;
	}

	Transaction transaction = null;
	
	
						
	private void removeInternalVersion(Content content) {
		
	}

	
	/**
	 * Todas las versiones pasadas
	 */
	private void removeHeadVersion(Content content) {
		content.getService(ContentService.class).deleteAllVersions(getDomain()==null?"Recycle Bin Clean Up Command (all versions)":" Empty Recycle Bin (all versions)");
	}

	/**
	 * sólo la working copy
	 * la anterior deberia pasar a ser unlocked
	 */
	private void removeWorkingCopy(Content content) {
		content.getService(ContentService.class).delete(getDomain()==null?"Recycle Bin Clean Up Command":" Empty Recycle Bin");
	}
	
	@Override
	protected void executeAsync() {

		this.total_processed = 0;

		int errors = 0;
			
		try {

			com.novamens.hibernate.session.Session.open();
			ServiceLocator.getService(SecurityService.class).authenticate("root@" + (getDomain()!=null?getDomain().getName(): "kbee"));
		
			if (days_internal_files==null) {
				String mn = getContentDao().findSystemParameterValueByKey( "recycle-bin-retention-internal-files-days", "365");
					try {
						days_internal_files =  Long.valueOf(mn);
					} catch (Exception e) {
						days_internal_files = Long.valueOf(365);
					}
			}


			if (hours_external_files==null) {
				String mn = getContentDao().findSystemParameterValueByKey( "recycle-bin-retention-external-files-hours", "6");
					try {
						hours_external_files =  Long.valueOf(mn);
					} catch (Exception e) {
						hours_external_files = Long.valueOf(6);
					}
			}

			OffsetDateTime oldest_internal;
			OffsetDateTime oldest_external;
				
			oldest_internal = (days_internal_files>0)  ? OffsetDateTime.now().minusDays(days_internal_files.longValue()) : OffsetDateTime.now();
			oldest_external = (hours_external_files>0) ? OffsetDateTime.now().minusHours(hours_external_files.longValue()) : OffsetDateTime.now();
				
				List<Content> list=(getDomain()==null) ? getContentDao().getRecycleBinContents(10000) : getContentDao().getRecycleBinContents(80000, getDomain());
 				
				this.size = Integer.valueOf(list.size()).longValue();
					
				if (size>0) {
					for (Content content: list) {
						
						try {
							transaction = beginTransaction();
							
							if (content.isExternal()) {

								if (content.getLastModifiedOffsetDateTime().isBefore(oldest_external)) {
									logger.debug(content.getTitle()+ " | " + String.valueOf(content.getId()) + " | " + content.getLastModifiedOffsetDateTimeColloquial());
									if (content.isHeadVersion())
										content.getService(ContentService.class).deleteAllVersions(getDomain()==null?"Recycle Bin Clean Up Command (all versions)":" Empty Recycle Bin (all versions)");
									else
										content.getService(ContentService.class).delete(getDomain()==null?"Recycle Bin Clean Up Command":" Empty Recycle Bin");
								}
							}
							else if (content.getLastModifiedOffsetDateTime().isBefore(oldest_internal)) {

								logger.debug(content.getTitle()+ " | " + String.valueOf(content.getId()) + " | " + content.getLastModifiedOffsetDateTimeColloquial());
									
									if (content.getWorkspace()!=null) {
										content.getService(ContentService.class).delete(getDomain()==null?"Recycle Bin Clean Up Command":" Empty Recycle Bin");
									}
									if (content.isHeadVersion()) {
										content.getService(ContentService.class).deleteAllVersions(getDomain()==null?"Recycle Bin Clean Up Command (all versions)":" Empty Recycle Bin (all versions)");
									}
									else {
										// refactorChain();
										// else if (((KbeeContent) content).getPreviousVersion()!=null) {
										//
										// 1 <- 2 [<- 3] <- 4 head
										// next version si no es null. apuntar previous version a previous version de este qe vamos a borrar
										//
										
										
										// TBA
										//
										logger.debug( " TBA not head " + (((KbeeContent) content).getTitle()));
										
										// content.getService(ContentService.class).delete(getDomain()==null?"Recycle Bin Clean Up Command":" Empty Recycle Bin");
									}
									
									this.total_processed++;
									
									setProgress(100.0 * new Double(total_processed).doubleValue() / new Double(size).doubleValue());
							}
							transaction.commit();
							setState(CommandState.COMPLETED);
							
						}
						catch (Exception e) {
							
							errors++;
							logger.error(e);
							
							if (!transaction.isCompleted())
								transaction.rollback();
							
							if (errors>200) {
								setState(CommandState.ERROR);
								break;
							}
						}
					}
				
					setResult("Deleted: " + String.valueOf(total_processed) + " Contents | Errors: " + String.valueOf(errors));
				}
			}
			catch (Exception e) {
				setResult(e.getClass().getSimpleName());
				setResultComments(e.getMessage());
				logger.error(e);
				setState(CommandState.ERROR);
				

		} finally {
			logger.debug("Deleted: " + String.valueOf(total_processed) + " Contents | Errors: " + String.valueOf(errors));
			setDateTerminated(OffsetDateTime.now());
			logger.debug("Duration: " + String.valueOf(getDuration()/1000)+" ms");
			com.novamens.hibernate.session.Session.close();	
			setStatusInfo("DB Session closed.");
		}
	}

	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
