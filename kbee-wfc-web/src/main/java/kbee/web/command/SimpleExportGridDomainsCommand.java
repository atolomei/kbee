package kbee.web.command;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
 
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.content.command.CommandState;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.ResultSet;
import com.novamens.metrics.domain.DomainMetricsService;
import com.novamens.service.ServiceLocator;

import kbee.web.query.DomainsQuery;

public class SimpleExportGridDomainsCommand extends SimpleBaseExportGridCommand {

	static Logger logger = LogManager.getLogger(SimpleExportGridUsersCommand.class.getName());
	
	static public final String USEPARATOR = ", ";
	
	private int total = 0;

	public SimpleExportGridDomainsCommand(DomainsQuery query) {		
		super.setQuery(query);
	}
	
	
	@Override
	protected void executeExport() {

		BufferedWriter out = null;
		setState(CommandState.RUNNING);
		
		this.total = 0;
		
		try {
				if (getQuery()==null) {
					logger.error("query is null.");
					this.setState(CommandState.ERROR);
					this.setResultComments("query is null.");
					return;
				}
				
				
				if (! (getQuery() instanceof DomainsQuery)) {
					logger.error("query must be a DomainsQuery.");
					this.setState(CommandState.ERROR);
					this.setResultComments("query must be a DomainsQuery.");
					return;
				}
				
				
				long start = System.currentTimeMillis();
				
				String name = "domains-" + String.valueOf(start);
				
				File file = new File(getWorkingDir() + File.separator + name + ".csv");
				
				out = new BufferedWriter(new FileWriter(file));
		
				ResultSet results = getQuery().execute();
				
				total = results.size();
				
				if (total==0) {
					this.setState(CommandState.COMPLETED);
					this.setProgress(100);
					return;
				}
					
				int progress = 0;
				int counter  = 0;
				
				logger.info("Processing: " + String.valueOf(total));
				
 	
				// init export file
				//
				
				StringBuilder header = new StringBuilder();

				header.append("Id"); // 1 
				header.append(USEPARATOR);

				header.append("State"); // 2
				header.append(USEPARATOR);

				header.append("Created"); // 3 
				header.append(USEPARATOR);
				
				header.append("Modified"); // 4 
				header.append(USEPARATOR);

				header.append("Modified User"); // 5 
				header.append(USEPARATOR);
 				
				header.append("Name"); // 6
				header.append(USEPARATOR);

				header.append("Address"); // 7
				header.append(USEPARATOR);

				header.append("Website"); // 8 
				header.append(USEPARATOR); 

				header.append("Enabled");  // 9 
				header.append(USEPARATOR);
				
				header.append("Organization"); // 10 
				header.append(USEPARATOR);
				
				
 				header.append("KBFS"); //  
				header.append(USEPARATOR);

				header.append("Quota"); // 11
				header.append(USEPARATOR);

				header.append("Type"); // 12
				header.append(USEPARATOR);

				header.append("Displayname"); // 13 
				header.append(USEPARATOR);
				
				header.append("Max Users"); // 14 
				header.append(USEPARATOR);
				
				header.append("Cabinet Template"); // 15 
				header.append(USEPARATOR);
				
				header.append("Cabinet KBase"); // 16 
				header.append(USEPARATOR);

				header.append("Cabinet External"); // 17 
				header.append(USEPARATOR);
				
				header.append("API"); // 18 
				header.append(USEPARATOR);
				
				header.append("Time Zone"); // 19 
				header.append(USEPARATOR);
 
				header.append("Storage Type"); // 20 
				header.append(USEPARATOR);

 				header.append("External Id"); // 21 
				header.append(USEPARATOR);
 							
				header.append("Users");   
				header.append(USEPARATOR);

 				header.append("Contents");   
				header.append(USEPARATOR);

 				header.append("Resources");   
				header.append(USEPARATOR);

 				
				
				logger.info(header.toString());
				out.write(header.toString()+"\n");
				
				results = getQuery().execute();
	
				logger.debug("total: " + String.valueOf(results.size()));
				
				int nn = 0;
				int errno = 0;
				// Attributes are not exported
				//
				while (results.hasNext() && errno < 100) {
					
					try {
						
						Domain domain = (Domain) results.next().getObject();
						
						logger.debug("row: " + String.valueOf(++nn) + ". " + domain.getName());
						
						
						StringBuilder str = new StringBuilder();

						str.append(escape(String.valueOf(domain.getId())));  // 1
						str.append(USEPARATOR);

						try {
							if (domain.getState()!=null)
								str.append(domain.getState().getLabel()); // 2
							else
								str.append("null"); //
						} catch (Exception e) {
							logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + e.getMessage());
							str.append(escape(e.getClass().getSimpleName()));
						}
						str.append(USEPARATOR);

						OffsetDateTime created = domain.getCreationOffsetDateTime();
						if (created!=null)
							str.append(escape(dateformat.format(created))); // 3
						else
							str.append("");
						str.append(USEPARATOR);
						
						OffsetDateTime modi = domain.getLastModifiedOffsetDateTime();
						
						if (modi!=null)
							str.append(escape(dateformat.format(modi))); // 4
						else
							str.append("");
						str.append(USEPARATOR);

						// Id 13
						//
						try {
							str.append(escape(domain.getLastModifiedUser().getDisplayName())); // 5
						} catch (Exception e) {
							errno++;
							logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName()+ " | " + e.getMessage());
							str.append(escape(e.getClass().getSimpleName()));
						}
						str.append(USEPARATOR);
		
						str.append(escape(domain.getName()));  
						str.append(USEPARATOR);

						str.append(escape(domain.getAddress()));   
						str.append(USEPARATOR);
						
						str.append(escape(domain.getWebsite())); // 8 
						str.append(USEPARATOR);

						str.append(domain.isEnabled()?"yes":"no"); // 9 
						str.append(USEPARATOR);
						
						str.append(escape(domain.getOrganization())); // 10 
						str.append(USEPARATOR);
					
						str.append(escape(domain.getStorageType().getDisplayName())); // 11 
						str.append(USEPARATOR);
 						
						str.append(escape(String.valueOf(domain.getQuota())));  // 12
						str.append(USEPARATOR);

						str.append(escape(domain.getDomainType().getLabel()));  // 13  
						str.append(USEPARATOR);

						str.append(escape(domain.getDisplayName()));  // 14
						str.append(USEPARATOR);
						
						str.append(escape(String.valueOf(domain.getMaxUsers()))); // 14  
						str.append(USEPARATOR);
						
						str.append(domain.isCabinetTemplate()?"yes":"no"); // 15 
						str.append(USEPARATOR);
						
						str.append(domain.isCabinetKnowledgeBase()?"yes":"no"); // 16 
						str.append(USEPARATOR);

						str.append(domain.isCabinetExternal()?"yes":"no"); // 17
						str.append(USEPARATOR);
						
						str.append(domain.isAPIEnabled()?"yes":"no"); // 18
						str.append(USEPARATOR);
						
						str.append(escape(domain.getTimeZone()));   // 19
						str.append(USEPARATOR);
		 
						str.append(escape(domain.getStorageType().getDisplayName()));  // 20   
						str.append(USEPARATOR);

						str.append(escape(domain.getExternalId()));     
						str.append(USEPARATOR);
		 				
 						str.append(escape(getTotalUsersStr(domain)));     
						str.append(USEPARATOR);
 									
						str.append(escape(getTotalContentsStr(domain)));     
						str.append(USEPARATOR);
	
						str.append(escape(getTotalResourcesStr(domain)));     
						str.append(USEPARATOR);
 						
						out.write(str.toString()+"\n");
						
						
					} catch (Exception  e) {
						errno++;
						logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName()+ " | " + e.getMessage());
					}
					finally {
						counter++;
						if (total>0) 
							progress = 100 * counter/total;
						this.setProgress(progress);
					}
				}
				
				if (out!=null)
					out.close();
	
				sendEmail(file);
				
				setState(CommandState.COMPLETED);
				setProgress(100);
		}
		catch (Throwable e) {
				logger.error(e.getClass().getName(), e);
				this.setResult(e.getClass().getSimpleName());
				this.setResultDetails(e.getMessage());
				setState(CommandState.ERROR);
				return;
		
		} finally {
			if (out!=null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error(e.getClass().getName(), e);
				}
			}
			setDateTerminated(OffsetDateTime.now());
		}
	}


	
	@Override
	public String getTitle() {
		return this.getClass().getSimpleName();
	}
	
	private String getTotalUsersStr(Domain domain)
	{	
		if (getDomainMetricsServices().getResources(domain)>=0) 
				return  String.valueOf(getDomainMetricsServices().getUsers(domain));
		return "";
	}

	private String getTotalContentsStr(Domain domain) {
		if (getDomainMetricsServices().getResources(domain)>=0) 
			return  String.valueOf(getDomainMetricsServices().getContents(domain));
		return "";
	}
	
								
	private String getTotalResourcesStr(Domain domain) {
		if (getDomainMetricsServices().getResources(domain)>=0) 
			return String.valueOf(getDomainMetricsServices().getResources(domain));
		return "";
	}
	
 
	
 
	private DomainMetricsService getDomainMetricsServices() {
		return ServiceLocator.getService(DomainMetricsService.class);
	}

	
	protected String escape(String str) {
		if (str==null)
			return "";
		return str.replace(SEPARATOR, "");
	}


}
