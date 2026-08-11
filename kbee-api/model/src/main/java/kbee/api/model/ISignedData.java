package kbee.api.model;

import java.io.Serializable;
import java.time.OffsetDateTime;

public class ISignedData implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private OffsetDateTime time;
	private ApiProxy user;
	private String data;
	private String signedData;
	
	public OffsetDateTime getTime() {
		return time;
	}
	
	public void setTime(OffsetDateTime time) {
		this.time = time;
	}
	
	public ApiProxy getUser() {
		return user;
	}
	
	public void setUser(ApiProxy user) {
		this.user = user;
	}

	public String getSignedData() {
		return signedData;
	}

	public void setSignedData(String signedData) {
		this.signedData = signedData;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	
}
