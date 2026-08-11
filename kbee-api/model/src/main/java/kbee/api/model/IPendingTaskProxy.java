package kbee.api.model;

public class IPendingTaskProxy extends ApiProxy {
	private static final long serialVersionUID = 1L;
	
	private String time;
	private String task;

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}
	
}