package com.novamens.timer;

import java.time.OffsetDateTime;

public interface Timer {
	
	public OffsetDateTime getDueDate();
	public CallBack getCallBack();
	public int getAttemps();
	public void setError(Exception e);
}
