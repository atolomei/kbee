package com.novamens.kbee.content.command.mt;

import com.novamens.dom.KbeeException;

public class QueueException extends KbeeException {

	private static final long serialVersionUID = 1L;
	
	public QueueException(String message) {
		super(message);
	}

	public QueueException(Exception cause) {
		super(cause);
	}
}