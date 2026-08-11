package kbee.api.model;

import java.time.OffsetDateTime;

public class IActivityProxy extends ApiProxy {
	private static final long serialVersionUID = 1L;
	
	private String subline;
	private String time;
	private OffsetDateTime timevalue;
	private String task;
	private INote note;
	private ApiProxy user;
	private boolean unread;
		

	public String getSubline() {
		return subline;
	}

	public void setSubline(String subline) {
		this.subline = subline;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
	
	public OffsetDateTime getTimeValue() {
		return timevalue;
	}

	public void setTime(OffsetDateTime time) {
		this.timevalue = time;
	}

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public INote getNote() {
		return note;
	}

	public void setNote(INote note) {
		this.note = note;
	}

	public ApiProxy getUser() {
		return user;
	}

	public void setUser(ApiProxy user) {
		this.user = user;
	}

	public boolean isUnread() {
		return unread;
	}

	public void setUnread(boolean unread) {
		this.unread = unread;
	}
}