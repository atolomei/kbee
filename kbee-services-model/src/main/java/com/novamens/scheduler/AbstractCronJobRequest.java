package com.novamens.scheduler;


import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * <p>Service Request executed by the Scheduler regularly, <br /> 
 * using a {@link CronExpressionJ8} <br /><br />
 *CronSJobRequest are added to the system directly into the database <br />
 * </p>
 * 
 * 
 *
 */
public abstract class AbstractCronJobRequest extends AbstractServiceRequest {
		
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger("Scheduler");
	
	private static final long serialVersionUID = 1L;
	
	private ZonedDateTime xtime = null;
	private CronExpressionJ8 cron_expression;
	private boolean is_user_request = false;
	
	private String timezone;
	public boolean enabled = true;
	private boolean executeOldTriggers;

	private Serializable domain_id;
	
	

	static public boolean isCrobJobRequest() {
		return true;
	}
	
	
	public AbstractCronJobRequest() {
		super();
	}
	
	
	public void setDomainId(Serializable id) {
		this.domain_id=id;
	}
	
	public Serializable getDomainId() {
		return this.domain_id;
	}
	
	
	public boolean isEnabled() {
		return this.enabled;
	}
	
	public void setEnabled(boolean b) {
		this.enabled=b;
	}
	
	
	public ZonedDateTime getTime() {
		if (xtime==null) {
			if (getTimeZone()==null)
				xtime = getCronExpression().nextTimeAfter(ZonedDateTime.now());
			else
				xtime = getCronExpression().nextTimeAfter(ZonedDateTime.now(ZoneId.of(getTimeZone())));			
	}
		return xtime;
	}
	
	public void setTime(ZonedDateTime time) {
		this.xtime = time;
	}
	
	public ZonedDateTime getNextTime() {
		return getCronExpression().nextTimeAfter(getTime());
	}
	
	public CronExpressionJ8 getCronExpression() {
		return cron_expression;
	}
	
	public void setCronExpression(CronExpressionJ8 expression) {
		cron_expression = expression;
	}
	
	@Override
	public boolean isCronJob() {
		return true;
	}

	@Override
	public void execute() {
		
	}
	
	@Override
	public void stop() {
	}
	
	public void setUserRequest(boolean b) {
		 this.is_user_request = b;
	}
	
	public boolean isUserRequest() {
		return this.is_user_request;
	}
	
	
	public void onClone(AbstractCronJobRequest clone) {
		clone.setId((long)getId());
		clone.setCronExpression(getCronExpression());
		clone.setName(getName());
		clone.setDescription(getDescription());
		clone.setUserRequest(isUserRequest());
		clone.setTime(getCronExpression().nextTimeAfter(getTime()));
		clone.setExecuteOldTriggers(getExecuteOldTriggers());
		clone.setEnabled(isEnabled());
		clone.setCost(getCost());
		clone.setExecuteAfter(getExecuteAfter());
		clone.setDomainId(getDomainId());
		clone.setTimeZone(getTimeZone());
		Map<String, String> pa=getParameters();
		if (pa!=null) {
			for(Entry<String, String> e: pa.entrySet()) 
				pa.put(e.getKey().toString(), e.getValue());
			clone.setParameters(pa);
		}
	}
	public AbstractCronJobRequest clone() {
		try {
			@SuppressWarnings("deprecation")
			AbstractCronJobRequest clone = (AbstractCronJobRequest)this.getClass().newInstance();
			onClone(clone);
			return clone;
		}
		catch (InstantiationException | IllegalAccessException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * <p>toString is used to display info of the Object for the developers</p>
	 */

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(super.toString());
		if (cron_expression!=null)
			str.append("| CronExpression -> " + cron_expression.getExpression());
		if (getTimeZone()!=null) 
			str.append("| TZ -> " + getTimeZone());
		return str.toString();
	}


	public boolean getExecuteOldTriggers() {
		return executeOldTriggers;
	}

	public void setExecuteOldTriggers(boolean executeOldTriggers) {
		this.executeOldTriggers = executeOldTriggers;
	}

	public void setTimeZone(String timezone) {
		this.timezone=timezone;
	}
	
	public String getTimeZone() {
		return this.timezone;
	}
}
