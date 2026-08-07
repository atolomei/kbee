package com.novamens.kbee.sysmessage;

import java.io.Serializable;
import java.util.List;

public interface SysMessageEntry extends com.novamens.security.Identifiable, Serializable {

	public SysMessage getSysMessage();

	public void setStartTime(long start_time);
	public long getStartTime();

	public boolean isValid();

	public List<String> getMetadataAsList();

	long getDueTime();
	
	
}
