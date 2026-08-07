package com.novamens.kbee.sql;

import java.sql.Timestamp;
import java.time.OffsetDateTime;

public class OraclePlatform implements SqlPlatform {
	
	public String nextSequenceQuery(String sequenceName) {
		return "select " + sequenceName + ".nextval from dual";
	}
	
	@Override
	public Object getTimestampValue(OffsetDateTime time) {
		return Timestamp.from(time.toInstant());		
	}
	
	@Override
	public Object getBooleanValue(Boolean value) {
		return value ? 1 : 0;
	}
	
	@Override
	public String getCurrentTimestamp() {
		return "systimestamp";
	}

	@Override
	public String getTrueValue() {
		return "1";
	}
	
	@Override
	public String getFalseValue() {
		return "0";
	}
}
