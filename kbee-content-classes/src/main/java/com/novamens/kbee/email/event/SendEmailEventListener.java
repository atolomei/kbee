package com.novamens.kbee.email.event;

 
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.logging.SendEmailEvent;


/**
 * 
 * NO SE USA !!!!
 *
 */
public class SendEmailEventListener implements EventListener {

	public SendEmailEventListener() {
	}

	@Override
	public boolean listen(Event event) {
		return event instanceof SendEmailEvent;
	}

	@Override
	public void onEvent(Event event) {
			
		
		SendEmailEvent sev= (SendEmailEvent) event;
		
		
		
		
	}

}
