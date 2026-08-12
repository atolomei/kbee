package com.novamens.indexer.iql;

import com.novamens.indexer.query.Query;

public interface IqlQuery extends Query {
	public String getStatement();
}
