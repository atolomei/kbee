package com.novamens.timer;

import java.time.OffsetDateTime;
import java.util.List;

import com.novamens.dao.Dao;

public interface TimerDao extends Dao {
	public void update(Timer timer);
	public void delete(Timer timer);
	public List<Timer> getTimersAt(OffsetDateTime time);
}
