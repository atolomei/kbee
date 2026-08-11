package com.novamens.kbee.content.command;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.novamens.content.command.Command;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.command.CommandService;
import com.novamens.service.ServiceLocator;

public class CommandListSearchResult implements SearchResult {

	private static final long serialVersionUID = 1L;

	private Command object;	
	
	private Serializable command_id;
	
	private boolean detached = false;
	
	public CommandListSearchResult(Command command) {
		this.object=command;
		this.command_id=command.getId();
	}
	
	@Override
	public void detach() {
		this.object=null;
		detached=true;
	}

	@Override
	public Object getObject() {
		if (detached) {
			object= getCommandService().getCommand((Long) this.command_id);
			detached=false;
		}
		return this.object;
	}

	@Override
	public String getText() {
		return null;
	}

	@Override
	public Map<String, Object> getParameters() {
		return null;
	}

	@Override
	public float getScore() {
		return 0;
	}

	@Override
	public List<String> getSnippets() {
		return null;
	}
	
	private CommandService getCommandService() {
			return (CommandService) ServiceLocator.getService(CommandService.class);
	}

}
