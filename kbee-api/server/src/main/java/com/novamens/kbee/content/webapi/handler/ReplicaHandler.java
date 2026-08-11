package com.novamens.kbee.content.webapi.handler;

import org.hibernate.SessionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.kbee.content.dao.Proxy;
import com.novamens.kbee.content.webapi.type.UriHelper;
import com.novamens.kbee.domain.KbeeReplica;
import com.novamens.security.Identifiable;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiObject;
import kbee.api.model.ApiProxy;
import kbee.api.model.ITransaction;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;
import kbee.replica.Replica;
import kbee.replica.ReplicaService;

public class ReplicaHandler extends AbstractRequestHandler {

	@Transactional
	public ITransaction replicate(ApiObject object, String replicaId) {
		try {
	        Replica replica= getReplica(Long.valueOf(replicaId));
	        if (replica==null) {
	            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.REPLICA_NOT_FOUND);
	        }
	        
	       Object local = ServiceLocator.getService(ReplicaService.class).replicate(replica, object);
	        
			ITransaction transaction  = getTransaction(getProxy(local));
			
			return transaction;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}
	
	public Replica getReplica(long replicaId) {
		Replica replica = getSessionFactory().getCurrentSession().load(KbeeReplica.class, replicaId);
		replica = (Replica)Proxy.Unproxy(replica);
		return replica;
	}
	
	private SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}
	
	private ApiProxy getProxy(Object object) {
		ApiProxy proxy = null;
		if (object instanceof Identifiable)
		proxy = new ApiProxy(((Identifiable)object).getDisplayName(), UriHelper.getUri(object));
		return proxy;
	}
}