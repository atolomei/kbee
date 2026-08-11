package com.novamens.kbee.content.rule;

import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.rule.Action;

public class KbeeMultipleAction extends KbeeAbstractAction {
	private static final long serialVersionUID = -1;
	
	private List<Action> actions;
	
	public KbeeMultipleAction(List<Action> actions) {
		this.actions = actions;
	}
	
	public Object execute(Content content) {
		return content;
	}
	
	public List<Action> getActions() {
		return actions;
	}
}
