package kbee.api.model;

import java.io.Serializable;
public class ApiToken implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String value;
	private String refreshToken;
	private int duration;
	private long lifeTime;
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String value) {
		this.refreshToken = value;
	}
	public long getLifeTime() {
		return lifeTime;
	}
	public void setLifeTime(long leftTime) {
		this.lifeTime = leftTime;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
}