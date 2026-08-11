package com.novamens.kbee.content.webapi.command;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.kbee.content.document.KbeeIDoc;
import com.novamens.kbee.content.webapi.controller.ApiDao;
import com.novamens.kbee.content.webapi.handler.FileDeleteHandler;
import com.novamens.kbee.content.webapi.handler.FileUpdateAbstractHandler;
import com.novamens.kbee.content.webapi.handler.RequestHandler;
import com.novamens.kbee.content.webapi.handler.UserUpdateHandler;
import com.novamens.kbee.content.webapi.logging.ApiLogDao;
import com.novamens.kbee.content.webapi.logging.ApiLogEvent;
import com.novamens.kbee.content.webapi.logging.FileDeleteEvent;
import com.novamens.kbee.content.webapi.logging.FileUpdateEvent;
import com.novamens.kbee.content.webapi.logging.UserUpdateEvent;
import com.novamens.kbee.content.webapi.type.IDocAdapter;
import com.novamens.kbee.content.webapi.type.gson.OffsetDateTimeAdapter;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;

import kbee.api.model.ApiFile;
import kbee.api.model.IError;
import kbee.api.model.ApiUser;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;

/**
 * Reprocesses up to size API Requests that returned 403, 412, 500 or 423 and were not overriden by a newer request
 */
public class ReprocessCommand extends AsyncCommand {

	private static String REPROCESS = "Reprocess"; // Reprocess source

	private GsonBuilder builder = new GsonBuilder();
	private Gson gson = null;
	
	private String statuses[] = {"423", "403", "412", "500", "429"};
	
	private Queue<ApiLogEvent> events;
	private int totalprogress = 0;
	private int totalitems = 0;
	
	static private Logger logger = LogManager.getLogger(ReprocessCommand.class.getName());
	static private Logger apilogger = LogManager.getLogger("ApiLogger");
	
	int limit = 0;
	
	public ReprocessCommand() {
		setName("Reprocess API Log Command");
		setParameter("limit", "1000");
		builder.registerTypeAdapterFactory(OffsetDateTimeAdapter.FACTORY);
		gson = builder.create();
	}
	
	public void executeAsync() {
		
		try {
			com.novamens.hibernate.session.Session.open();
			
			events = null;
			
			ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");

			logger.debug("Starting processing events");
			
			while (!getEvents().isEmpty() && isRunning()) {
				
				ApiLogEvent event = getEvents().poll();
				
				if (event!=null) {
					logger.debug(event.getId() + "/" + event.getResponse());
					if (event instanceof FileUpdateEvent) {
						logger.debug("reprocessing eventId: " + String.valueOf(event.getId()) + " | total: " + String.valueOf(totalprogress));
						reprocess((FileUpdateEvent)event);
					}
					if (event instanceof FileDeleteEvent) {
						logger.debug("reprocessing eventId: " + String.valueOf(event.getId()) + " | total: " + String.valueOf(totalprogress));
						reprocess((FileDeleteEvent)event);
					}
					if (event instanceof UserUpdateEvent) {
						logger.debug("reprocessing eventId: " + String.valueOf(event.getId()) + " | total: " + String.valueOf(totalprogress));
						reprocess((UserUpdateEvent)event);
					}
				}
				
				totalprogress++;
			}
			end();
		}	
		catch (Exception e) {
			logger.error("Reprocess Command Error",  e);
			stop();
		}
		finally {
			com.novamens.hibernate.session.Session.close();
			logger.debug("Total " + String.valueOf(totalprogress) + " .done");
		}
	}
	
	protected Transaction beginTransaction()  {
		return ServiceLocator.getService(TransactionService.class).beginTransaction(false);
	}
	
	
	protected void reprocess(FileUpdateEvent update) {
		try {
			ApiFile file = getFile(update);
			
			if (file.getLastModifiedDate()==null) {
				file.setLastModifiedDate(update.getTime());
			}	
			
			su(update.getUser());
			
			FileUpdateEvent newupdate = new FileUpdateEvent(file);
			
			newupdate.setId(getLogDao().getNewId());
			newupdate.setSource(REPROCESS);
			newupdate.setRetryNumber(update.getRetryNumber()+1);
			
			try {
				newupdate.setResponse(((FileUpdateAbstractHandler)getHandler("file-update")).update(file));
				Content content = getApiDao().findContentByExternalId(update.getSource(), update.getFile());
				if (content!=null) {
					update.setContentClass(content.getContentTemplate().getName());
					newupdate.setContentClass(content.getContentTemplate().getName());
				}
			}
			catch (ApiException e) {
				if (update.getRetryNumber()>0) newupdate.setRetry((long)0);
				newupdate.setResponse(new IError(e.getErrorCode(), e.getMessage()));
				newupdate.setStatus(e.getHttpStatus());
			}
			
			update.setRetry(newupdate.getId());
			update.setClosed(true);
			getLogDao().update(update);
			apilogger.info(newupdate);
		}
		catch (JsonParseException e) {
			update.setRetry((long)0);
			getLogDao().update(update);
		}
	}
	
	protected void reprocess(UserUpdateEvent update) {
		try {
			ApiUser user = getUser(update);
			
			if (user.getLastModifiedDate()==null) {
				user.setLastModifiedDate(update.getTime());
			}	
			
			su(update.getUser());
			
			UserUpdateEvent newupdate = new UserUpdateEvent(user);
			
			newupdate.setId(getLogDao().getNewId());
			newupdate.setSource(REPROCESS);
			newupdate.setRetryNumber(update.getRetryNumber()+1);
			
			try {
				newupdate.setResponse(((UserUpdateHandler)getHandler("user-update")).update(user));
//				Content content = getApiDao().findContentByExternalId(update.getSource(), update.getFile());
//				if (content!=null) {
//					update.setContentClass(content.getContentTemplate().getName());
//					newupdate.setContentClass(content.getContentTemplate().getName());
//				}
			}
			catch (ApiException e) {
				if (update.getRetryNumber()>0) newupdate.setRetry((long)0);
				newupdate.setResponse(new IError(e.getErrorCode(), e.getMessage()));
				newupdate.setStatus(e.getHttpStatus());
			}
			
			update.setRetry(newupdate.getId());
			update.setClosed(true);
			getLogDao().update(update);
			apilogger.info(newupdate);
		}
		catch (JsonParseException e) {
			update.setRetry((long)0);
			getLogDao().update(update);
		}
	}

	
	protected void reprocess(FileDeleteEvent delete) {
		try {
			su(delete.getUser());
			
			FileDeleteEvent newdelete = new FileDeleteEvent(delete.getUri());
		
			newdelete.setId(getLogDao().getNewId());
			newdelete.setSource(REPROCESS);
			newdelete.setFile(delete.getFile());
			newdelete.setRetryNumber(delete.getRetryNumber()+1);
			
			boolean error = false;
			
			try {
				Content content = getApiDao().findContentByExternalId(delete.getSource(), delete.getFile());
				if (content!=null) {
					newdelete.setDomain(content.getDomain().getName());
					newdelete.setContentClass(content.getContentTemplate().getName());
					if (delete.getDomain()==null) 
						delete.setDomain(content.getDomain().getName());
					if (delete.getContentClass()==null) 
						delete.setContentClass(content.getContentTemplate().getName());
					if (content.getLastModifiedOffsetDateTime().isBefore(delete.getTime())) {
						ApiFile file = getFile(content);
						// permanente o no permanente?
						newdelete.setResponse(((FileDeleteHandler)getHandler("file-delete")).delete(file));
					}
					else {
						newdelete.setResponse(new IError(ApiError.INVALID_VERSION, "File was modified after delete"));
						newdelete.setStatus(HttpStatus.BAD_REQUEST);
						error = true;
					}
				}
				else {
					newdelete.setResponse(new IError(ApiError.FILE_NOT_FOUND, "File not found"));
					newdelete.setStatus(HttpStatus.NOT_FOUND);
					newdelete.setDomain(getDomain(delete.getUri()));
					if (delete.getDomain()==null) 
						delete.setDomain(getDomain(delete.getUri()));
					error = true;
				}
			}
			catch (ApiException e) {
				error = true;
				newdelete.setResponse(new IError(e.getErrorCode(), e.getMessage()));
				newdelete.setStatus(e.getHttpStatus());
			}
			
			if (error && delete.getRetryNumber()>0) {
				// para que no se reprocese mas:
				newdelete.setRetry((long)0);
			}
			
			delete.setRetry(newdelete.getId());
			delete.setClosed(true);
			getLogDao().update(delete);
			apilogger.info(newdelete);
		}
		catch (JsonParseException e) {
			delete.setRetry((long)0);
			getLogDao().update(delete);
		}
	}
	
	@Override
	public double getProgress() {
		double progress;
		progress = totalprogress>0 && getTotalItems()>0 ? (double)totalprogress/(double)getTotalItems()*100 : 0;
		return progress;
	}
	
	@Override
	public long getTotalItems() {
		return totalitems;
	}
	
	@Override
	public long getTotalItemsProcessed() {
		return totalprogress;
	}

	public int getLimit() {
		if (limit>0) {
			return limit;
		}
		
		if (getParameter("limit")!=null) {
			try {
				limit = Integer.valueOf((String)getParameter("limit"));
			}
			catch (NumberFormatException e) {
				limit = 1000;
			}
		}
		else {
			limit = 1000;
		}
		
		return limit;
	}
	
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	private Queue<ApiLogEvent> getEvents() {
		if (events!=null)
			return events;
		
		events = new LinkedList<ApiLogEvent>();
		
		if (getParameter("statement")==null) {
			events.addAll(getLogDao().getEvents(statuses, getLimit()));
		}
		else {
			events.addAll(getLogDao().getEvents((String)getParameter("statement")));
		}
		
		totalitems = events.size();
		
		return events;
	}
	
	private ApiFile getFile(Content content) {
		if (content == null) {
			throw new ApiException(HttpStatus.NOT_FOUND, ApiError.FILE_NOT_FOUND);
		}
		boolean access = false;
		if (!getDomain().getName().equals(content.getDomain().getName())) {
			if ("kbee".equals(getDomain().getName())) {
				access = su(content.getDomain()) && isReadable(content);
			}
		}
		else {
			access = isReadable(content);
		}
		
		if (!access) {
			throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
		}
		
		ApiFile file = (new IDocAdapter(false)).adapt((KbeeIDoc)content);
		
		return file;
	}
	

	
	private String getDomain(String uri) {
		String domain = null;
		if (!uri.startsWith("/")) uri = "/" + uri; 
		String tokens[] = uri.split("/");
		if (tokens.length>3) {
			domain = tokens[3];
		}
		return domain;
	}
	
	private void su(String username) {
		if (!getUser().getName().equals(username)) {
			ServiceLocator.getService(SecurityService.class).authenticate(username);
		}
	}
	
	private boolean su(Domain domain) {
		try {
			if (!getDomain().equals(domain)) {
				String username = getUser().getName();
				int i = username.indexOf("@");
				if (i<0) i = username.length();
				username = username.substring(0, i);
				String suusername = username + "@" + domain.getName();
				ServiceLocator.getService(SecurityService.class).authenticate(suusername);
				return true;
			}
			return false;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	private boolean isReadable(Content content) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(content);
	}
	
	private ApiFile getFile(FileUpdateEvent event) throws JsonParseException {
		ApiFile file = gson.fromJson(event.getRequest(), ApiFile.class);
		return file;
	}
	
	private ApiUser getUser(UserUpdateEvent event) throws JsonParseException {
		ApiUser user = gson.fromJson(event.getRequest(), ApiUser.class);
		return user;
	}
	
	private RequestHandler getHandler(String handlername) {
		return (RequestHandler)ServiceLocator.getService(BeansService.class).getBean(handlername+"-handler");
	}
	
	private ApiLogDao getLogDao() {
		return (ApiLogDao) ServiceLocator.getService(BeansService.class).getBean("apiLogDao");	
	}
	
	private ApiDao getApiDao() {
		return (ApiDao)ServiceLocator.getService(BeansService.class).getBean("apiDao");
	}
	
	private User getUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	public Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
