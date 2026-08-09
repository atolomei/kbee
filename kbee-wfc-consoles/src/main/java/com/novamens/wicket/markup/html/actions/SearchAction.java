package com.novamens.wicket.markup.html.actions;

import com.novamens.indexer.query.Query;
import com.novamens.wicket.markup.html.actions.Action;

public interface SearchAction extends Action {
	public Query getQuery();
}
