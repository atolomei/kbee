package com.novamens.kbee.content.command;

import java.io.IOException;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.resource.KBFile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.logging.UpdateEvent;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.SimpleImageInfo;

public class ImageWidthHeightCommand extends AbstractCommand  {

	static private Logger DBLogger = LogManager.getLogger("DBEventLogger");	
	static Logger logger = LogManager.getLogger(ImageWidthHeightCommand.class.getName());

	private Serializable domainId = null;
	private Domain domain = null;

	private int counter = 0;
	private int file_counter = 0;
	private int file_errors = 0;
	private int err_count = 0;
	private boolean aborted = false;
	private int size = 0;

	
	public ImageWidthHeightCommand(String statement) {
		setName("Image Width & Height generator");
		setParameter("statement", statement);
		setPriority(SchedulerService.HIGH_PRIORITY);

	}
	
	public ImageWidthHeightCommand(String statement, Domain domain) {
		setName("Image Width & Height generator");
		setParameter("statement", statement);
		setDomain(domain);
		setPriority(SchedulerService.HIGH_PRIORITY);
	}

	
	@Override
	public void execute() {
		
		setDateStarted(OffsetDateTime.now());

		setProgress(0);

		BeansService beans = ServiceLocator.getService(BeansService.class);
		SessionFactory sf = (SessionFactory)beans.getBean("sessionFactory");
		
		try {
			
			this.counter=0;
			this.size = ((Long)sf.getCurrentSession().createQuery("select count(*) " + getStatement()).uniqueResult()).intValue();
			Query query = sf.getCurrentSession().createQuery(getStatement());
			Iterator<?> iterator = query.iterate();

			List<Object> list = new ArrayList<Object>();
			
			while (iterator.hasNext() && !isStopped() && !aborted) {
				Object object1 = iterator.next();
				list.add(object1);
				if (list.size()==50) {
					processList(list);
					list.clear();
				}
				
				if (err_count>100) 
					aborted=true;
				
			}
			
			if (list.size()>0 && !aborted) {
				processList(list);
				list.clear();
			}
			
			
			if (!aborted) {
				setProgress(100);
				setResult("OK");
				setState(CommandState.COMPLETED);
				setDateTerminated(OffsetDateTime.now());
				setResultComments("Processed " +String.valueOf(counter)+" Objects. Total Files: " + String.valueOf(file_counter) + ". Errors: " + String.valueOf(file_errors));
			}
			else {
				setResult("Error");
				setState(CommandState.ERROR);
				setDateTerminated(OffsetDateTime.now());
				setResultComments("Reached 100 Errors, aborted !    Processed " +String.valueOf(counter)+" Objects. Total Files: " + String.valueOf(file_counter) + ". File: " + String.valueOf(file_errors));
			}
		
		} catch ( org.hibernate.QueryException |
				NullPointerException e) {
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				setResult(e.getClass().getSimpleName());
				setResultDetails(e.getMessage());
				setResultComments("Processed " +String.valueOf(counter)+" Objects. ");
				setState(CommandState.ERROR);
				setDateTerminated(OffsetDateTime.now());
		}
		
		logger.debug("Ending Command execution " + getName());
	}
	

	
	
	private boolean isElegible(Object object) {
		
		if (object==null)
			return false;
				
		if ( (object instanceof DomainObject)               && 
			 (((DomainObject)object).getDomain() != null)   && 
			 (((DomainObject)object).getDomain().equals(getDomain())) 
		   ) 
			return true;

		return false;
	}
	
	/** -----------------------------------------------------------------------------------
	 * 
	 * @param list
	 */
	private void processList(List<Object> list) {

		BeansService beans = ServiceLocator.getService(BeansService.class);
		SessionFactory sf = (SessionFactory)beans.getBean("sessionFactory");
		
		Session session = sf.openSession();
		Transaction transaction = session.beginTransaction();

		try { 
			for (Object obj: list) {
				
				Object object = reload(obj);
				
				if (isElegible(object)) {
					
					try {
						if (object instanceof ResourceContainer) {
							List<Resource> resources = ((ResourceContainer) object).getResources();
							for (Resource res: resources) {
									if (res instanceof KBFile) {
										try {
											if (kbee.util.FSUtils.isImage(((KBFile) res).getFile())) {
												SimpleImageInfo imageInfo;
												int nw, nh;
												try {
														imageInfo = new SimpleImageInfo(((KBFile) res).getFile());
														nw  = imageInfo.getWidth();
														nh = imageInfo.getHeight();
														((KBFile) res).setWidth(nw);
														((KBFile) res).setHeight(nh);
														if (object instanceof Content) {
															 getContentDao().save(((Content) object));
															 DBLogger.info(new UpdateEvent(((Content) object)));
															 file_counter++;
														}
											
													} catch (IOException e) {
														nw = 0;
														nh = 0;
														file_errors++;
													}
											}
										} catch (IOException e) {
											logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
											file_errors++;
										}
									}
							}
						}
					} catch (Exception e) {
						logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
						err_count++;
						file_errors++;
					}
				}
				
				
				logger.info("[ " + String.valueOf(getProgress())+"% ]");
				counter++;
				setProgress(size>0?(int) 100*counter/size:100);
			}	
		}
		finally {
			if (session!=null && transaction != null && session.isOpen()) {
				session.flush();
				transaction.commit();
				session.close();
	        }
		}
	}

	/** -----------------------------------------------------------------------------------
	 */
	public void setDomainId(Serializable id) {
		domainId = id;
	}

	/** -----------------------------------------------------------------------------------
	 */
	public Serializable getDomainId() {
		return domainId;
	}

	/** -----------------------------------------------------------------------------------
	 */

	public String getStatement() {
		return (String)getParameter("statement");
	}

	/** -----------------------------------------------------------------------------------
	 */

	public void setDomain(Domain domain) {
		this.domain = domain;
		domainId = domain.getId();
	}

	/** -----------------------------------------------------------------------------------
	 */

	public Domain getDomain() {
		if (domain == null) {
			if (domainId == null) {
				domain = ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
			}	
			else
				domain = getContentDao().findDomainById(domainId);
		}
		return domain;
	}

	/** -----------------------------------------------------------------------------------
	 */

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	/** -----------------------------------------------------------------------------------
	 */

	private Object reload(Object object) {
		return getContentDao().reload(object);
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
