package com.novamens.kbee.content.command;

import com.novamens.service.ServiceLocator;
import com.novamens.timer.TimerService;

public class KbeeCheckTimersCommand extends AsyncCommand {
	
	public KbeeCheckTimersCommand() {
		setName("Check Timers");
	}
	
	public void executeAsync() {
		ServiceLocator.getService(TimerService.class).checkTimers();
	}	
}

