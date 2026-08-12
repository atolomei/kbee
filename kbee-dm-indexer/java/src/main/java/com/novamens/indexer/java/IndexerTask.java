package com.novamens.indexer.java;

import com.novamens.scheduler.ServiceRequest;

public interface IndexerTask extends ServiceRequest {
	public void execute();
	public Object getObject();
	public boolean isTransactional();
	public boolean isSynchronous();
}
