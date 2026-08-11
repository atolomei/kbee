package com.novamens.kbee.content.command;


import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Iterator;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.resource.KBFile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.event.LogEvent;
import com.novamens.indexer.java.FileIndexerService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.java.LogIndexerService;
import com.novamens.indexer.service.IndexerException;
import com.novamens.indexer.service.JavaIndex;
import com.novamens.kbee.metrics.KbeeSystemMetricsService;
import com.novamens.security.Identifiable;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.util.NumberFormatter;

public class ReindexCommand extends AbstractCommand {
			
	private Serializable domainId = null;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReindexCommand.class.getName());
	
	private int priority;
	private int max_elements  = 0;
	private Meter metric_reindex;   	// num requests lp in / sec
	
	private boolean do_not_su = false;
	
	private boolean include_attachments = false;
	
	private long total_objects = -1;
	private long total_indexed = 0;
	
	private Domain domain = null;
	
	public ReindexCommand(String statement) {
		setName("Reindex");
		setParameter("statement", statement);
	}
	
	public ReindexCommand(String statement, Domain domain) { 
		setName("Reindex " + domain.getName());
		setParameter("statement", statement);
		setDomain(domain);
	}
	
	public void setIncludeAttachments(boolean ia) {
		this.include_attachments=ia;
	}
	
	public boolean isIncludeAttachments() {
		return this.include_attachments;
	}
	
	public void setDoNotSu(boolean b) {
		this.do_not_su=b;
	}
	
	@Override
	public int getPriority() {
		return priority;
	}
	
	@Override
	public void setPriority(int p) {
		priority=p;	
	}

	public void setMaxElements(int  max_elements) {
		this.max_elements=max_elements;
	}
	
	public int getMaxElements() {
		return this.max_elements;
	}

	@Override
	public void execute() {
		
		Domain initialdomain = null;
		try {
		
			setDateStarted(OffsetDateTime.now());
			
			initialdomain = ServiceLocator.getService(UserService.class).getSessionUserProfile()!=null ? ServiceLocator.getService(UserService.class).getDomain() : null;
			
			int numberOfObjects = getNumbersOfObjectsToIndex();
			Double numberOfObjectsD = Double.valueOf(numberOfObjects);
			 
			Integer five_percent = Double.valueOf(numberOfObjectsD / 20.0).intValue();
			Integer ten_percent = Double.valueOf(numberOfObjectsD / 10.0).intValue();
			
			if (five_percent==0)
				five_percent = 1;
			else  if (five_percent>200)
				five_percent=200;
						
			if (ten_percent==0)
				ten_percent = 1;
			else  if (ten_percent>200)
				ten_percent=200;
			
			/** 
			El iterate hace una ejecucion lazy del query (leventa solo ids). Como es un query con cantidad de resultados potencialmente grande lo considero
			indispensable. Otra razon del query lazy es asegurarse que el objeto que se va a indexar se levante en el momento en que se indexa y no cuando se
			ejecute el query 
			*/ 
			@SuppressWarnings("deprecation")
			Iterator<?> iterator = getQuery().iterate();
		
			boolean done = numberOfObjects==0 ? true : false;
					
			int errors = 0;
			total_indexed = 0;
					
			while (iterator.hasNext() && !done && !isStopped()) {
				
				Object object = null;
				
				try {
					
					/** 
					El problema del query lazy es que solo levanta los atributos de la clase base 
				 	por lo que para indexarlo hay que transformar este proxy en el objeto real. 
					Esta accion genera un warning en el log pero segun parece no implica problemas.
					*/
					long start= System.currentTimeMillis();
					Object object1 = iterator.next();
					object = reload(object1);
					index(object);
					getMeterReindex().mark();
					onIndex(object);
					long end= System.currentTimeMillis();

					if (object instanceof Content) {
						if (logger.isDebugEnabled() || total_indexed % five_percent == 0) {
							StringBuilder str  = new StringBuilder();
							str.append( " [ "+String.format("%4d", end-start) + " ms - " + String.format("%6.2f", getProgress())  + " % ] | ");
							Content content = ((Content) object); 
							str.append("Id: " + String.format("%12d", content.getOId())+" / "+ String.format("%12d",content.getId()));
							str.append(" | " + String.format("%12s", content.getContentTemplate().getContentClassCode()));		
							str.append(" | " + (content.getTitle()!=null?content.getTitle():"null"));
							info(str.toString());
							logger.debug(str.toString());
						}
					}
					else if (object instanceof LogEvent) {
						if (getLogger().isDebugEnabled() || total_indexed % five_percent == 0) {
							StringBuilder str  = new StringBuilder();
							str.append( " [ "+String.format("%4d", end-start) + " ms - " + String.format("%6.2f", getProgress())  + " % ] | ");
							LogEvent o = (LogEvent) object; 
							str.append("Id: " + String.format("%16d", o.getId()));
							str.append("| Name: " + o.getDisplayName());
							info(str.toString());
							logger.debug(str.toString());
							
						}
					}
					else if (object instanceof Identifiable) {
						if (getLogger().isDebugEnabled() || total_indexed % five_percent == 0) {
							StringBuilder str  = new StringBuilder();
							str.append( " [ "+String.format("%4d", end-start) + " ms - " + String.format("%6.2f", getProgress())  + " % ] | ");
							Identifiable o = (Identifiable) object; 
							str.append("Id: " + String.format("%16d", o.getId()));
							str.append("| Name: " + o.getDisplayName());
							info(str.toString());
							logger.debug(str.toString());
						}
					}
					
				}
				catch (com.codesnippets4all.json.exceptions.JSONParsingException e) {
					error("Json object broken"+" | " + e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + e.getMessage());
					errors++;
				}
				
				catch (IndexerException e) {
					if (object!=null) {
						try {
							if (object instanceof Identifiable) {
								if (total_indexed % five_percent == 0) {
									StringBuilder str  = new StringBuilder();
									Identifiable o = (Identifiable) object; 
									str.append("Id: " + o.getId().toString());
									str.append("| Class: " + o.getClass().getSimpleName());		
									str.append("| Name: " + o.getDisplayName());
									error(str.toString()  + "[IndexerException]");
									
								}
							}
						} catch (Throwable x) {
							logger.error(x);
						}
					}
					logger.error(e);
				}
				
				catch (Exception e) {
						logger.error(e, "indexing");
					errors++;
				}
				catch (Error e1) {
					logger.error(e1);
					errors++;
				}
				
				finally {
					total_indexed++;
					setProgress(numberOfObjects>0?  total_indexed/numberOfObjectsD*100 : 0);
					done = (total_indexed >= numberOfObjects);
					if (total_indexed%100==0) {
						((SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory")).getCurrentSession().clear();
					}
				}
			}
			
			commit();
					
			if (!isStopped()) {

				info("Completed normally. Total " + String.valueOf(total_indexed) + " items");
				logger.info("Completed normally. Total " + String.valueOf(total_indexed) + " items");

				double p1m  = getMeterReindex().getOneMinuteRate();
				double p5m  = getMeterReindex().getFiveMinuteRate();
				double p15m = getMeterReindex().getFifteenMinuteRate();

				String rate_p = "1m. "  + NumberFormatter.formatNumber(p1m) + " | " +
						"5m. "  + NumberFormatter.formatNumber(p5m) + " | " +
						"15m. " + NumberFormatter.formatNumber(p15m) + " [" + NumberFormatter.formatNumber(getMeterReindex().getMeanRate())+" obj/sec ]";
				
				info(rate_p);
				logger.info(rate_p +" [obj/seg]");
				
				setResultComments(getResultComments((int)total_indexed, errors) + " \n " + rate_p );
				setProgress(100);
				setResult("OK");
				setState(CommandState.COMPLETED);
			}
			else {
				
				info("Stopped by the User");
				info("Total " + String.valueOf(total_indexed) + " items");
				
				logger.info("Stopped by the User");
				logger.info("Total " + String.valueOf(total_indexed) + " items");
				
				setResult("Cancelled by user.");
				setResultComments(getResultComments((int)total_indexed, errors));
				setState(CommandState.CANCELED);
			}
					
			setDateTerminated(OffsetDateTime.now());
		}
		catch (Exception e) {
			String s=e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + e.getMessage();
			error(s);
			setStatusInfo(getStatusInfo()!=null?(getStatusInfo()+" | "+s):s);
			setState(CommandState.ERROR);
			setResult("Error");
			setResultComments(s);
			
		}
		catch (Error e1) {
			error("ERROR -------------------------------------");
			error(e1.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			setResult("Error");
			setResultComments(e1.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			setState(CommandState.ERROR);
		}
		finally {
			
			setDateTerminated(OffsetDateTime.now());
			
			if (!do_not_su && initialdomain!=null)
				su(initialdomain);
			
			KbeeSystemMetricsService mt = ServiceLocator.getService(KbeeSystemMetricsService.class);
			mt.getMetrics().remove(ReindexCommand.class.getName()+".reindex-"+String.valueOf(getId()));
		}
	}

	public void setDomainId(Serializable id) {
		domainId = id;
	}
	
	public Serializable getDomainId() {
		return domainId;
	}
	
	public String getStatement() {
		return (String)getParameter("statement");
	}
	
	
	public int getNumbersOfObjectsToIndex() {
		try {
			if (total_objects<0) {
				BeansService beans = ServiceLocator.getService(BeansService.class);
					
				SessionFactory sf = (SessionFactory)beans.getBean("sessionFactory");
					 
				if (getStatement().toLowerCase().contains("order by")) {
					int end = getStatement().toLowerCase().split("order by")[0].length();
					String st = getStatement().substring(0, end);
					total_objects = ((Long)sf.getCurrentSession().createQuery("select count(*) " + st).uniqueResult()).intValue();
				}
				else { 
					total_objects = ((Long) sf.getCurrentSession().createQuery("select count(*) " + getStatement()).uniqueResult()).intValue();
				}	
					
				if (getMaxElements()>0 && (total_objects > getMaxElements())) { 
					total_objects = getMaxElements();
				}
			}
			return (int)total_objects;
		} 
		catch (Exception e) {
			error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " | "  + e.getMessage() + " | Can not estimate the number of objects to index");
			setStatusInfo(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " | "  + e.getMessage() );
			return 0;
		}
	}
	
	public void setDomain(Domain domain) {
		this.domain = domain;
		domainId = domain.getId();
	}
	
	public Domain getDomain() {
	 	if (domain == null) {
			if (domainId == null) {
				if (ServiceLocator.getService(UserService.class).getSessionUserProfile()==null)
					return null;
				domain = ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
			}	
			else
				domain = getContentDao().findDomainById(domainId);
		}
		return domain;
	}
	
	public void onIndex(Object object) {
		
	}
	
	@Override
	public long getTotalItems() {
		return getNumbersOfObjectsToIndex();
	}
	
	@Override
	public long getTotalItemsProcessed() {
		return total_indexed;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(this.getClass().getSimpleName());
		if (this.getStatement()!=null) { 
				str.append(" | ");
			str.append(this.getStatement());
		}
		return str.toString();
	}
	
	private void index(Object object) throws IndexerException {
		
		if (!(object instanceof DomainObject)) 
			return;
		
		Domain domain = ((DomainObject)object).getDomain();
		
		if (getDomainId()!=null) {
			if (domain==null || !domain.equals(getDomain())  )  {
				return;
			}
		}
		else {
			if (getDomain()==null || !domain.equals(getDomain())) {
				commit();
				su(domain);
			}
		}
		
		JavaIndex index = getIndex(object);
		
		index.index(object, !isIncludeAttachments(), true, true);
	}

	private JavaIndex getIndex(Object object) {
		JavaIndex index;
		if (object instanceof LogEvent) {
			index = (JavaIndex)getDomain().getService(LogIndexerService.class).getIndex();
		}
		else
		if (object instanceof KBFile) {
			index = (JavaIndex)getDomain().getService(FileIndexerService.class).getIndex();
		}
		else {
			index = (JavaIndex) getDomain().getService(JavaIndexerService.class).getIndex();
		}
		return index;
	}
	
	private void commit() throws IndexerException {
		if (getDomain()!=null) {
			//getDomain().getService(LogIndexerService.class).getIndex().commit();
			getDomain().getService(JavaIndexerService.class).getIndex().commit();
		}
	}
	
	private void su(Domain domain) {
		this.domain = null;
		String suusername;
		if (domain.getName()==null) {
			Domain realdomain = getContentDao().findDomainById(domain.getId());
			suusername = "root@" + realdomain.getName();
		}
		else {
			suusername = "root@" + domain.getName();
		}
		ServiceLocator.getService(SecurityService.class).authenticate(suusername);
	}
	
	private Query<?> getQuery() {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		SessionFactory sf  = (SessionFactory)beans.getBean("sessionFactory");
		Query<?> query = sf.getCurrentSession().createQuery(getStatement());
		return query;
	}
	
	private String getResultComments(int indexed, int errors) {
		StringBuilder str = new StringBuilder();
		str.append("Indexed " +String.valueOf(indexed)+" Objects. ");
		if (errors>0)
			str.append(" -  Errors: " +String.valueOf(errors)+" Objects. ");
		return str.toString();
	}
	
	private Object reload(Object object) {
		return getContentDao().reload(object);
	}

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private Meter getMeterReindex() {
		if (metric_reindex==null) {
			KbeeSystemMetricsService mt = ServiceLocator.getService(KbeeSystemMetricsService.class);
			metric_reindex = mt.getMetrics().meter(MetricRegistry.name(ReindexCommand.class, "reindex-"+String.valueOf(getId())));
		}
		return metric_reindex;
	}
}
