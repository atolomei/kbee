package com.novamens.kbee.content.notification;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.HashMap;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.notes.Billboard;
import com.novamens.logging.WorkNoteUpdateEvent;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.CronSchedulerService;
import com.novamens.service.ServiceLocator;


/**
 * 
 *
 */
public class BillboardCronJobServiceRequest extends AbstractCronJobRequest {
		
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger("Scheduler");
	
	static private Logger txlogger = LogManager.getLogger("TxLogger");
	

	public BillboardCronJobServiceRequest() {
		setName("Billboard");
	}

	/**
	 * Recurrent Billboard 
	 * Cron Expression
	 *  
	 * 
	 * @param note
	 */
	public BillboardCronJobServiceRequest(Billboard note) {
		
		setName("Billboard -> " + note.getTitle());
		setDescription(note.getTitle() + " | " + note.getCronExpression()!=null?note.getCronExpression().toString():"");
		setCronExpression(note.getCronExpression());
		Map<String, String> map = new HashMap<String, String>();
		map.put("note_id", note.getId().toString());
		setDomainId(note.getDomain().getId());
		setParameters(map);
		setTimeZone(note.getDomain().getTimeZone());
	}

	public String toString() {
		
		StringBuilder str= new StringBuilder();
		str.append(super.toString());
		str.append(" | note_id -> " + (getBillboardId()!=null?getBillboardId():"null"));
		
		return str.toString();
		
	}


	
	public Serializable getBillboardId() {
		return getParameters().get("note_id");
	}

	/**
	 * @see com.novamens.scheduler.ServiceRequest#execute()
	 *  Async
	 */

	@Override
public void execute() {
	
		if (getParameters()==null) {
			logger.error("parameters is null");
			return;
		}
		
		if (getParameters().get("note_id")==null) {
			logger.error("no note id");
			return;
		}
		
		try {
			
			Long note_id= Long.valueOf(getParameters().get("note_id"));
			
			Billboard note = null;
			try {
				
				note = (Billboard) getContentDao().findWorkNote(note_id);
				
				if (note.getCronExpression()==null) {
					logger.error("note.getCronExpression()==null");
					return;
				}

				if ((note.getEndpub()!=null) && note.getEndpub().isBefore(OffsetDateTime.now())) {
					ServiceLocator.getService(CronSchedulerService.class).deleteCronJob(this);
					return;
				}
				
				note.setLastModifiedOffsetDateTime(OffsetDateTime.now());
				getContentDao().save(note);

				// in this case it is not silent -> send the notifications
				txlogger.info(new WorkNoteUpdateEvent(note, false)); 
										
			} catch (ContentMgmtException e1) {
					logger.error(e1);
					return;
			}
		} catch (Exception e) {
			logger.error(e);
			
		}
		
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
}
