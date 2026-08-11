package com.novamens.content.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.base.Content;

@JsonTypeName("relation added")
public class RelationAdded extends AbstractUpdatedField {
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("id")
	private Long id;
	@JsonProperty("title")
	private String title;
	
	public RelationAdded() {
	}
	
	public RelationAdded(EForm form, String field, Content content) {
		setForm(form);
		setField(field);
		setContent(content);
	}
	
	public void setContent(Content content) {
		this.id = (Long)content.getId();
		this.title = content.getTitle();
	}
	
	@JsonProperty("id")
	public Long getContentId() {
		return id;
	}
	
	@JsonProperty("title")
	public String getTitle() {
		return title;
	}
	
	@Override
	@JsonIgnore
	public String getAction() {
		return "Added in " + getField();
	}
	
	@Override
	@JsonIgnore
	public String getLabel() {
		return getTitle();
	}
	
	public String getType() {
		return "relation added";
	}
}