package kbee.api.model;

import java.time.OffsetDateTime;
import java.util.List;

public class INote extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private String text;
	private OffsetDateTime time;
	private ApiProxy author;
	private List<ApiResource> resources;
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public OffsetDateTime getTime() {
		return time;
	}
	
	public void setTime(OffsetDateTime time) {
		this.time = time;
	}
	
	public ApiProxy getAuthor() {
		return author;
	}
	
	public void setAuthor(ApiProxy author) {
		this.author = author;
	}

	public List<ApiResource> getResources() {
		return resources;
	}

	public void setResources(List<ApiResource> resources) {
		this.resources = resources;
	}

}
