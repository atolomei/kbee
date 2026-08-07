package com.novamens.kbee.security;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

import com.novamens.security.AuthToken;

public class KbeeAuthToken implements AuthToken {

	private static Random Random = new Random();
	
	private String tokenValue;
	private OffsetDateTime time;
	private int duration;
	
	public KbeeAuthToken(int duration) {
		String value = String.valueOf(Random.nextInt(999999));
		value = "000000".substring(0, 6-value.length()) + value;
	    setTokenValue(value);
	    setDuration(duration);
	    setTime(OffsetDateTime.now());
	}
	
	public void setTokenValue(String tokenValue) {
		this.tokenValue = tokenValue;
	}
	
	public String getTokenValue() {
		return tokenValue;
	}
	
	public boolean isValid() {
		return !OffsetDateTime.now().isAfter(getTime().plusSeconds(getDuration()));
	}
	
	public long getLifeTime() {
		return isValid() ? ChronoUnit.SECONDS.between(OffsetDateTime.now(), getTime().plusSeconds(getDuration())) : 0;
	}
	
	public OffsetDateTime getTime() {
		return time;
	}
	
	public void setTime(OffsetDateTime time) {
		this.time = time;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}
}