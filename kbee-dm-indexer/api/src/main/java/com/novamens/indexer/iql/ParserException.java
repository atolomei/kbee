package com.novamens.indexer.iql;

public class ParserException extends Exception{
	private static final long serialVersionUID = 1L;

	public ParserException(String message) {
		super( message );
	}
	
	public ParserException(Throwable exception)	{
		super(exception);
	}
	
	public ParserException(String message, Throwable exception)	{
		super(message, exception);
	}
}
