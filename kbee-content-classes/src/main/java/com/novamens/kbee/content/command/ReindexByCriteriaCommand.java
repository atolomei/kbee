package com.novamens.kbee.content.command;

import java.time.OffsetDateTime;

import org.hibernate.SessionFactory;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.model.ObjectId;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.hibernate.session.Session;
import com.novamens.indexer.java.KbeeJavaIndex;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexProxy;
import com.novamens.indexer.service.IndexerException;
import com.novamens.security.Identifiable;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class ReindexByCriteriaCommand extends AsyncCommand {
											
	private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReindexByCriteriaCommand.class.getName());
	
	private Index index;
	private Query query;
	private Domain domain;
	private String[] fields = null;
	private long totalitems = 0, totalindexed = 0;
	
	public ReindexByCriteriaCommand(Query query, Index index) {
		setQuery(query);
		setIndex(index);
	}
	
	public ReindexByCriteriaCommand(Query query, Index index, String...fields) {
		setQuery(query);
		setIndex(index);
		this.fields = fields;
	}
	
	public Index getIndex() {
		return ((IndexProxy)index).getIndex();
	}
	
	public void setIndex(Index index) {
		this.index = index;
	}
	
	public Query getQuery() {
		return this.query;
	}
	
	public void setQuery(Query query) {
		this.query = query;
	}
	
	@Override
	public void executeAsync() {
		setDateStarted(OffsetDateTime.now());
		ResultSet resultSet = null;
		try {
			Session.open();
			resultSet = getQuery().execute();
			String condition = (String)getParameter("condition");
			condition = condition==null ? "" : "("+condition+") ";
			totalitems = resultSet.size();
			while (resultSet.hasNext() && !isStopped()) {
				Object object = next(resultSet);
				if (object instanceof DomainObject) {
					su(((DomainObject)object).getDomain());
					if (fields!=null && object instanceof Content) {
						((KbeeJavaIndex)this.getIndex()).reindex(object, fields);
					}
					else {
						((KbeeJavaIndex)this.getIndex()).index(object);
					}
				}
				else {
					logger.debug("Not a domain object "+getId(object));
				}
				totalindexed++;
				if (totalindexed%1000==0) {
					((SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory")).getCurrentSession().clear();
				}
			}
			this.getIndex().commit();
			end();
			long duration = getDuration();
			logger.debug("duration " + duration);
			
		}
		catch (IndexerException e) {
			logger.error(e);
			stop();
			throw new RuntimeException(e);
		}
		finally {
 			if (resultSet!=null)
				resultSet.close();
			Session.close();
		}
	}
	
	@Override
	public long getTotalItemsProcessed() {
		return totalindexed;
	}
	
	@Override
	public long getTotalItems() {
		return totalitems;
	}
	
	@Override
	public double getProgress() {
		return totalitems>0 ? (double)totalindexed/(double)totalitems*100 : 0;
	}
	
	protected String getId(Object object) {
		String id;
		if (object instanceof Identifiable) {
			id = (new ObjectId(object)).toString(); 
		}
		else {
			try {
				id = object!=null ? object.toString() : "null";
			} 
			catch (Exception e) {
				return "null";
			}
		}
		return id;
	}
	
	protected Object next(ResultSet resultSet) {
		Object object = resultSet.next().getObject();
		if (object instanceof HibernateProxy) {
			HibernateProxy proxy = (HibernateProxy)object;
			LazyInitializer initializer = proxy.getHibernateLazyInitializer();
			object = initializer.getImplementation();
		}
		return object;
	}
	
	protected void su(Domain domain) {
		if (this.domain==null || !domain.equals(this.domain)) {
			ServiceLocator.getService(SecurityService.class).authenticate("root@" + domain.getName());
			this.domain = domain;
		}
	}
}
