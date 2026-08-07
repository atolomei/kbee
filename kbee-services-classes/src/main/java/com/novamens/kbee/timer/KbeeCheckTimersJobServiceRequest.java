package com.novamens.kbee.timer;

import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.service.ServiceLocator;
import com.novamens.timer.TimerService;
									
/**
 * 
 * 
<bean id="check-timers-job" class="com.novamens.kbee.timer.KbeeCheckTimersJobServiceRequest">
		<property name="cronExpression" value="0 45 4 * * *"/>
	</bean>
	 *
 */
public class KbeeCheckTimersJobServiceRequest extends AbstractCronJobRequest {
	private static final long serialVersionUID = 1L;
	
	public KbeeCheckTimersJobServiceRequest() {
		super();
		setName("Check Workflow Timers");
		setTimeZone("US/Central");
	}
	
	@Override
	public String getTimeZone() {
		return super.getTimeZone();
		// default_time_zone = getContentDao().findSystemParameterValueByKey("timezone.default", "US/Central");
	}
	
	public void execute() {
		ServiceLocator.getService(TimerService.class).checkTimers();
	}
}


