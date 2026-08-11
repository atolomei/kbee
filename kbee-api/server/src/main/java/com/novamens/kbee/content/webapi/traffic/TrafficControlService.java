package com.novamens.kbee.content.webapi.traffic;


import com.novamens.service.SystemService;

public interface TrafficControlService extends SystemService {

	final int DEFAULT_TOKENS = 8; 
			
	public TrafficPass getPass();
	public void release(TrafficPass pass);
}
