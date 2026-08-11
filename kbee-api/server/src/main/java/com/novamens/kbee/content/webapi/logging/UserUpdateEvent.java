package com.novamens.kbee.content.webapi.logging;

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;

import com.novamens.kbee.content.webapi.type.UriHelper;

import kbee.api.model.IError;
import kbee.api.model.ITransaction;
import kbee.api.model.ApiUser;

public class UserUpdateEvent extends UserEvent {
	
	private long start;

	public UserUpdateEvent(String uri) {
		setTime(OffsetDateTime.now());
		setUser();
		setUri(uri);
		setMethod("POST");
		//setDomain(user.getDomain());
		//setRequest(toJson(user));
		setContentClass("user");
		start = System.currentTimeMillis();
	}
	
	public UserUpdateEvent(ApiUser user) {
		setTime(OffsetDateTime.now());
		setUser();
		setUri(UriHelper.getUri(user));
		setMethod("POST");
		setDomain(user.getDomain());
		setRequest(toJson(user));
		setContentClass("user");
		start = System.currentTimeMillis();
	}
	
	public void setResponse(ITransaction transaction) {
		long now = System.currentTimeMillis();
		setProcessingTime(now-start);
		setTransaction(transaction.getId());
		setStatus(HttpStatus.OK);
		super.setResponse(toJson(transaction));
	}
	
	public void setResponse(IError error) {
		long now = System.currentTimeMillis();
		setProcessingTime(now-start);
		super.setResponse(toJson(error));
	}
}
