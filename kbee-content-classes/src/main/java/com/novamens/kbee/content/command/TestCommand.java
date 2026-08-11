package com.novamens.kbee.content.command;

import java.time.OffsetDateTime;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.content.command.CommandState;

					
/**
 * 
 * 
 * Executes in a thread inside the Scheduler
 * Worker-...
 *
 */
public class TestCommand extends AbstractCommand  {

	private static Logger logger = LogManager.getLogger(TestCommand.class.getName());
	 
	private int steps = 100;
	private int duration_secs=1;
	
	
	
	public TestCommand(int duration_secs) {
		this("Test Command", duration_secs);
	}
	public TestCommand() {
		this("Test Command", 5);
	}
	public TestCommand(String name, int duration_secs) {
		setName(name);
		this.duration_secs=duration_secs;
		setDescription("Testing Scheduler CommandRequest and CommandService");
	}
	
	
	
	public void setDurationSeconds(int seconds) {
		duration_secs=seconds;
		
	}
	@Override
	public void execute() {

			logger.debug("Starting Command execution " + getName());
			
			setDateStarted(OffsetDateTime.now());
	
			setProgress(0);
			
			
			steps=duration_secs * 1000 / 100;
				
			for (int n=0; n<steps; n++) {

				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {
					logger.info("Stop received from the external world");
				}
				
				setProgress((int)(100*n/steps));
				
				if (this.isStopped()) {
					setResultComments("Terminado por el usuario.");
					break;
				}
			}
	
			
	
			if (!isStopped()) {
				setProgress(100);
				setResult("OK");
				setResultComments("Todo bien");
				setState(CommandState.COMPLETED);
			}
			else {
				setState(CommandState.CANCELED);
			}
	
			setDateTerminated(OffsetDateTime.now());
			logger.debug("Ending Command execution " + getName());
		
		
	}
	
	public String toString() {
		return this.getClass().getSimpleName() + " | " + getDescription();
	}
	

}
