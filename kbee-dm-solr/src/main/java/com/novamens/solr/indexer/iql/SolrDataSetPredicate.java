package com.novamens.solr.indexer.iql;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.DataSet;
import com.novamens.dom.Domain;
import com.novamens.indexer.iql.CalculatedPredicate;
import com.novamens.service.ServiceLocator;

public class SolrDataSetPredicate extends SolrAbstractPredicate  implements CalculatedPredicate {
	
	//private Classifier classifier;
	//private MemberDao memberDao;
	Domain domain;
	
	@Override
	public String getHelpValueTypeDescription() {
		return "Dataset";
	}
	
	@Override
	public boolean isInformatioModel() {
		return true;
	}
	
	public boolean isCanonical() {
		return false;
	}
	
	public String getCode(String argument) {
		
		String code;
		
		DataSet dataset = getDataSet(argument);
		
		
		code = getPath() + ":" + (dataset!=null ? String.valueOf(dataset.getId()) : "-");
	
		return code;
	}
	
	
	public boolean evaluate(Object object, Object argument) {
		return false;
	}
	
	public String getPath() {
		return "dataset";
	}
	
	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	public String getArgument(String value) {
		return value;
	}

	protected DataSet getDataSet(String argument) {
		for (DataSet dataset : getContentDao().getDataSets(getDomain())) {
			if (dataset.getAlias().equals(argument)) {
				return dataset;
			}
		}
		return null;
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
