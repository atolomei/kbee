package com.novamens.kbee.scheduler;

public enum SchedulerStatus {
	STARTING,
	RUNNING,     //  isRunning()
	RESETTING,   //  isRunning(), isResetting()   
	STOPPED,
	ERROR
	
}
