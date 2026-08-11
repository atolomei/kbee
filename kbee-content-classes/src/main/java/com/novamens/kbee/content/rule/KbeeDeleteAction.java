package com.novamens.kbee.content.rule;

import java.io.Serializable;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.Content;
import com.novamens.content.rule.DeleteAction;

public class KbeeDeleteAction extends KbeeAbstractAction implements DeleteAction, Serializable {
	private static final long serialVersionUID = 1L;

	@Transactional(propagation = Propagation.REQUIRED)
	public Object execute(Content content) {
		getContentDao().delete(content);
		return content;
	}
}	