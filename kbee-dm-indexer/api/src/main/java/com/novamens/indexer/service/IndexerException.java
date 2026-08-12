package com.novamens.indexer.service;

public class IndexerException extends RuntimeException 
{
	private static final long serialVersionUID = 1L;

	public IndexerException(String message)	{
		super(message);
	}

	public IndexerException(Throwable exception) {
		super(exception );
	}
	
	public IndexerException(String message, Throwable exception) {
		super(message, exception);
	}
}
