package com.novamens.kbee.content.webapi.command;

import com.novamens.scheduler.AbstractCronJobRequest;

import kbee.util.PropertiesFactory;

/**
 * 
 * The idea is to reprocess API Requests:
 * select * from api_logevent  where event_status=412 or  event_status=403 or event_status=500 
 *
 */
public class ReprocessAPIRequestsCronJobRequest extends AbstractCronJobRequest {
			
	private static final long serialVersionUID = 1L;

	int limit = 0;
									
	private static final boolean IS_API_ENABLED =  PropertiesFactory.getInstance("kbee").getProperties().getProperty("api.enabled", "yes").toLowerCase().trim().equals("yes");
	
	public ReprocessAPIRequestsCronJobRequest() {
		setName("Reprocess failed API Log Requests");
		setDescription("");
	}

	/**
	 * 
	 *  The command is executed Asynchronously (in a separate thread)
	 *  and therefore it has to create its own DB Trx 
	 *  (it is not included inside the Scheduler Trx)
	 *  
	 */
	@Override
	public void execute() {
		
		if (!IS_API_ENABLED)  
			return;
		
		if (getParameters()!=null) {
			if (getParameters().get("param")!=null) {
				String p= (String) getParameters().get("param");
				Integer to = Integer.valueOf(0);
				try {
					to=Integer.parseInt(p);
					setLimit(to.intValue()); 
				} catch (Exception e) {
					
				}
				
				
			}
		}
		ReprocessCommand command = new ReprocessCommand(); 
		command.setLimit(getLimit());
		command.execute();		
	}
	
	public int getLimit() {
		return limit;
	}
	
	public void setLimit(int size) {
		this.limit=size;
	}
}
