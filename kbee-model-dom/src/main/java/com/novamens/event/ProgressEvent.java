package com.novamens.event;

public class ProgressEvent extends AbstractEvent {

    private long progress;
    private long expected;
    
    public ProgressEvent(Object object, long progress, long expected) {
    	super(object);
    	setProgress(progress);
    	setProgress(expected);
    }
    
	public long getProgress() {
		return progress;
	}
    
	public void setProgress(long progress) {
		this.progress = progress;
	}
	
	public long getExpected() {
		return expected;
	}
	
	public void setExpected(long expected) {
		this.expected = expected;
	}
}
