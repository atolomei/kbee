package kbee.web.uploader;


import java.time.Instant;

import com.novamens.content.resource.KBFile;
import com.novamens.event.Event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileUploadedEvent implements Event {
	
	private String destination;
	private Instant time;
	private String name;
	private KBFile file;
	
	public Object getObject() {
		return file;
	}
}
