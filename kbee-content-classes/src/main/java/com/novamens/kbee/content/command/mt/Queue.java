package com.novamens.kbee.content.command.mt;

import java.util.Map;

 
public interface Queue<T> {
	
	public T dequeue() throws QueueException;
	public void enqueue(T file) throws QueueException;
	public void remove(T file) throws QueueException;
	public void close();
	public long size() throws QueueException;
	public void setParameters(Map<String, Object> parameters);
}
