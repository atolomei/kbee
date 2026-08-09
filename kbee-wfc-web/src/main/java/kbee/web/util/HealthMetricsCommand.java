package kbee.web.util;

import java.time.OffsetDateTime;
import java.util.Locale;


import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.indexer.java.FileIndexerService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.java.LogIndexerService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.metrics.domain.DomainMetricsService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.util.NumberFormatter;
import kbee.web.query.ContentBaseQuery;

/**
 * Database <-> SolR
 * Domain metrics
 */
public class HealthMetricsCommand extends AsyncCommand {

	static final String total_library_query = "select count (*) from content where ishead = true and state = 1";
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(HealthMetricsCommand.class.getName());

	public HealthMetricsCommand() {
		setName(this.getClass().getName());
	}

	
	@Override
	protected void executeAsync() {

		StringBuilder str = new StringBuilder();
		
		try {

			com.novamens.hibernate.session.Session.open();
			ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");

			setState(CommandState.RUNNING);
			logger.debug("Starting Command execution " + getName());

			setDateStarted(OffsetDateTime.now());
			setProgress(0);

			Integer total_library = getContentDao().executeCountNativeQuery(total_library_query);
			
			ContentBaseQuery cbase_solr = new ContentBaseQuery(getQueryIndex());  // state=1, head=true
			cbase_solr.getFilterParameters().remove("domain");
			ResultSet res= cbase_solr.execute(); 
			
			int total_db = total_library.intValue(); 
			int total_solr = res.size();

			logger.debug("Db -> " + NumberFormatter.formatNumber(total_db,Locale.ENGLISH) +  " | Solr  -> " + NumberFormatter.formatNumber(total_solr,Locale.ENGLISH));
			str.append("Db  -> " + NumberFormatter.formatNumber(total_db,Locale.ENGLISH) +  " | Solr  -> " + NumberFormatter.formatNumber(total_solr,Locale.ENGLISH));
			
			ServiceLocator.getService(DomainMetricsService.class).setDBSolrCheck( total_db, total_solr, OffsetDateTime.now());
			
			logger.debug("calculating all domain metrics");
			ServiceLocator.getService(DomainMetricsService.class).forceCalculateAll();
			
			setDateTerminated(OffsetDateTime.now());
			setProgress(100.0);
			setResult("OK");
			setState(CommandState.COMPLETED);

		} catch (Exception e) {
			logger.error(e);
			
			setResult(e.getClass().getName());
			setState(CommandState.ERROR);

		} finally {

			com.novamens.hibernate.session.Session.close();
			setStatusInfo("DB Session closed.");
			setResultComments(str.toString());

			if (!isStopped()) {
				

			} else {
				setResult("Cancelled by User");
				setState(CommandState.CANCELED);
			}

	
		}
	}

	
	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}


	protected Index getFileIndex() {
		return getDomain().getService(FileIndexerService.class).getIndex();
	}

	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}

	protected Index getAuditIndex() {
		return getDomain().getService(LogIndexerService.class).getIndex();
	}	
	
	

	


}
