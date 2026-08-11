package com.novamens.kbee.content.workflow;

import com.novamens.event.AbstractEvent;
import com.novamens.workflow.Procedure;

public class ProcedureUpdateEvent extends AbstractEvent {

	public ProcedureUpdateEvent() {
		super();
	}
	
	public ProcedureUpdateEvent(Procedure procedure) {
		super(procedure);
	}

	@Override
	public boolean distributable() {
		return true;
	}
}
