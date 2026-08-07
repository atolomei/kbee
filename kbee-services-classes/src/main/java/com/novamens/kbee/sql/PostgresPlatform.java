package com.novamens.kbee.sql;

import java.time.OffsetDateTime;

public class PostgresPlatform implements SqlPlatform {
	
	public String nextSequenceQuery(String sequenceName) {
		return "select nextval('"+sequenceName+"')";
	}
	
	@Override
	public Object getTimestampValue(OffsetDateTime time) {
		return time;
	}
	
	@Override
	public Object getBooleanValue(Boolean value) {
		return value;
	}
	
	@Override
	public String getCurrentTimestamp() {
		return "now()";
	}
	
	@Override
	public String getTrueValue() {
		return "true"; 
	}
	
	@Override
	public String getFalseValue() {
		return "false"; 
	}
 
}
