package com.novamens.kbee.security;

import java.time.OffsetDateTime;

import com.novamens.security.TokenSubmission;

public class KbeeTokenSubmission implements TokenSubmission {
	
	private String tokenValue, feedback, email, phone;
	private OffsetDateTime time;
	private boolean hasError;
	
	public KbeeTokenSubmission() {
		hasError = false;
		time = OffsetDateTime.now();
	}
	
	public String getTokenValue() {
		return tokenValue;
	}
	
	public void setTokenValue(String tokenValue) {
		this.tokenValue = tokenValue;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getFeedback() {
		return feedback;
	}
	
	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}
	
	public OffsetDateTime getTime() {
		return time;
	}
	
	public void setTime(OffsetDateTime time) {
		this.time = time;
	}
	
	public boolean hasError() {
		return hasError;
	}
	
	public void setError(boolean hasError) {
		this.hasError = hasError;
	}
}