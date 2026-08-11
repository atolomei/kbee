package com.novamens.kbee.content.indexer;


import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hibernate.SessionFactory;
import org.hibernate.WrongClassException;

import com.novamens.beans.BeansService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.java.KbeeJavaIndex;
import com.novamens.indexer.service.IndexProxy;
import com.novamens.indexer.service.IndexerDocument;
import com.novamens.indexer.service.IndexerException;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.security.Identifiable;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.service.SolrIndex;
import com.novamens.system.parameters.SystemParameterService;


/**
 * This Cron Job cleans up SolR indexes for all object classes.
 * It should be scheduled to execute once a week, or once a month.  
 * It should take 1 - 10 minutes depending of the size of the Database.
 *
 */
public class CleanUpIndexesRequest extends AbstractCronJobRequest {
		
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(CleanUpIndexesRequest.class.getName());

 	private Integer cantRes;
 	private Integer total;
	private Integer deleted;
	private Integer errors;
	private Integer max_to_scan;
	
	/**
	 * 
	 */
 	public CleanUpIndexesRequest() {
 			setName("Clean Up SolR Indexes");
 			setDescription("Clean Up SolR Indexes (DataSetMember, User, Group, Rule, Content, DataSet, Classifier, Attribute, Domain, Library, Role)");
 	}
 	
 	/**
 	 * @see com.novamens.scheduler.ServiceRequest#execute()
 	 */
	@Override
	public void execute() {
	
		ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");

		long start = System.currentTimeMillis();
		
				try {

					logger.debug("Starting Clean Up process");
			
					this.deleted=0;
					this.errors=0;
					this.total=0;
					
					

					// content
					clean("type:idoc");
					clean("type:text");
					clean("type:worknote");

					// principal
					clean("type:user");
					clean("type:group");
					clean("type:rule");
					clean("type:role");
					
					// model
					clean("type:dataset");
					clean("type:classifier");
					clean("type:attribute");
					clean("type:contenttemplate");

					// values
					clean("type:datasetmember");

					// values
					clean("type:domain");
					
					clean("type:cabinet");					

					// site
					clean("type:site");
					
	 			} catch (Exception e) {
					logger.error(e);
				}
				finally {
					logger.debug("Total time: " + String.valueOf( (System.currentTimeMillis()-start) / 1000.0)+" secs");
					logger.debug("------------------------------------------------------------------------------------------");
				}
 	}
	
	/** ---------------------------------------------
	 * 
	 * @param statement
	 */
	private void clean(String statement) {
		
		Domain domain = getDomain();
		
		KbeeJavaIndex index = (KbeeJavaIndex)((IndexProxy) domain.getService(JavaIndexerService.class).getIndex()).getIndex();
		
		this.cantRes = 0;
		
		try {

			QueryResponse response = ((SolrIndex)index).select(statement, null, "modified", true, 0, getMaxToScan().intValue() , false, false, null);
			
			SolrDocumentList resultSet = response.getResults();
			
			this.cantRes=resultSet.size()<getMaxToScan().intValue()?resultSet.size():getMaxToScan().intValue();
			logger.debug(statement + " | to scan -> " + String.valueOf(this.cantRes) +" | max to scan -> " +  String.valueOf(getMaxToScan().intValue()));
			
			for (int i = 0; i<resultSet.size(); i++) {

				try {
					
					SolrDocument solrdocument = resultSet.get(i);
					String id = solrdocument.getFieldValue("id").toString();
					IndexerDocument document = new IndexerDocument();
					document.addField("id", id);
					document.setId(id);
					Object object = null;
					
					try {
						object = index.getObjectBuilder().build(document);
						
						if (object instanceof Identifiable)
							logger.debug( ((Identifiable) object).getDisplayName());
					}
					catch (WrongClassException e) {
						logger.error(e);
					}	
					
					if(object==null) {
						index.delete(id);
						this.deleted++;
					}
	
					if (i%100==0) {
						((SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory")).getCurrentSession().clear();
					}
				}
				catch (Exception e) {
						logger.error(e);
						errors++;	
				}
				
				finally {
					this.total++;
				}
			}	
			index.commit();

		}
		catch (IndexerException e) {
			logger.error(e);
			errors++;
		}
		
		finally {
			
			String del  = "| Deleted : "+ String.format("%8d", this.deleted) + " | Errors  : "+ String.format("%8d",this.errors) + " | Cant    : "+ String.format("%8d",this.cantRes);
			logger.debug(String.format("%-40s", statement) + " " + del);
		}
	}
	

	public Integer getMaxToScan() {
		if (this.max_to_scan!=null)
			return this.max_to_scan;
		try {
				String m=ServiceLocator.getService(SystemParameterService.class).getParameter("index.cleanup.max", "5000");
				this.max_to_scan = Integer.valueOf(m);
		} catch (Exception  e) {
			this.max_to_scan = Integer.valueOf(50000);
		}
		
		return this.max_to_scan;
	}
	
	public Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
	}

}
