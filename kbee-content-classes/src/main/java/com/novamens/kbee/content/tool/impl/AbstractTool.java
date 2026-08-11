package com.novamens.kbee.content.tool.impl;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.novamens.content.model.ContentTemplate;
import com.novamens.content.tools.Tool;
import com.novamens.kbee.content.base.KbeeContent;

@Entity
@PrimaryKeyJoinColumn(name="content_id")
@Table(name = "Tool")
@DiscriminatorColumn(name = "tool_class", discriminatorType=DiscriminatorType.STRING)
@DiscriminatorValue(Tool.TOOL)
public class AbstractTool extends KbeeContent implements Tool {

	private static final long serialVersionUID = -7118509858238738861L;

	@Column(name = "url")
	private String url;
	

	@Column(name = "subtitle")
	private String subtitle;
	
	public AbstractTool() {
		super();
	}
	
	public AbstractTool(ContentTemplate ct) {
		super(ct);
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url=url;
	}
	
	public String getSubtitle() {
		return subtitle;
	}
	
	public void setSubtitle(String subtitle) {
		this.subtitle=subtitle;
	}

	
	
}
