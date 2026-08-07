package com.novamens.security;

import java.time.OffsetDateTime;

public interface TokenSubmission {
	public String getTokenValue();
	public String getFeedback();
	public String getEmail();
	public String getPhone();
	public boolean hasError();
	public OffsetDateTime getTime();
}