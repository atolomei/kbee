package kbee.api.model;

public class IDevice extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private String number;
	
	public IDevice() {
	}
	
	public IDevice(String id) {
		setId(id);
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

}