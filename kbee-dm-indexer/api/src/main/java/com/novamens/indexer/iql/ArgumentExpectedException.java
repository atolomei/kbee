package com.novamens.indexer.iql;

import com.novamens.indexer.service.IndexerException;

public class ArgumentExpectedException extends IndexerException {
	private static final long serialVersionUID = 1L;

	public ArgumentExpectedException(String classifierName)	{
		super("Argument expected for " + classifierName + ".");
	}
}
