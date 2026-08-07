package com.novamens.calendar;


import java.time.OffsetDateTime;

import com.novamens.service.BusinessObjectService;


public interface CalendarService extends BusinessObjectService {
	
	static public final String DEFAULT_NON_WORKABLE = "12/24; 12/25; 12/31; 1/1";
	static public final String DEFAULT_CUTOFF_TIME = "17";
								
	static public final String START_HOUR = "8";
	static public final String END_HOUR = "17";
	
	
	public OffsetDateTime getDueDate(int hours);
	public OffsetDateTime getDueDate(OffsetDateTime started, int hours);
	
	public void evict();
	
	// in business hours
	public double getBusinessHoursDuration(OffsetDateTime started, OffsetDateTime ended);
	
}


/**




start (laborable)
end (laborable) 

end es el mismo business day que start ?
yes -> [end - start] en segundos / 3600

no ->
[end-hour - start] en segundos / 3600 +
camino los dias hasta llegar al dia de end. +8 hs por dia laborable

dia end. (end - start hour) en segundos / 3600 



*/