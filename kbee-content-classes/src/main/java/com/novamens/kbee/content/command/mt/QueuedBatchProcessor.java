package com.novamens.kbee.content.command.mt;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * 
   @see BatchReindexExecutor
  @see QueuedBatchProcessor
  @see BatchReindexCommand
  @see QueueProcessorCommand
  

 *
 */
public abstract class QueuedBatchProcessor implements Runnable {
	
	private static AtomicInteger Instances = new AtomicInteger();
	
	static protected Logger logger = LogManager.getLogger(QueuedBatchProcessor.class.getName());
	
	public static int Instances() {
		return Instances.get();
	}
	
	public static void resetInstances() {
		Instances.set(0);
	}
	
	public static void IncrementInstances() {
		Instances.incrementAndGet();
	}
	
	public static void DecrementInstances() {
		Instances.set(Instances.decrementAndGet());
	}	
}