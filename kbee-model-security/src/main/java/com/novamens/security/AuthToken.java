package com.novamens.security;

import java.time.OffsetDateTime;

public interface AuthToken {
	public String getTokenValue();
	public boolean isValid();
	public long getLifeTime();
	public int getDuration();
	public OffsetDateTime getTime();
}