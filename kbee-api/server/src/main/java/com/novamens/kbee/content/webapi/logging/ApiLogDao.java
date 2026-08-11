package com.novamens.kbee.content.webapi.logging;

import java.util.List;

public interface ApiLogDao {
	public void append(ApiLogEvent event);
	public List<ApiLogEvent> getEvents(String[] statuses, int limit);
	public List<ApiLogEvent> getEvents(String statement);
	public long setClose(String statement, boolean value);
	public Long getNewId();
	public void update(ApiLogEvent event); 
}
