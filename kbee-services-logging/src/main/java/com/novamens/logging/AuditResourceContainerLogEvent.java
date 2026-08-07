package com.novamens.logging;

import com.novamens.content.resource.KBFile;
import com.novamens.event.LogEvent;

public interface AuditResourceContainerLogEvent extends LogEvent {

	public KBFile getFile();
	
}
