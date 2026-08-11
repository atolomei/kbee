package com.novamens.kbee.content.webapi.transaction;

import com.novamens.beans.BeansService;
import com.novamens.kbee.content.webapi.logging.ApiLogDao;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiProxy;
import kbee.api.model.ITransaction;

public class KbeeApiTransactionService implements ApiTransactionService {
	
	public KbeeApiTransactionService() {
	}
	
	public ITransaction getTransaction(ApiProxy proxy) {
		ITransaction transaction = new ITransaction(getLogDao().getNewId(), proxy);
		return transaction;
	}
	
	protected ApiLogDao getLogDao() {
		return (ApiLogDao)ServiceLocator.getService(BeansService.class).getBean("apiLogDao");	
	}
}
