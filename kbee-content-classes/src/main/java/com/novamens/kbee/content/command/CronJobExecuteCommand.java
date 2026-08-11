package com.novamens.kbee.content.command;

import java.time.OffsetDateTime;

import com.novamens.content.command.CommandState;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;

/**
 * <p>Executes a Cron Job Request by Id</p>
 */
public class CronJobExecuteCommand extends AbstractCommand {
																								
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(CronJobExecuteCommand.class.getName());
	
	public CronJobExecuteCommand() {

		setName("Executes a Cron Job Request");
		setDescription("Parameter id=CronJob Request id");
		
		setExactlyOneSemantics(true);
	}
	
	
	@Override
	public void execute() {
		
		try {
			setProgress(0);
			setDateStarted(OffsetDateTime.now());
			setResultComments(null);
			setResult(null);
			
		if (getParameters().containsKey("id")) {
			String id = (String) getParameters().get("id");
			ServiceLocator.getService(SchedulerService.class).processCronJobById(id);
			setState(CommandState.COMPLETED);
			setProgress(100.00);
			setResult("Done");
		}
		else {
			setState(CommandState.ERROR);
			logger.error("Parameter Id is null");
			setResult("Parameter Id is null");
			setResultComments("Please check in Scheduler page the id of the CronJob");
		}
		} finally  {
				setDateTerminated(OffsetDateTime.now());
		}
	}

}
