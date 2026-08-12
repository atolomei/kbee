package com.novamens.indexer.iql;

import com.novamens.indexer.service.IndexerException;

public class IllegalArgumentException extends IndexerException {
	private static final long serialVersionUID = 1L;

	public IllegalArgumentException(String classifierName)	{
		super("Illegal Argument for " + classifierName + ".");
	}
}
