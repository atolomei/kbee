package com.novamens.kbee.content.command;

import java.util.List;

import com.novamens.indexer.query.SearchResult;

public class CommandListResultSet extends ListResultSet<CommandProxy> {

	public CommandListResultSet(List<CommandProxy> list) {
		super(list);
	}

	@Override
	public SearchResult next() {
		CommandProxy obj = (CommandProxy) getIterator().next();
		return new CommandListSearchResult(obj);
	}
}
