package com.novamens.scheduler;

public interface Dispatcher {
	
	public int getPriority();
	public void dispatch(Batch batch);
	public void execute(Runnable batch);
	public int getPoolSize();
	public int getMaximumPoolSize();
	public String getInfo();
	public void shutDownNow();
	public String getStatus();
	public void restart();
	public void restart(boolean force);
	
}
