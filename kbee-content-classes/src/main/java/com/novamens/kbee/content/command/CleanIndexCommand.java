package com.novamens.kbee.content.command;



import java.time.OffsetDateTime;
import java.util.Collection;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hibernate.SessionFactory;
import org.hibernate.WrongClassException;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.KbeeJavaIndex;
import com.novamens.indexer.java.FileIndexerService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.IndexProxy;
import com.novamens.indexer.service.IndexerDocument;
import com.novamens.indexer.service.IndexerException;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.service.SolrIndex;
import com.novamens.util.KbeeRuntimeException;

import kbee.util.PropertiesFactory;
import net.bytebuddy.asm.Advice.This;


/**
 * <p>Cleans SolR index</p>
 *	statement : SolR clause 
 *  type:idoc
 *   
 *AbstractCommand
 *
 */
public class CleanIndexCommand extends AsyncCommand  {

								
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(CleanIndexCommand.class.getName());

	
	

	static int MAX = 5000000;
	static  {

		try {
			MAX = Integer.valueOf(PropertiesFactory.getInstance("kbee").getProperties().getProperty("command.clean.max", String.valueOf(MAX)).trim());
		} catch (Exception e) {
			logger.error(e);
			MAX =5000000;
		}
	}


	private Domain domain;
	private SessionFactory sf = null;
	
	
	private long total_files_to_process = 0;
	private int total_scanned 			= 0; 
	private int files_touched 			= 0;
	private boolean aborted 			= false;

	
	
	public CleanIndexCommand() {
		setName(this.getClass().getName());
	}

	@Override
	public long getTotalItems() {
        return total_files_to_process;
    }

    
	@Override
	public long getTotalItemsProcessed() {
        return this.total_scanned;
    }
	

	
	
	@Override
	protected  void initCommand() {
		super.initCommand();
		
		total_files_to_process 	= 0;
		total_scanned 			= 0; 
		files_touched 			= 0;
		aborted 				= false;
	}
	
	public CleanIndexCommand(String statement, String domain_id ) {
		setParameter("statement", statement);
		setParameter("domain", domain_id);
		setName(getClass().getSimpleName());
		setDescription("Clean SolR indexes. Scans up to " + String.valueOf(MAX) +" files");
		
	}

	public CleanIndexCommand(String statement, String index, String domain_id) {
		setParameter("statement", statement);
		setParameter("index", index);
		setParameter("domain", domain_id);
		setName(getClass().getSimpleName());
		setDescription("Clean SolR indexes. Scans up to " + String.valueOf(MAX) +" files");
	}
	
	
	@Override
	public void executeAsync() {

		logger.debug(getParameters().toString());
		

		KbeeJavaIndex index;
		
		
		try {
		
					initCommand();
					
					setDateStarted(OffsetDateTime.now());
					super.setState(CommandState.RUNNING);
					setProgress(0);
					
			
					this.sf = com.novamens.hibernate.session.Session.open();

					if (getDomain()==null) {
						logger.error("domain is null");
						this.setStatusInfo("domain is null");
						setState(CommandState.ERROR);
						throw new IllegalArgumentException("Domain is null");
					}
						

					
					ServiceLocator.getService(SecurityService.class).authenticate("root@" + getDomain().getName());
					
					if (getIndex()!=null && getIndex().trim().equals("file"))
						index = (KbeeJavaIndex)((IndexProxy) getDomain().getService(FileIndexerService.class).getIndex()).getIndex();
					
					else 
						index = (KbeeJavaIndex)((IndexProxy) getDomain().getService(JavaIndexerService.class).getIndex()).getIndex();	
			
					logger.debug("Starting Clean Indexes  -> " +  getStatement());
					this.setStatusInfo("Starting Clean Indexes  -> " +  getStatement());
			
					QueryResponse response = ((SolrIndex)index).select(getStatement(), null, null, true, 0, MAX, false, false, null);
					SolrDocumentList resultSet = response.getResults();
						
					this.files_touched=0;
						
					
					/**
					 * STATUS
							Status Activity	
							clean {"createdmember":"2014/7","kbfs":"1","created":"Fri Jul 25 17:53:33 UTC 2014","domain":"250","lastmodifieduser":"5252","modified":"Fri Jul 25 17:53:33 UTC 2014","title":"Hulbert","text":"Hulbert","titlephonetic":"Hulbert","type":"kbfile","modifiedmember":"2014/7","id":"kbfileimpl#328897","lastmodifiedtime":"1406310813000","_version_":"1701209014275145730","score":"2.809425E-7"}
							Threads	
					 * 
					 * 
					 *   createdmember":"2014/7"
					 *   
					 */
					//this.cantRes=resultSet.size();
						
					this.total_files_to_process=resultSet.size();
						
					
					if (this.total_files_to_process<1) {
						this.setStatusInfo("Total files is 0");
						Thread.sleep(2000);
						setState(CommandState.COMPLETED);
						return;
					}
		
						
						for (int i = 0; i<resultSet.size(); i++) {
						
							if (isStopped() || aborted)
								break;
						
							SolrDocument solrdocument = resultSet.get(i);
							String id = solrdocument.getFieldValue("id").toString();
							
							IndexerDocument document = new IndexerDocument();
							
							document.addField("id", id);
							document.setId(id);
							Object object = null;
							
							try {
								object = index.getObjectBuilder().build(document);
							}
							catch (WrongClassException e) {
								logger.error(e);
							}
							if (i%10==0) {
								logger.debug(String.valueOf(i+1)+"/"+ String.valueOf(this.total_files_to_process));
							}	
							
							if(object==null){
								index.delete(id);
								String s=toString(solrdocument);
								logger.debug("clean -> "+ s);
								this.setStatusInfo("clean "+ (s.length()>200 ? s.substring(0, 199) : s) );
								this.files_touched++;
							}
							
							
							if (i%100==0) {
								((SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory")).getCurrentSession().clear();
								this.setStatusInfo("sessionFactory->clear");
							}
							
							this.total_scanned++;
							
							setProgress( 100.0 * (double) this.total_scanned / (double) this.total_files_to_process);
							
						}	
						index.commit();
			
						
						setResult("ok");
						
						setResultDetails( 	" Total: " + String.valueOf(this.total_files_to_process) +
											" | Scanned:  " + String.valueOf(this.total_scanned) +
											" | Cleaned:  " + String.valueOf(this.files_touched) 
						          );

						
						setState(CommandState.COMPLETED);
						
		
			} catch (Throwable e) {
				logger.error(e);
				setResult(e.getClass().getSimpleName());
				setResultComments(e.getMessage());						

				setResultDetails( " Total:  " + String.valueOf(this.total_files_to_process) +
								  " | Scanned:  " + String.valueOf(this.total_scanned) +
								  " | Cleaned:  " + String.valueOf(this.files_touched) 
						          );
						          
				setState(CommandState.ERROR);
				
			
				
			} finally {
		
			if (sf!=null) {
				com.novamens.hibernate.session.Session.close();
				this.setStatusInfo("DB Session closed");
			}
			setDateTerminated(OffsetDateTime.now());
	
	
		
	
			
	
	
		}
	}
	
	
	
	public void setDomain(Domain domain) {
		this.domain=domain;
	}
	
	public Domain getDomain() {
		
		if (domain!=null)
			return domain;
		
		if (getParameters()==null || getParameter("domain")==null)
			return null;

		String domain_id= (String)getParameter("domain");
		
		domain=getContentDao().findDomainById(Long.valueOf(domain_id));
		
		
		if (domain==null)
			logger.error("Domain is NULL");
		
		return domain;
		
		// return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
		
	}

	public String getStatement() {
		
		if (getParameters()==null || getParameter("statement")==null)
			return null;
		
		return (String)getParameter("statement");
	}
	
	public String getIndex() {
		if (getParameters()==null || getParameter("index")==null)
			return null;
		return (String)getParameter("index");
	}
	

		
	private String toString(SolrDocument document) {
		StringBuffer string = new StringBuffer();
		string.append("{");
		int names = 0;
		for (String fieldname : document.getFieldNames()) {
			Collection<?> values = document.getFieldValues(fieldname);
			if (names++>0)
				string.append(",");
			string.append("\""+fieldname+"\":");
			if (values.size()>1) {
				string.append("[");
			}
			int v = 0;
			for (Object value : values) {
				if (v++>0)
					string.append(",");
				String stringvalue = value.toString();
				if (stringvalue.length()>50) {
					stringvalue = stringvalue.substring(0,49)+"....";
				}
				stringvalue = stringvalue.replace("\n", "");
				string.append("\""+stringvalue+"\"");
			}
			if (values.size()>1) {
				string.append("]");
			}
		}
		string.append("}");
		return string.toString();
	}
	
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(getClass().getSimpleName());
		str.append(" | ");
		str.append(getStatement());
		return str.toString();	
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

}
