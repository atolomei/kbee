package kbee.api.model;

import java.io.Serializable;
import java.time.OffsetDateTime;

public class ApiProcess implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String id;
	private OffsetDateTime startTime;
	private OffsetDateTime endime;
	private ApiProxy procedure;
	private String state;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public OffsetDateTime getStartTime() {
		return startTime;
	}
	public void setStartTime(OffsetDateTime startTime) {
		this.startTime = startTime;
	}
	
	public OffsetDateTime getEndime() {
		return endime;
	}
	public void setEndime(OffsetDateTime endime) {
		this.endime = endime;
	}
	
	public ApiProxy getProcedure() {
		return procedure;
	}
	public void setProcedure(ApiProxy procedure) {
		this.procedure = procedure;
	}
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
}
