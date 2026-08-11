package kbee.api.model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class IFieldValue implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String id;
	private String parent;
	private String displayName;
	private String subline;
	private String thumbnailUri;
	private String uri;
	private String contentType;
	private String type;
	private OffsetDateTime date;
	private long size;
	private List<ITextPart> parts;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String getSubline() {
		return subline;
	}
	
	public void setSubline(String value) {
		this.subline = value;
	}
	
	public String getUri() {
		return uri;
	}
	
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public String getThumbnailUri() {
		return thumbnailUri;
	}

	public void setThumbnailUri(String thumbnailUri) {
		this.thumbnailUri = thumbnailUri;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentTye) {
		this.contentType = contentTye;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public OffsetDateTime getDate() {
		return date;
	}

	public void setDate(OffsetDateTime date) {
		this.date = date;
	}

	public List<ITextPart> getParts() {
		return parts;
	}

	public void setParts(List<ITextPart> parts) {
		this.parts = parts;
	}
}