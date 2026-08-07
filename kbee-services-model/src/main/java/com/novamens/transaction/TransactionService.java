package com.novamens.transaction;

import com.novamens.service.SystemService;

public interface  TransactionService extends SystemService {
	public Transaction beginTransaction();
	public Transaction beginTransaction(boolean opensession);
	public <T> T execute(ReadOperation<T> operation);
}
