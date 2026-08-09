package kbee.web.resource;

import com.novamens.util.KbeeRuntimeException;

public class ResourceException extends KbeeRuntimeException {
	private static final long serialVersionUID = 1L;

	public ResourceException(Exception e) {
		super(e);
	}
}
