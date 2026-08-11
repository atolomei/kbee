package kbee.replica;

import com.novamens.dom.KbeeException;

public class ReplicaException extends KbeeException {
	private static final long serialVersionUID = 1L;

	public ReplicaException(String message) {
		super(message);
	}
	
	public ReplicaException(Exception e) {
		super(e);
	}
}