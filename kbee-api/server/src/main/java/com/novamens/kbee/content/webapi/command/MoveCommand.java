package com.novamens.kbee.content.webapi.command;

import com.novamens.kbee.content.command.mt.Callback;
import com.novamens.kbee.content.command.mt.QueueProcessorCommand;

import kbee.api.model.ApiFile;

// Parameters:
// source-url
// source-user
// source-password
// source-criteria
// source-domain
// max-threads
// batch-size
// target-url
// target-user
// target-password

public class MoveCommand extends QueueProcessorCommand<ApiFile> {
	
	public MoveCommand() {
		setName("Move Domain Command");
	}
	
	@Override
	public String getProcessorBean() {
		return getParameter("move-processor-bean")!=null ? (String)getParameter("move-processor-bean") : "ApiMoveProcessor";
	}
	
	@Override
	protected Callback<ApiFile> newCallback() {
		return new Callback<ApiFile>() {
			public void execute(ApiFile file) throws Exception {
				synchronized (MoveCommand.this) {
					MoveCommand.this.notify();
					getQueue().remove(file);
					incCounter();
				}	
			}
		};	
	}
}