package com.novamens.kbee.content.command;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.dom.Domain;

import com.novamens.service.ServiceLocator;

public class EmptyRecycleBinCommand extends AsyncCommand {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmptyRecycleBinCommand.class.getName());

	
	public EmptyRecycleBinCommand(Domain domain) {
		setName("Empty Recycle");
		setDomainId(domain.getId());
	}
	
	@Override
	protected void executeAsync() {
		try  {
			
			setDateStarted(OffsetDateTime.now());
			
			List<Serializable> ids = new ArrayList<Serializable>();
			
			com.novamens.hibernate.session.Session.open();
			
			ScrollableResults scrollableResults = getSession().createQuery(getStatement()).scroll();
			
			while(scrollableResults.next()) {
				Content content = (Content) scrollableResults.get()[0];
				ids.add(content.getOId());
			}
			int i = 0;
			setProgress(0);
			for (@SuppressWarnings("unused") Serializable id : ids) {
				Thread.currentThread();
				Thread.sleep(100);
				setProgress((int)((double)i++/(double)ids.size()*100));
			}
			stop();
		}
		catch (Exception e) {
			logger.error(e);
		}
		finally {
			
			setDateTerminated(OffsetDateTime.now());
			com.novamens.hibernate.session.Session.close();
		}
	//	SolrResultSet solr
	}

	protected Session getSession() {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		SessionFactory sf  = (SessionFactory)beans.getBean("sessionFactory");
		return sf.getCurrentSession();
	}
	
	protected String getStatement() {
		String statement = "select c from KbeeContent c where c.state = 1 and c.domain.id=100";
		return statement;
	}
}
