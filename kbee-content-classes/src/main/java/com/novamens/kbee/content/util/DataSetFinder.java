package com.novamens.kbee.content.util;

import org.apache.logging.log4j.LogManager;
import org.hibernate.SessionFactory;

import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.DataSet;

public class DataSetFinder {
	private ContentDao dao;
	SessionFactory sessionFactory;
	
	private org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getName());

	public DataSetFinder() {
	}
	
	public void setContentDao(ContentDao dao) {
		this.dao = dao;
	}
	
	public void setSessionFactory(SessionFactory factory) {
		sessionFactory = factory;
	}
	
	@org.springframework.transaction.annotation.Transactional
	public DataSet find(String datasetname, String domainid) {
		DataSet dataset = (DataSet)dao.findModelObjectByName(DataSet.class, datasetname, Long.valueOf(domainid));
		if (dataset==null) logger.warn("DataSet "+datasetname +" not found in domain " + domainid);
		return dataset;
	}
}
