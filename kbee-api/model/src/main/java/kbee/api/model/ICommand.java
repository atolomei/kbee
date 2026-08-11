package kbee.api.model;

import java.util.Date;

public class ICommand extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private String description;
	//private String state;
	private double progress;
	private Date startTime;
	private Date endTime;
	private ApiProxy log;
	 
	public ICommand() {
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
//	
//	public String getState() {
//		return state;
//	}
//	
//	public void setState(String state) {
//		this.state = state;
//	}
	
	public Date getStartTime() {
		return startTime;
	}
	
	public void setStartTime(Date date) {
		this.startTime = date;
	}
	
	public Date getEndTime() {
		return endTime;
	}
	
	public void setEndTime(Date date) {
		this.endTime = date;
	}
	
	public double getProgress() {
		return progress;
	}
	
	public void setProgress(double progress) {
		this.progress = progress;
	}
	
	public ApiProxy getLog() {
		return log;
	}
	
	public void setLog(ApiProxy log) {
		this.log = log;
	}

}
