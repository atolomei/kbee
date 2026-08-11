package com.novamens.kbee.content.webapi.transaction;

import com.novamens.service.SystemService;

import kbee.api.model.ApiProxy;
import kbee.api.model.ITransaction;

public interface ApiTransactionService extends SystemService {
	public ITransaction getTransaction(ApiProxy target);
}
