package com.novamens.kbee.content.webapi.type;

import java.time.OffsetDateTime;
import java.util.Date;

public class IDateAdapter implements Adapter<OffsetDateTime, Date> {
	
	
	public IDateAdapter() {
	}
	
	public Date adapt(OffsetDateTime time) {
		long epochMilli = time.toInstant().toEpochMilli();
		Date date = new Date(epochMilli);
		return date;
	}
}
