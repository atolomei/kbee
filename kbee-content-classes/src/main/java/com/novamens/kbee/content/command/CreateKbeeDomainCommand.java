package com.novamens.kbee.content.command;

import java.time.OffsetDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.kbee.domain.KbeeDomain;
import com.novamens.service.ServiceLocator;

public class CreateKbeeDomainCommand extends AbstractCommand   {
	
	static Logger logger = LogManager.getLogger(CreateKbeeDomainCommand.class.getName());
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(getClass().getSimpleName());
		return str.toString();	
	}
	
	@Override
	public void execute() {

		setDateStarted(OffsetDateTime.now());
		setProgress(0);
		
		Domain kbee = null;
		
		List<Domain> domains = getContentDao().getDomains();
		
		for (Domain dom: domains) {
			if (dom.getName().equals("kbee")) {
				kbee=dom;
				break;
			}
		}
		
		if (kbee==null) {
			
			kbee = new KbeeDomain();
			
			kbee.setDomainType(DomainType.PREMIUM);
			kbee.setEncryptFiles(true);
			kbee.setEnabled(true);
			kbee.setName("kbee");
		}
	}
	 
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

}
