package com.novamens.kbee.sms;

import java.util.ArrayList;
import java.util.List;

import com.novamens.sms.SmsMessage;

public class KbeeSmsMessage implements SmsMessage {
	
	private List<String> to;
	private String message;
	
	public KbeeSmsMessage(String to, String message) {
		List<String> tolist = new ArrayList<String>();
		tolist.add(to);
		setTo(tolist);
		setMessage(message);
	}
	
	public List<String> getTo() {
		return to;
	}
	
	public void setTo(List<String> to) {
		this.to = to;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
}
