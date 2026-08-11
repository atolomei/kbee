package com.novamens.kbee.content.command;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;


public class ReindexEverythingCommand extends AbstractCommand {
			
	private Logger clog = LogManager.getLogger(ReindexEverythingCommand.class.getName());

	private Boolean attachments = Boolean.valueOf(true);
	
	private int index = 0;
	private int total_objects = 0;
	
	private Map<Serializable, ReindexDomainCommand> commands = new HashMap<Serializable, ReindexDomainCommand>();

	public ReindexEverythingCommand() {
		setName("Reindex Everything");
	}
	
	public ReindexEverythingCommand(boolean is_attachments) {
		setName("Reindex Everything");
		this.attachments= Boolean.valueOf(is_attachments);
	}

	
	public Boolean isAttachments() {
		return this.attachments;
	}
	
	
	public void  setAttachments(Boolean b) {
		this.attachments=b;
	}
	
	
	@Override
	public void execute() {

		try {
			
			setDateStarted(OffsetDateTime.now());
			//setLogger(getLoggerName());
			
			int n = 0;
			int total=getContentDao().getDomains().size();
			
			if (total>0) {
				
				this.total_objects = 0;
				
				for (Domain domain: getContentDao().getDomains()) {
					ReindexDomainCommand rdc = new ReindexDomainCommand(domain) {
						public void onIndex(Object object) {
								super.onIndex(object);
								double in = Double.valueOf(ReindexEverythingCommand.this.index++).doubleValue();
								double to = Double.valueOf(ReindexEverythingCommand.this.total_objects).doubleValue();
								ReindexEverythingCommand.this.setProgress(100.0 * in / to);
						}
					};
					
					this.total_objects += rdc.getTotalItems();
					rdc.setIncludeAttachments(isAttachments());
					this.commands.put(domain.getId(), rdc);
					clog.info("Domain: "+ domain.getName() + " items: " + rdc.getTotalItems());
					
				}

				
				if (this.total_objects==0)
					this.total_objects=1;
				
				for (Domain domain: getContentDao().getDomains()) {
					ReindexDomainCommand rdc = commands.get(domain.getId());
					clog.info("Starting domain ("+ String.valueOf(++n)+"/"+ String.valueOf(total)+ "): " + domain.getName());
					rdc.execute();
					clog.info("done");
				}
			}
		}
		finally {
			
			setDateTerminated(OffsetDateTime.now());
			
			if (!isStopped()) {
				setProgress(100.00);
				setResult("Ok");
				setState(CommandState.COMPLETED);
			}
			else {
				setResult("Cancelled by user.");
				setState(CommandState.CANCELED);
			}
		}
	}

	@Override
	public long getTotalItems() {
		return total_objects;
	}
	
	
	@Override
	public long getTotalItemsProcessed() {
		return index;
	}

	
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	private String getLoggerName() {
		
		String name = "logs/reindex-everything-";
		DateFormat format = new SimpleDateFormat("MM-dd-yyyy");
		name += format.format(new Date());
		name += "-" + String.valueOf(getId()) + ".log";
		return name;
		
	}
}
