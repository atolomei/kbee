package com.novamens.kbee.content.webapi.command;

import com.novamens.beans.BeansService;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.kbee.content.webapi.controller.ApiDao;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;

public abstract class ApiCommand extends AsyncCommand{
	
	private String username;
	
	public ApiCommand(String commandname) {
		setName(commandname);
		setUserName(getUser().getName());
	}
	
	protected void setUserName(String name) {
		this.username = name;
	}
	
	protected String getUserName() {
		return username;
	}
	
	protected User getUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	protected void authenticate(String username) {
		ServiceLocator.getService(SecurityService.class).authenticate(username);
	}

	protected Transaction beginTransaction()  {
		return ServiceLocator.getService(TransactionService.class).beginTransaction(false);
	}
	
	protected ApiDao getApiDao() {
		return (ApiDao)ServiceLocator.getService(BeansService.class).getBean("apiDao");
	}
}
