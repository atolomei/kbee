package com.novamens.kbee.timer;

import java.time.OffsetDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.timer.Timer;
import com.novamens.timer.TimerDao;
import com.novamens.timer.TimerService;

public class KbeeTimerService implements TimerService {
	private TimerDao timerDao;
	
	static Logger logger = LogManager.getLogger(KbeeTimerService.class.getName());
	
	@Transactional
	public void setTimer(Timer timer) {
		getTimerDao().update(timer);
	}
	
	@Transactional
	public void checkTimers() {
		try {
			for (Timer timer : getTimerDao().getTimersAt(OffsetDateTime.now())) {
				try {
					if (timer.getAttemps()<3) {
						timer.getCallBack().execute();
						getTimerDao().delete(timer);
					}
				}
				catch (Exception e) {
					try {
						timer.setError(e);
						getTimerDao().update(timer);
					}
					catch (Exception e1) {
						logger.error("Error in timer "+((KbeeTimer)timer).getId()+": "+e1.getMessage(), e);
					}
				}
			}
		}
		catch (Exception e) {
			logger.error("Error in timers ", e);
		}
    	}
	
	public TimerDao getTimerDao() {
		return timerDao;
	}
	
	public void setTimerDao(TimerDao dao) {
		timerDao = dao;
	}
}