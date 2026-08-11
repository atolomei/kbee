package com.novamens.kbee.content.command;

import java.time.OffsetDateTime;
import java.util.Map;

import com.novamens.content.command.CommandState;
import com.novamens.kbee.command.CommandService;
import com.novamens.service.ServiceLocator;

/**
 *<p>Commands that execute on their own Thread. Unlike {@link ServiceRequest}, Command need not be Serializable.</p>
 *
 *
 */
public abstract class AsyncCommand extends AbstractCommand implements Runnable  {

	
	public AsyncCommand(Map<String, Object> map) {
		super(map);
		setName(this.getClass().getSimpleName() + " (async)");
	}
	
	public AsyncCommand() {
		setName(this.getClass().getSimpleName() + " (async)");
	}

	@Override
	public void execute() {
		ServiceLocator.getService(CommandService.class).run(this);
	}
	
	
	protected void initCommand() {
		super.initCommand();
	}
	
	
	public void syncExecute() {
		
		try {
		
			setDateStarted(OffsetDateTime.now());
			setState(CommandState.RUNNING);
			executeAsync();
		
		} finally  {
			
			if (getState()==CommandState.RUNNING)
				setState(CommandState.COMPLETED);
			setDateTerminated(OffsetDateTime.now());
		}
		
	}
	
	@Override
	public void run() {
		try {
			setDateStarted(OffsetDateTime.now());
			setState(CommandState.RUNNING);
			executeAsync();
		}  finally {
			if (getState()==CommandState.RUNNING)
				setState(CommandState.COMPLETED);
			setDateTerminated(OffsetDateTime.now());
		}
	}
	
	@Override
	public synchronized void stop() {
		super.stop();
	}
	
	public boolean isRunning() {
		return getState().equals(CommandState.RUNNING);
	}


	
	
	protected abstract void executeAsync();

}



