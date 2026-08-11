package com.novamens.kbee.content.tool.impl;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.model.ContentTemplate;
import com.novamens.content.tools.Application;
import com.novamens.content.tools.Tool;

@Entity
@DiscriminatorValue(Tool.APPLICATION)
public class ApplicationImpl extends AbstractTool implements Application {

	private static final long serialVersionUID = 1873443275256741085L;

	public ApplicationImpl() {
		super();
	}
	
	public ApplicationImpl(ContentTemplate ct) {
		super(ct);
	}
}
