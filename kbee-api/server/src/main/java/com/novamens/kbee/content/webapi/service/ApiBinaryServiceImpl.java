package com.novamens.kbee.content.webapi.service;

import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.kbee.content.webapi.handler.FileUpdateAbstractHandler;
import com.novamens.kbee.content.webapi.handler.RequestHandler;
import com.novamens.kbee.content.webapi.logging.FileUpdateEvent;
import com.novamens.kbee.content.webapi.traffic.TrafficControlService;
import com.novamens.kbee.content.webapi.traffic.TrafficPass;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiFile;
import kbee.api.model.IError;
import kbee.api.model.ITransaction;
import kbee.api.service.ApiBinaryService;
import kbee.api.service.ApiException;

public class ApiBinaryServiceImpl implements ApiBinaryService {
	
	static private Logger logger = LogManager.getLogger("ApiLogger");
	
	@Override
	public ITransaction update (ApiFile file) {
		TrafficPass pass = ServiceLocator.getService(TrafficControlService.class).getPass();
		
		FileUpdateEvent logevent = new FileUpdateEvent(file);
		
		try {
			ITransaction transaction = ((FileUpdateAbstractHandler)getHandler("binary-file-update")).update(file);
			logevent.setResponse(transaction);
			logger.info(logevent);
			return transaction;
		}
		catch (ApiException e) {
			logevent.setResponse(new IError(e.getErrorCode(), e.getMessage()));
			logevent.setStatus( e.getHttpStatus());
			logger.info(logevent);
			throw e;
		}
		finally {
			ServiceLocator.getService(TrafficControlService.class).release(pass);
		}
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Override
	public ITransaction update1 (ApiFile file, InputStream resource) {
		TrafficPass pass = ServiceLocator.getService(TrafficControlService.class).getPass();
		
		FileUpdateEvent logevent = new FileUpdateEvent(file);
		
		try {
			ITransaction transaction = ((FileUpdateAbstractHandler)getHandler("binary-file-update")).update(file, resource);
			logevent.setResponse(transaction);
			logger.info(logevent);
			return transaction;
		}
		catch (ApiException e) {
			logevent.setResponse(new IError(e.getErrorCode(), e.getMessage()));
			logevent.setStatus( e.getHttpStatus());
			logger.info(logevent);
			throw e;
		}
		finally {
			ServiceLocator.getService(TrafficControlService.class).release(pass);
		}
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Override
	public ITransaction zipupdate (ApiFile file, InputStream resource) {
		TrafficPass pass = ServiceLocator.getService(TrafficControlService.class).getPass();
		
		FileUpdateEvent logevent = new FileUpdateEvent(file);
		
		try {
			ITransaction transaction = ((FileUpdateAbstractHandler)getHandler("binary-file-update")).zipupdate(file, resource);
			logevent.setResponse(transaction);
			logger.info(logevent);
			return transaction;
		}
		catch (ApiException e) {
			logevent.setResponse(new IError(e.getErrorCode(), e.getMessage()));
			logevent.setStatus( e.getHttpStatus());
			logger.info(logevent);
			throw e;
		}
		finally {
			ServiceLocator.getService(TrafficControlService.class).release(pass);
		}
	}
	
//	/** ------------------------------------------------------------------------------------------------------------------------
//	 */
//	@Override
//	public ITransaction addResource (IFile file, IBinaryResource resource, InputStream stream) {
//		TrafficPass pass = ServiceLocator.getService(TrafficControlService.class).getPass();
//		
//		FileUpdateEvent logevent = new FileUpdateEvent(file);
//		
//		try {
//			ITransaction transaction = ((FileUpdateHandler)getHandler("binary-file-update")).addResource(file, resource, stream);
//			logevent.setResponse(transaction);
//			logger.info(logevent);
//			return transaction;
//		}
//		catch (ApiException e) {
//			logevent.setResponse(new IError(e.getErrorCode(), e.getMessage()));
//			logevent.setStatus( e.getHttpStatus());
//			logger.info(logevent);
//			throw e;
//		}
//		finally {
//			ServiceLocator.getService(TrafficControlService.class).release(pass);
//		}
//	}
//	
//	/** ------------------------------------------------------------------------------------------------------------------------
//	 */
//	@Override
//	public ITransaction publish (IFile file) {
//		TrafficPass pass = ServiceLocator.getService(TrafficControlService.class).getPass();
//		
//		FileUpdateEvent logevent = new FileUpdateEvent(file);
//		
//		try {
//			ITransaction transaction = ((FileUpdateHandler)getHandler("binary-file-update")).publish(file);
//			logevent.setResponse(transaction);
//			logger.info(logevent);
//			return transaction;
//		}
//		catch (ApiException e) {
//			logevent.setResponse(new IError(e.getErrorCode(), e.getMessage()));
//			logevent.setStatus( e.getHttpStatus());
//			logger.info(logevent);
//			throw e;
//		}
//		finally {
//			ServiceLocator.getService(TrafficControlService.class).release(pass);
//		}
//	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	private RequestHandler getHandler(String handlername) {
		return (RequestHandler)ServiceLocator.getService(BeansService.class).getBean(handlername+"-handler");
	}
}
