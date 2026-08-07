package com.novamens.content.web.multidimensional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ObjectId;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.java.LogIndexerService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.IndexerException;
import com.novamens.indexer.service.JavaIndex;
import com.novamens.kbee.content.command.AbstractCommand;
import com.novamens.kbee.content.multidimensional.DateFacet.DateRange;
import com.novamens.security.Identifiable;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;

@Deprecated
public class ReindexDateRangeCommand extends AbstractCommand {
		
	static private Logger logger = LogManager.getLogger(ReindexDateRangeCommand.class.getName());

	private String facetName;

	public ReindexDateRangeCommand() {
		facetName="modifiedmember";	
	}
	
	
	public ReindexDateRangeCommand(String facet) {
		setName("Reindex Date Range Command");
		setFacetName(facet);
	}
	
	@Override
	public void execute() {
		 
		boolean isok = true;
		
		setDateStarted(OffsetDateTime.now());
		setState(CommandState.RUNNING);
		
		int counter = 0;
		int counter_data = 0;
		int counter_log = 0;
		
		try {

			ResultSet resultSet = null;

			
			logger.debug(this.toString());
			
			int numberOfObjects = getNumbersOfObjectsToIndex();
			Double numberOfObjectsD = Double.valueOf(numberOfObjects);

			try {
				if (!isStopped()) {
						for (Domain domain : getContentDao().getDomains()) {
							JavaIndex index =  (JavaIndex)domain.getService(JavaIndexerService.class).getIndex();
							SolrParametersQuery query = new SolrParametersQuery(index);
							query.getParameters().put(facetName, String.valueOf(DateRange.MONTH.value()));
							query.getParameters().put("domain", String.valueOf(domain.getId()));
							resultSet = query.execute();
							
							List<ObjectId> ids = new ArrayList<ObjectId>();
							int o = 0;
							while (resultSet.hasNext() && !isStopped()) {
								try {
									Object obj = resultSet.next().getObject();
									if (obj instanceof Identifiable)
										ids.add(new ObjectId(obj));
								} 
								finally {
									if (o++%100==0) {
										((SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory")).getCurrentSession().clear();
									}
								}
							}
							
							for (ObjectId id : ids) {
								try {
									Object obj = getContentDao().findObjectById(id);
									index.index(obj);
									
									if (logger.isInfoEnabled() && (obj instanceof Identifiable))
											logger.info(((Identifiable) obj).getDisplayName());
									
								} 
								finally {
									counter_data++;
									counter++;
									if (counter%100==0) {
										((SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory")).getCurrentSession().clear();
									}
									setProgress(numberOfObjects>0?  new Double (Double.valueOf(100*counter)/ numberOfObjectsD).intValue():100);
									if (isStopped())
										break;
								}
							}
							
							index.commit();
							logger.info("Indexed " + String.valueOf(counter_data) + " data objects");
		
							if (!isStopped()) {
								// Logs
								//
								index =  (JavaIndex)domain.getService(LogIndexerService.class).getIndex();
				 				query = new SolrParametersQuery(index);
								query.getParameters().put(facetName, String.valueOf(DateRange.MONTH.value()));
								query.getParameters().put("domain", String.valueOf(domain.getId()));
								
								resultSet = query.execute();
								
								ids = new ArrayList<ObjectId>();
								o = 0;
								while (resultSet.hasNext() && !isStopped()) {
									try {
										Object obj = resultSet.next().getObject();
										if (obj instanceof Identifiable)
											ids.add(new ObjectId(obj));
									} 
									finally {
										if (o++%100==0) {
											((SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory")).getCurrentSession().clear();
										}
									}
								}
								
								for (ObjectId id : ids) {
									try {
										Object obj = getContentDao().findObjectById(id);
										index.index(obj);
										if (logger.isInfoEnabled() && (obj instanceof Identifiable))
											logger.info(((Identifiable) obj).getDisplayName());
									} 
									finally {
										counter_log++;
										counter++;
										if (counter%100==0) {
											((SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory")).getCurrentSession().clear();
										}
										setProgress(numberOfObjects>0?  new Double (Double.valueOf(100*counter)/ numberOfObjectsD).intValue():100);
										if (isStopped())
											break;
									}
								}

								index.commit();
								logger.info("Indexed " + String.valueOf(counter_log) + " log objects");
							}
						}
				}
			}
			catch (IndexOutOfBoundsException | IndexerException e) {
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				logger.debug(e);
				isok = false;
				throw new RuntimeException(e);
			}
			finally {

				if (resultSet!=null) 
					resultSet.close();

				setProgress(numberOfObjects>0?  new Double (Double.valueOf(100*counter)/ numberOfObjectsD).intValue():100);

				
			}
		}	
		catch (Exception e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());

			setResult(e.getClass().getName());
			setResultComments(e.getMessage());
			isok = false;
		} 
		finally {
			
			setDateTerminated(OffsetDateTime.now());
			
			if (isok) {
				setProgress(100);
				setResult("OK");
				
				setResultComments("Indexed " +String.valueOf(counter)+" Objects. " + " (Data: " + String.valueOf(counter_data)+". Log: "+ String.valueOf(counter_log)+") ");
				setState(CommandState.COMPLETED);
				
				logger.info(super.getResult() + " | " + super.getResultComment());
				
			}
			else if (isStopped()) {
				info("Stopped by user.");
				logger.info("Stopped by user.");	
				setResult("Cancelled by User.");
				setResultComments("Indexed " +String.valueOf(counter)+" Objects. " + " (Data: " + String.valueOf(counter_data)+". Log: "+ String.valueOf(counter_log)+") ");
				setState(CommandState.CANCELED);
				
				logger.info(super.getResultComment());
			}
			else {
				setState(CommandState.ERROR);
				info("Error.");
				logger.info("Error.");	
			}
			
			//closeLogger();
		}
	}
	
	
	
	private int getNumbersOfObjectsToIndex() {
		
		ResultSet resultSet = null;

		int total = 0;
		
		JavaIndex index;
		SolrParametersQuery query;

		try {
				try {
					for (Domain domain : getContentDao().getDomains()) {
						index =  (JavaIndex)domain.getService(JavaIndexerService.class).getIndex();
						query = new SolrParametersQuery(index);
						query.getParameters().put(facetName, String.valueOf(DateRange.MONTH.value()));
						query.getParameters().put("domain", String.valueOf(domain.getId()));
						resultSet = query.execute();
						total += resultSet.size(); 
					}
				} catch (Exception e) {
					logger.error(e.getClass().getName(), e);
				}
				
				try {
					for (Domain domain : getContentDao().getDomains()) {
						index =  (JavaIndex)domain.getService(LogIndexerService.class).getIndex();
						query = new SolrParametersQuery(index);
						query.getParameters().put(facetName, String.valueOf(DateRange.MONTH.value()));
						query.getParameters().put("domain", String.valueOf(domain.getId()));
						resultSet = query.execute();
						total += resultSet.size(); 
					}
				} catch (Exception e) {
					logger.error(e.getClass().getName(), e);
				}
				return total;
				
		} finally {
			if (resultSet!=null)
				resultSet.close();
		}
		
	}

	public void setFacetName(String facet) {
		this.facetName = facet;
	}
	
	public String getFacetName() {
		return this.facetName; 
	}
	
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(getClass().getSimpleName());
		str.append(" | ");
		str.append(getFacetName());
		return str.toString();	
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
