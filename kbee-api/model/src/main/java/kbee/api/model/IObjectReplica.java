package kbee.api.model;

import java.io.Serializable;

public class IObjectReplica implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String iclass;
	private String data;
	
	public String getIclass() {
		return iclass;
	}
	
	public void setIclass(String iclass) {
		this.iclass = iclass;
	}
	
	public String getData() {
		return data;
	}
	
	public void setData(String data) {
		this.data = data;
	}
}