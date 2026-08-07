package com.novamens.timer;

import com.novamens.service.SystemService;

public interface TimerService extends SystemService {
	public void setTimer(Timer timer);
	public void checkTimers();
}
