package com.novamens.logging;

import org.hibernate.CacheMode;

import com.novamens.hibernate.query.HibernateQuery;

public class ActivitiesQuery extends HibernateQuery {
	private static final long serialVersionUID = 1L;
	
	private long processid = 0;
	
	public ActivitiesQuery(long processid) {
		this.processid = processid;
	}

	@Override
	public CacheMode getCacheMode() {
		return CacheMode.IGNORE;
	}
	
	@Override
	public String getStatement() {
		
		String statement = "from KbeeWorkflowActivity A where A.process.id= "+String.valueOf(processid);

		setStatement(statement);

		return statement;
	}
 
}