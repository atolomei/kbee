package com.novamens.kbee.content.command;

import java.time.OffsetDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.content.command.CommandState;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
				
public class ShutdownCommand extends AbstractCommand {
			
	private static Logger logger = LogManager.getLogger(ShutdownCommand.class.getName());
	
	public ShutdownCommand() {
		setName("Shutdown Command");
	}
		
	@Override
	public void execute() {
		logger.debug("Starting Command execution " + getName());
		setDateStarted(OffsetDateTime.now());
		setProgress(0);
		try {
					
			logger.info("Dulce et decorum est pro patria mori...");
			logger.info("goodbye...");

			ServiceLocator.getService(SecurityService.class).exit(0, 10000);
			
		} catch (Exception e) {
			setDateTerminated(OffsetDateTime.now());
			setState(CommandState.ERROR);
			setResult(e.getClass().getName() + " | " + e.getMessage());
		}
	}
	
	
}
