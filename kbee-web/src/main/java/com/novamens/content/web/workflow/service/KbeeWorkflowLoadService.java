package com.novamens.content.web.workflow.service;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.content.base.Content;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.dao.ContentDao;

import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classification;

import com.novamens.content.service.workflow.ComplianceWorkflowRoleData;
import com.novamens.content.service.workflow.UserWorkLoadData;
import com.novamens.content.service.workflow.WorkflowLoadService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

import kbee.web.query.RulesQuery2;
import kbee.web.query.WorkspaceQuery;



/**
 *   user
 *   review (backup)
 *   audit (backup)
 *   
 *   1. Como indicar el classifier "effective date" unique name "effectivedate"
 *   2. Como hacer que funcione 
 */
public class KbeeWorkflowLoadService implements WorkflowLoadService, EventListener {
		
	static private Logger logger = LogManager.getLogger(KbeeWorkflowLoadService.class);

	//
	// Property user (condition), 
	// principals that are auditor or reviewer
	//
	static private Map<Serializable, ComplianceWorkflowRoleData> rules_map = new ConcurrentHashMap<Serializable, ComplianceWorkflowRoleData>();
	static private Map<Serializable, UserWorkLoadData> map = new ConcurrentHashMap<Serializable, UserWorkLoadData>();
	
	private final static int CACHE_DURATION = 1000 * 60 * 2; // 2 min in milisecs
	
	private  String effective_date_uname; 	
	private  String windsor_domain;

	
	private Domain domain = null;

	public KbeeWorkflowLoadService() {
	}

	public KbeeWorkflowLoadService(Domain domain) {
		 this.domain = domain;
	}

	public String getEffectiveDateUniqueName() {
		if (this.effective_date_uname==null)
			effective_date_uname 	= getContentDao().findSystemParameterValueByKey("dashboard.effective.date.uniquename", "date");
		return this.effective_date_uname;
	}
	
	public String setEffectiveDateUniqueName(String uname) {
		return this.effective_date_uname=uname;
	}
	
	public String getWindsorDomainName() {
		if (windsor_domain==null)
			windsor_domain = getContentDao().findSystemParameterValueByKey("dashboard.windsor.name", "windsor");
		return this.windsor_domain;
	}
	
	public String setWindsorDomainName(String uname) {
		return this.windsor_domain=uname;
	}
	
	
	public synchronized void evict() {
		map.clear();
		rules_map.clear();
		effective_date_uname=null;
		windsor_domain=null;
	}
	
	@Override
	public ComplianceWorkflowRoleData getComplianceWorkflowRoleData(User user) {
		if ( (!rules_map.containsKey(user.getId()))  ||  
		 	 ((System.currentTimeMillis()-rules_map.get(user.getId()).timestamp)>CACHE_DURATION) ) {
			synchronized (this) {
				ComplianceWorkflowRoleData data = calculateRules(user);
				rules_map.put(user.getId(), data);
			}
		}
		return rules_map.get(user.getId());	
	}

	
	/**
	 * Scans all rules and 
	 * 
	 * @param user
	 * @return
	 */
    private ComplianceWorkflowRoleData calculateRules(User user) {


    	
    	RulesQuery2 query = new RulesQuery2();
    	ResultSet results = query.execute();
    	
		while (results.hasNext()) {
			SearchResult res = results.next();
		
			if (res.getObject() instanceof SecurityRule) {
				
				SecurityRule rule = (SecurityRule) res.getObject();
				
				logger.info(rule.toString());
				
				try {
					
					// identifico la Property
					// Si la Condition tiene Property
					//String condition = rule.getCondition();

					// agrego la info
					//
					
				} catch (Exception e) {
							logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				}
			}
		}
		return null;
   	}
    
    
	
	@Override
	public UserWorkLoadData getUserWorkLoad(User user) {
		if ( (!map.containsKey(user.getId()))  ||  
		 	 ((System.currentTimeMillis()-map.get(user.getId()).timestamp)>CACHE_DURATION) ) {
				synchronized (this) {
					UserWorkLoadData data = calculate(user);
					map.put(user.getId(), data);	
				}
		}
		return map.get(user.getId());	
	}

	
	/**
	 * THis is used from the Expanded Panel, for that reason there is no need for a cache.
	 * 
	 * @param user
	 * @return
	 */
    @Override					
	public Map<String, Integer> getTaskTypesWorkLoad(User user) {
    	synchronized (this) {
			Map<String, Integer> mp = new HashMap<String, Integer>();
			WorkspaceQuery query = new WorkspaceQuery(getQueryIndex(user), user);
			ResultSet results = query.execute();
			while (results.hasNext()) {
				SearchResult res = results.next();
				if (res.getObject() instanceof Content) {
					Content content = (Content) res.getObject();
					if (content.isEnabled()) {
							try {
								WorkflowService workflowService = content.getService(WorkflowService.class);
								String taskname;
								if (workflowService==null || workflowService.getTask()==null)
									taskname="None";
								else {
									if (workflowService.getActivity().getProcess().getProcedure().getCode()==null)
										taskname = "N/A. " + workflowService.getTask().getName();
									else
										taskname = workflowService.getActivity().getProcess().getProcedure().getCode().trim() + ". " + workflowService.getTask().getName();
								}
								if (!mp.containsKey(taskname))
									mp.put(taskname, Integer.valueOf(0));
								mp.put(taskname, mp.get(taskname)+1);
							} catch (Exception e) {
								logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
							}
						}
				}
			}
		return mp;
    	}
    	
	}
	
    

    /**
     * 
     * 
     * @param user
     * @return
     */
    private  UserWorkLoadData calculate(User user) {

		UserWorkLoadData data = new UserWorkLoadData();
		data.user=user;
		WorkspaceQuery query = new WorkspaceQuery(getQueryIndex(user), user);
		ResultSet results = query.execute();
		
		data.total=0;
		data.due_none=0;
		data.due_plus_one=0;
		data.due_plus_two=0;
		data.due_plus_three=0;
		data.due_plus_four=0;
		data.due_plus_five=0;
		data.due_plus_six=0;
		data.due_plus_n=0;
		data.today_due_date=0;
		
		data.effective[0]=Integer.valueOf(0);
		data.effective[1]=Integer.valueOf(0);
		data.effective[2]=Integer.valueOf(0);
		data.effective[3]=Integer.valueOf(0);
		data.effective[4]=Integer.valueOf(0);
		data.effective[5]=Integer.valueOf(0);
		data.effective[6]=Integer.valueOf(0);
		
		
		data.timestamp=System.currentTimeMillis();
		
		int curren_year 		= OffsetDateTime.now().getYear();
		int curren_year_minus_1 = OffsetDateTime.now().getYear()-1;
		int curren_year_minus_2 = OffsetDateTime.now().getYear()-2;
		int curren_year_minus_3 = OffsetDateTime.now().getYear()-3;
		int curren_plus_1 		= OffsetDateTime.now().getYear()+1;
		int curren_plus_2 		= OffsetDateTime.now().getYear()+2;
		int curren_plus_3 		= OffsetDateTime.now().getYear()+3;
		
		DateTimeService dateservice  = ServiceLocator.getService(DateTimeService.class);
		
		
		while (results.hasNext()) {
			
			SearchResult res = results.next();
			
			if (res.getObject() instanceof Content) {

				Content content = (Content) res.getObject();
					
				if (content.isEnabled()) {
						
						data.total++;
						
						OffsetDateTime due = getOffsetDateTime(content);
						OffsetDateTime td =  OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS);
						
						if (due!=null)
							due=due.truncatedTo(ChronoUnit.DAYS);
						
						if (due==null) {
							data.due_none++;	
						}
						else if (due.isBefore(td)) {
							data.past_due_date++;
						}
						else if (due.isEqual(td)) {
							data.today_due_date++;			
						}
						else if (due.isAfter(td.plusDays(6))) {
							data.due_plus_n++;			
						}
						else if (due.isAfter(td.plusDays(5))) {
							data.due_plus_six++;			
						}
						else if (due.isAfter(td.plusDays(4))) {
							data.due_plus_five++;			
						}
						else if (due.isAfter(td.plusDays(3))) {
							data.due_plus_four++;			
						}
						else if (due.isAfter(td.plusDays(2))) {
							data.due_plus_three++;			
						}
						else if (due.isAfter(td.plusDays(1))) {
							data.due_plus_two++;			
						}
						else if (due.isAfter(td)) {
							data.due_plus_one++;			
						}
						

						
						if (getDomain().getName()!=null && getDomain().getName().equals(this.getWindsorDomainName())) {			
							
								boolean found = false;
								
								for (Classification clasi: content.getClassification()) {
									if (clasi.getClassifier().getUniqueName()!=null && clasi.getClassifier().getUniqueName().toLowerCase().equals(this.getEffectiveDateUniqueName())) {
										found = true;
										OffsetDateTime date = clasi.getDateValue();
										if (date!=null) {
											int year = date.getYear();
											if (year<=curren_year_minus_3) 
												data.effective[0]++;
											else if (year==curren_year_minus_2)
												data.effective[1]++;
											else if (year==curren_year_minus_1)
												data.effective[2]++;
											else if (year==curren_year)
												data.effective[3]++;
											else if (year==curren_plus_1)
												data.effective[4]++;
											else if (year==curren_plus_2)
												data.effective[5]++;
											else if (year==curren_plus_3)
												data.effective[6]++;
										}
									}
								}
								
								if (!found) {
									for (AttributeTemplate att: content.getContentTemplate().getAttributes()) {
										if (att.getAttribute().getType()==AttributeType.DATE && att.getAttribute().getUniqueName()!=null && att.getAttribute().getUniqueName().toLowerCase().equals(this.getEffectiveDateUniqueName())) {
											found = true;
											List<String> ld=content.getAttributeValues(att.getAttribute());
											if (ld!=null && !ld.isEmpty()) {
												String lds=ld.get(0);
												try {
													
													OffsetDateTime date = dateservice.parseStrDate(lds);
													
													if (date!=null) {
														int year = date.getYear();
														if (year<=curren_year_minus_3) 
															data.effective[0]++;
														else if (year==curren_year_minus_2)
															data.effective[1]++;
														else if (year==curren_year_minus_1)
															data.effective[2]++;
														else if (year==curren_year)
															data.effective[3]++;
														else if (year==curren_plus_1)
															data.effective[4]++;
														else if (year==curren_plus_2)
															data.effective[5]++;
														else if (year==curren_plus_3)
															data.effective[6]++;
													}
												} catch (Exception e) {
													logger.error(e.getStackTrace());
												}
											}
											
										}
									}
								}
							
						} // windsor domain
						
				} // content enabled
				
			} // content
			
		} // while
		
		
		return data;
		
	}

    
    
    
	@Override
	public boolean listen(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			return true;
		return false;
	}

	@Override
	public void onEvent(Event event) {
			logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + event.getClass().getName());
			if (event instanceof EvictCacheServiceEvent)
				evict();
	}

	
	

   /** 
    * 
    * 
    */
	private OffsetDateTime getOffsetDateTime(Content content) {
		try {
			return content.getService(WorkflowService.class).getContext().getDueDate();
		} 
		catch (Exception e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			logger.error(e.getMessage());
			return  null;
		}
	}
	
	private Index getQueryIndex(User user) {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}

	private Domain getDomain() {
		return this.domain;
	}
	
	/**
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	**/
	// Spring 
	//
	private ContentDao contentDao;
	public ContentDao getContentDao()							 	{		return contentDao;} // return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	public void setContentDao(ContentDao dao) 						{		contentDao=dao;}

}
