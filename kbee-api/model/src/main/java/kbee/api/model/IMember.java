package kbee.api.model;

public class IMember extends ApiObject {
	private static final long serialVersionUID = 1L;

	private String path;
	private int count;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
}