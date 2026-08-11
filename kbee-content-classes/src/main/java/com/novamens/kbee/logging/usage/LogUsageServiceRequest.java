package com.novamens.kbee.logging.usage;


import java.util.List;

import com.novamens.content.model.DataSet;
import com.novamens.kbee.dependencies.DataSetLocatorByAlias;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.dom.KBFSStorageType;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.logging.usage.KbeeUsageStat;
import com.novamens.logging.usage.UsageStat;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.Batch;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrQuery;


/**
 * 
 *  <p>Log of resources of each domain every day, see {@link  UsageStatService}.<br />
 * Requests that are executed by the Scheduler's worker thread do not need to open a Hibernate Session or a Database Transaction because they are managed by the Scheduler </p>
 *  
 *  @see {@link Batch}
 *  @see {@link KbeeBatch}
 *  
 *     
 */			
public class LogUsageServiceRequest extends AbstractCronJobRequest  {
 
 	private static final long serialVersionUID = -4427241243588519532L;

 	static Logger logger = LogManager.getLogger(LogUsageServiceRequest.class.getClass().getName());

 	
 	public LogUsageServiceRequest() {
 			setName("Log daily usage");
 			setDescription("Logs daily usage (Hard Disk, Contents, Users, Resources) for every domain.");
 	}

 	/**
 	 * @see com.novamens.scheduler.ServiceRequest#execute()
 	 */
	@Override
	public void execute() {
	
		try {
			
					
				/** Requests that are executed by the Scheduler's worker thread do not need to open a Hibernate Session or a Database Transaction because they are managed by the Scheduler **/ 
				ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
		
				List<Domain> list = getContentDao().getDomains(ObjectState.ENABLED);
				
				logger.debug("Total domains : " + list.size());
				
				
		 		UsageStatService service = (UsageStatService) ServiceLocator.getService(BeansService.class).getBean("usageStatService");
		 		
		 		for (Domain domain: list) {
		 			
					if (!domain.getName().toLowerCase().equals("kbee")) {
						try {
		
							logger.debug( this.getClass().getName() +" | loggging usage stat  -> " + domain.getName());
			 				UsageStat stat = new KbeeUsageStat();
							
			 				stat.setDomainId(domain.getId());
							stat.setContents(getContentDao().getTotalContents(domain));

							//DataSet siteDS = (DataSet) new DataSetLocatorByAlias("Property", (Long)domain.getId()).resolveObject();
							
							//if(siteDS == null)
							//	siteDS = (DataSet) new DataSetLocatorByAlias("sitename", (Long)domain.getId()).resolveObject();
							//if(siteDS != null)
							//	stat.setBillableSites(getContentDao().getDataSetMemberWithContents(domain, siteDS));
							//else
							//								logger.error( this.getClass().getName() + " | No DataSet alias 'Property' nor 'sitename' was found for Domain -> " + domain.getName() );

							stat.setBillableUsers(getContentDao().getTotalBillableUsers(domain));

							/**
							 * External -> total externals
							 */
							// all versions
							long ex=getContentDao().getTotalExternalContents(domain);
							logger.debug(domain.getDisplayName() + " - TotalExternalContents -> " + String.valueOf(ex));
							stat.setExternalContents(ex);
							
							// only head
							stat.setExternalLibraryContents(getContentDao().getTotalExternalLibraryContents(domain));
							stat.setExternalArchiveContents(getContentDao().getTotalExternalArchiveContents(domain));
							stat.setExternalRecycleContents(getContentDao().getTotalExternalRecycleContents(domain));
							
							stat.setHardDisk(getContentDao().getTotalHardDisk(domain)); // KBFS1, KBFS2
							stat.setResources(getContentDao().getTotalResources(domain));
							stat.setGatewayResources(getContentDao().getTotalResources(domain, KBFSStorageType.External));
							stat.setUsers(getContentDao().getTotalUsers(domain));
											
							stat.setGatewayHardDisk(		getContentDao().getTotalHardDisk(domain, KBFSStorageType.External));
							stat.setKBFS2ArchiveHardDisk(	getContentDao().getTotalHardDisk(domain, KBFSStorageType.MinioArchive));
							stat.setGatewayHardDisk(		getContentDao().getTotalHardDisk(domain, KBFSStorageType.External));
							stat.setKBFS2HardDisk(			getContentDao().getTotalHardDisk(domain, KBFSStorageType.Minio));
						 
							stat.setOdilonHardDisk(getContentDao().getTotalHardDisk(domain, KBFSStorageType.Odilon));
							stat.setS3HardDisk(getContentDao().getTotalHardDisk(domain, KBFSStorageType.AmazonS3));
							
							
							stat.setSolRTotalContent(getSolRContentIndexItems(domain));
							
							// la TRX la hace el scheduler
							//
							service.nonTrxSave(stat);
							logger.debug(this.getClass().getName() +" | Stat: " + stat.toString());

							
			 			} catch (Exception e) {
							logger.error(this.getClass().getName() + " | "+ e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName()+ " | " + e.getMessage());
						//  
							// Database transactions that fail must propagate the exception
							// for the Scheduler to rollback and mark the request
							// 
							throw(e);
							
						}
					}
					else {
						
				 		try {
							
				 			// KBEE
							UsageStat gstat = new KbeeUsageStat();
							gstat.setDomainId(domain.getId()) ;
							gstat.setContents(getContentDao().getTotalContents());
		
							// all versions
							gstat.setExternalContents(getContentDao().getTotalExternalContents());
							
							// Database Size (for all domains)
							gstat.setDBUsage(getContentDao().getDatabaseSize());
							
							// head
							gstat.setExternalLibraryContents(getContentDao().getTotalExternalLibraryContents());
							gstat.setExternalArchiveContents(getContentDao().getTotalExternalArchiveContents());
							gstat.setExternalRecycleContents(getContentDao().getTotalExternalRecycleContents());
		
							// KBFS1, KBFS2
							//
							gstat.setHardDisk(				getContentDao().getTotalHardDisk(KBFSStorageType.KBFS1) +  getContentDao().getTotalHardDisk(KBFSStorageType.Minio)); 
							gstat.setKBFS2ArchiveHardDisk(	getContentDao().getTotalHardDisk(KBFSStorageType.MinioArchive));
							gstat.setGatewayHardDisk(		getContentDao().getTotalHardDisk(KBFSStorageType.External));
							
							gstat.setOdilonHardDisk(        getContentDao().getTotalHardDisk(KBFSStorageType.Odilon));
							gstat.setKBFS2HardDisk(			getContentDao().getTotalHardDisk(KBFSStorageType.Minio));
							 
							gstat.setResources(				getContentDao().getTotalResources());
							gstat.setGatewayResources(		getContentDao().getTotalResources(KBFSStorageType.External));
							gstat.setUsers(					getContentDao().getTotalUsers());

							// DB TRX done by scheduler
							service.nonTrxSave(gstat);
							
							logger.debug(this.getClass().getName() + " | " + "Stat: " + gstat.toString());
							
				 		} catch (Exception e) {
				 			logger.error(this.getClass().getName() + " | " + e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + e.getMessage());
				 			//  
							// Database transactions that fail must propagate the exception
							// for the Scheduler to rollback and mark the request
							// 
							throw(e);
						}
					}
		 		}
			} finally {
				logger.debug(this.getClass().getName() + " | " + "done.");
				/** there is no need to manage Hibernate Session because this Request is executed by a Scheduler's Batch */
					
			}
 	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	
	private long getSolRContentIndexItems(Domain domain) {
		try {
			
			final String did = String.valueOf(domain.getId()).trim();
			
			SolrQuery q = new SolrQuery(getQueryIndex(domain)) {
				private static final long serialVersionUID = 1L;
				@Override
				public String getStatement() {
					return "domain:"+did;
				}
				@Override
				public String getSolrStatement() {
					return "domain:" +did;
				}
			};
			return Long.valueOf(q.execute().size()).longValue();
		
		} catch (Exception e) {
			logger.error(e);
			return -1;
		}
	}
	
	private Index getQueryIndex(Domain domain) {
		return domain.getService(JavaIndexerService.class).getIndex();
	}

}
