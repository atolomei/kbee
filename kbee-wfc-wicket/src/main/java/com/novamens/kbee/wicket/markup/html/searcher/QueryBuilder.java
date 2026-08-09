package com.novamens.kbee.wicket.markup.html.searcher;

import com.novamens.indexer.query.Query;

public interface QueryBuilder {
	public Query getNewQuery(String text);
}
