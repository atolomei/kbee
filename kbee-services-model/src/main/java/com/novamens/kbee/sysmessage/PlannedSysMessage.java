package com.novamens.kbee.sysmessage;

// import com.novamens.scheduler.CronExpression;
import com.novamens.scheduler.CronExpressionJ8;

public interface PlannedSysMessage extends SysMessage {

	public CronExpressionJ8 getCronExpression();
	public void setCronExpression(String ce);

	

	
	
}
