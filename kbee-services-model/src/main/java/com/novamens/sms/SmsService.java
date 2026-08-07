package com.novamens.sms;


import com.novamens.service.SystemService;

public interface SmsService extends SystemService {
	public void sendMessage(SmsMessage message);
}