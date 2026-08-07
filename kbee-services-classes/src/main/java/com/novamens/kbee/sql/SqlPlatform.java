package com.novamens.kbee.sql;

import java.time.OffsetDateTime;

public interface SqlPlatform {
	public String nextSequenceQuery(String sequenceName);
	public String getTrueValue();
	public String getFalseValue();
	public Object getTimestampValue(OffsetDateTime time);
	public Object getBooleanValue(Boolean value);
	public String getCurrentTimestamp();
}
