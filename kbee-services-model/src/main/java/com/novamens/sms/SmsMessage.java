package com.novamens.sms;

import java.util.List;

public interface SmsMessage {
	public List<String> getTo();
	public String getMessage();
}
