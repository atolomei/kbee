package com.novamens.kbee.content.service;


import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.query.DomainQuery2;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.CronExpressionJ8;
import com.novamens.service.ServiceLocator;

/**
 * 
 */
public class SemanticRelatedServiceRequest extends AbstractCronJobRequest {

	private static final long serialVersionUID = -5979134348669290073L;
	static Logger logger = LogManager.getLogger(SemanticRelatedServiceRequest.class.getName());

	static final int MAX = 1000;
	
	 public SemanticRelatedServiceRequest() {
		 	setName("Semantic Related Service");
		 }
	 /**
	  *  
	  * Leo los modificados en el día
	  * armo una lista de tags
	  * 
	  * busco todos los que tienen alguno de esos tags
	  * ordenados por cantidad de tags. asi vienen los 1000 mas relevantes primero
	  * 
	  * recalculo hasta 1000. La transaccionalidad se realiza.
	  * 
	  */
	@Override
	public void execute() {
		logger.info("Executing " + getName());
	    try {
	    List<Domain> domains = getContentDao().getDomains();
		for(Domain domain: domains) {
			if (domain.isEnabled()) {
				Query sq = new SemContentQuery(domain, getQueryIndex(domain));
				ResultSet rs = null;
				try {
					rs = sq.execute();
					int counter=0;
					
					int errno = 0;
					while (rs.hasNext() && counter++<MAX) {
						try {	
								Content nextco = ((Content) rs.next().getObject());
								logger.info("Calculating Semantic related " + nextco.getTitle());
								SemanticService service = nextco.getService(SemanticService.class);
								service.generateSemanticRelatedNoTrx();
								
						} catch (Throwable e) {
							logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + e.getMessage());
							if (errno++>10)
								break;
						}
					}
					logger.info("Done. Total: " + counter);
				}
				finally {
					if (rs!=null)
						rs.close();
					}
				}
			}
		} catch(Exception e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName()  + " | " + e.getMessage());
		} finally {
		}
	}
	public void setCronExpression(String expression) {
		super.setCronExpression(new CronExpressionJ8(expression));
	}
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	private Index getQueryIndex(Domain domain) {
		return domain.getService(JavaIndexerService.class).getIndex();
	}
	private class SemContentQuery extends DomainQuery2 {
		private static final long serialVersionUID = 40456546399167505L;
		public SemContentQuery(Domain domain, Index index) {
			super(domain, index);
			getParameters().put("type", "[text, idoc, question]");
			getParameters().put("sort", "modified");
			getParameters().put("head", "true");
			getParameters().put("state", String.valueOf(ObjectState.ENABLED.getId()));
			getParameters().put("domain", String.valueOf(domain.getId()));
			getParameters().put("ascending", "false");
		}
	}
}
