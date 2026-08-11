package kbee.api.model;

import java.time.OffsetDateTime;

public class ILogEvent extends ApiObject {
	private static final long serialVersionUID = 1L;
	private ApiProxy user;
	private OffsetDateTime time;
	private String type;
	private int version;
	private String description;
	private String parameters;
	
	public ApiProxy getUser() {
		return user;
	}
	
	public void setUser(ApiProxy user) {
		this.user = user;
	}
	
	public OffsetDateTime getTime() {
		return time;
	}
	
	public void setTime(OffsetDateTime date) {
		this.time = date;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public int getVersion() {
		return version;
	}
	
	public void setVersion(int version) {
		this.version = version;
	}
	
	public String getParameters() {
		return parameters;
	}
	
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
