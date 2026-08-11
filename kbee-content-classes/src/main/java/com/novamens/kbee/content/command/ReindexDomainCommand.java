package com.novamens.kbee.content.command;


import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandParameter;
import com.novamens.content.command.CommandParameterType;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;


public class ReindexDomainCommand  extends AbstractCommand {
	
	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReindexDomainCommand.class.getName());
	
	private Serializable domain_id = null;
	private Domain domain = null;
	private int total, index;
	private boolean include_attachments = false;
	
	private Logger clog = LogManager.getLogger(ReindexDomainCommand.class.getName());
	private Double dtotal = Double.valueOf(0);
	
	private List<String> statements;
	private Map<String, Integer> total_map = new HashMap<String, Integer>();
	

	private Integer itotal;
	
	
	public ReindexDomainCommand() {
	}
	
	public ReindexDomainCommand(Map<String, Object> map) {
		if(map!=null && map.containsKey("domain")) 
			this.domain_id=(Serializable) map.get("domain");
	}
	
	public ReindexDomainCommand(Domain domain) {
		this.domain = domain;
	}

	public void setIncludeAttachments(boolean ia) {
		this.include_attachments=ia;
	}
	
	public boolean isIncludeAttachments() {
		return this.include_attachments;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(getClass().getSimpleName());
		if (getDomain()!=null) 
			str.append(getDomain().getName());
		return str.toString();
	}
	
	
	
	public long getTotalItemsProcessed() {
		return index;
	}
	
	
	@Override
	public void execute() {

		setDateStarted(OffsetDateTime.now());

		
		
		if (getDomain()==null) {
			error("No Domain defined. Ending the process.");
			setResult("No Domain");
			setState(CommandState.COMPLETED);
			setDateTerminated(OffsetDateTime.now());
			return;
		}
		
 		try {
			this.total = getTotalObjects(getStatements());
			this.index = 0;

			if (this.total<1)
				this.total=1;

			setTotalObjects(this.total);
			
			clog.info("total objects to index "+ String.valueOf(total));
			info("total objects to index "+ String.valueOf(total));
			
			for (String stm: getStatements()) {
				try {
					
					if (!isStopped()) {	
						
						clog.info("Starting "+ stm);
						info("Starting "+ stm);
						
						ReindexCommand reindexcommand = new ReindexCommand(stm, getDomain()) {
								public void onIndex(Object object) {
								ReindexDomainCommand.this.onIndex(object);
							}
						};
						
						reindexcommand.setIncludeAttachments(isIncludeAttachments());
						//reindexcommand.setLogger(getLogger());
						reindexcommand.execute();
					}
				} 
				catch (Exception e) {
					
					clog.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
					error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				}
			}
		} 
		finally {
			
			setDateTerminated(OffsetDateTime.now());
			
			if (!isStopped()) {
				setProgress(100.00);
				setResult("Ok");
				setState(CommandState.COMPLETED);
			}
			else {
				setResult("Cancelled by user.");
				setState(CommandState.CANCELED);
			}
		}
	}

	public void onIndex(Object object) {
		ReindexDomainCommand.this.setProgress(Double.valueOf(index++) * 100.00 / getTotalObjects());
	}
	
	
	public Domain getDomain() {
		if (domain==null) {
			domain_id=250L;
			if (domain_id==null)
				return null;
			domain=getContentDao().findDomainById(domain_id);
		}
		return domain;
	}

	
	protected List<String> getStatements() {
		 
		if (statements !=null)
			 return statements;
		 
		 statements =new ArrayList<String>();
		 
			// Domain
			//
			statements.add("from KbeeDomain"); 
			
			// Security
			//
			statements.add("from KbeeGroup where domain.id=" 		+ getDomain().getId().toString()); // 1
			statements.add("from KbeeUser where domain.id=" 		+ getDomain().getId().toString());

			statements.add("from KbeeSecurityRule where domain.id=" + getDomain().getId().toString()); // 3
			statements.add("from KbeeENotiRule where domain.id=" 	+ getDomain().getId().toString());
			statements.add("from KbeeEntityRole where domain.id=" + getDomain().getId().toString());
			statements.add("from KbeeDomainRole where domain.id=" + getDomain().getId().toString());
			
			
			// Templates
			//
			statements.add("from KbeeEmailTemplate where domain.id=" + getDomain().getId().toString());  // 5
			
			// model 
			//
			statements.add("from KbeeDataSet where domain.id=" 			+ getDomain().getId().toString());
			statements.add("from KbeeClassifier where domain.id=" 		+ getDomain().getId().toString()); 
			statements.add("from KbeeAttribute where domain.id=" 		+ getDomain().getId().toString());
			statements.add("from KbeeContentTemplate where domain.id=" 	+ getDomain().getId().toString());
		
			// members
			//
			statements.add("from KbeeDataSetMember where domain.id=" 	+ getDomain().getId().toString()); // 10
			statements.add("from KbeePerson where domain.id=" 			+ getDomain().getId().toString()); // 10
			statements.add("from KbeeUserLabel"); 


			// Library
			//
			statements.add("from KbeeLibrary where domain.id=" + getDomain().getId().toString());
			statements.add("from KbeeNotification where domain.id=" + getDomain().getId().toString());



			// Content
			//
			statements.add("from KbeeBillboard where domain.id="+ getDomain().getId().toString()); // 12;


			statements.add("from KbeeIDoc where domain.id=" + getDomain().getId().toString());
			statements.add("from KbeeIDoc where domain.id=" + getDomain().getId().toString());
			statements.add("from KbeeOrganizationalText where domain.id=" + getDomain().getId().toString());
			statements.add("from KbeeOrganizationalText where domain.id=" + getDomain().getId().toString());

			statements.add("from KbeeUserListItem where domain.id=" + getDomain().getId().toString());


			// Content
			//
			statements.add("from KBFileImpl where domain.id=" + getDomain().getId().toString());

			// statements.add("from KbeeIDoc where domain.id=" + getDomain().getId().toString() +" and year(lastmodifiedDate)>"+String.valueOf(OffsetDateTime.now().getYear()-2));
			// statements.add("from KbeeIDoc where domain.id=" + getDomain().getId().toString() +" and year(lastmodifiedDate)<="+String.valueOf(OffsetDateTime.now().getYear()-2));
			// statements.add("from KbeeOrganizationalText where domain.id=" + getDomain().getId().toString() +" and year(lastmodifiedDate)>"+String.valueOf(OffsetDateTime.now().getYear()-2));
			// statements.add("from KbeeOrganizationalText where domain.id=" + getDomain().getId().toString() +" and year(lastmodifiedDate)<="+String.valueOf(OffsetDateTime.now().getYear()-2));
								
			if (isPortal()) {					
				statements.add("from KbeeSite where domain.id=" 				+ getDomain().getId().toString());
				//statements.add("from KbeePage where domain.id=" 				+ getDomain().getId().toString());
				//statements.add("from KbeeArea where domain.id=" 				+ getDomain().getId().toString());
				//statements.add("from KbeeBlock where domain.id=" 				+ getDomain().getId().toString());
				//statements.add("from KbeeViewBK where domain.id=" 				+ getDomain().getId().toString());
				//statements.add("from KbeeViewDetailContent where domain.id=" 	+ getDomain().getId().toString());
			}

			// Log
			//
			//statements.add("from ObjectEvent where domainId=" + getDomain().getId().toString());
			//statements.add("from SendEmailEvent where event_domain_id=" + getDomain().getId().toString()); // 15

			for (String s: statements) {
				info(s + " -> " + String.valueOf( getTotalStatements().get(s))+ " items");
				clog.info(s + "-> " +  (getTotalStatements().get(s)!=null?(String.valueOf(getTotalStatements().get(s).intValue())):" na")+ " items");
			}
			
		 return statements;
	}

	
	private void setTotalObjects(int total) {
		dtotal = Double.valueOf(total);
	}
	
	private Double getTotalObjects() {
		return dtotal;
	}
	
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	

	@Override
	public long getTotalItems() {
		return getTotalObjects(getStatements());
	}
	
	
	
	protected Map<String, Integer> getTotalStatements() {
		return total_map;
	}

	
	protected int getTotalObjects(List<String> statements) {

		if (itotal!=null)
			return itotal;
		
		int total = 0;
		for (String stm: statements) {
			try {
				info("Calculating total for "+ stm);
				ReindexCommand reindexcommand = new ReindexCommand(stm, getDomain());
				
				int tn = reindexcommand.getNumbersOfObjectsToIndex();
				total_map.put(stm, Integer.valueOf(tn));
				total += tn;
				
			} 
			catch (Exception e) {
				
				clog.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			}
		}
		itotal =  Integer.valueOf(total);
		return itotal.intValue();
	}
	
	private String getLoggerName() {
		
		String name = "logs/reindex-domain-"+getDomain().getName()+"-";
		DateFormat format = new SimpleDateFormat("MM-dd-yyyy");
		name += format.format(new Date());
		name += "-" + String.valueOf(getId()) + ".log";
		return name;
		
	}
	
	private boolean isPortal() {
		return false;
	}

	@Override
	public List<CommandParameter> getParametersDefinition() {
		List<CommandParameter> commandParameterList=new ArrayList<CommandParameter>();
		//commandParameterList.add(new CommandParameter("schemaid", "schema name", false, CommandParameterType.LONG));
		//commandParameterList.add(new CommandParameter("domain", "domain name", false, CommandParameterType.DATE));
		return commandParameterList;
	}
}
