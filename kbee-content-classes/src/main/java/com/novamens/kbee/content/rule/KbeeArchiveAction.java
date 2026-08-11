package com.novamens.kbee.content.rule;

import java.io.Serializable;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.Content;
import com.novamens.content.rule.ArchiveAction;
import com.novamens.content.service.ContentService;
import com.novamens.dom.ObjectState;

public class KbeeArchiveAction extends KbeeAbstractAction implements ArchiveAction, Serializable {
	private static final long serialVersionUID = 1L;
	
	@Transactional(propagation = Propagation.REQUIRED)
	public Object execute(Content content) {
		if (!content.getState().equals(ObjectState.ARCHIVED))
		content.getService(ContentService.class).archive();
		return content;
	}
}